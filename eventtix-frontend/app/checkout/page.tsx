'use client';

import { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import { checkout } from '@/lib/api';

export default function CheckoutPage() {
  const router = useRouter();
  const [data, setData] = useState<any>(null);
  const [email, setEmail] = useState('');
  const [name, setName] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [expiresIn, setExpiresIn] = useState(0);
  const [done, setDone] = useState<any>(null);

  useEffect(() => {
    const raw = sessionStorage.getItem('checkout');
    if (!raw) { router.replace('/events'); return; }
    const parsed = JSON.parse(raw);
    setData(parsed);
    const tick = () => {
      const left = Math.max(0, Math.floor((new Date(parsed.expiresAt).getTime() - Date.now()) / 1000));
      setExpiresIn(left);
      if (left <= 0) { sessionStorage.removeItem('checkout'); setError('Hold expired. Please try again.'); }
    };
    tick();
    const id = setInterval(tick, 1000);
    return () => clearInterval(id);
  }, [router]);

  const handlePay = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!data) return;
    setLoading(true);
    setError('');
    try {
      const intent: any = await checkout.createIntent({
        tierId: data.tier.id, quantity: data.quantity, holdKey: data.holdKey,
        guestEmail: email, guestName: name,
      });
      if (intent.paymentProvider === 'MOCK' || !intent.clientSecret?.startsWith('pi_')) {
        const confirmed: any = await checkout.confirmMock({ orderId: intent.orderId });
        sessionStorage.removeItem('checkout');
        setDone(confirmed);
        return;
      }
      sessionStorage.removeItem('checkout');
      alert(`Payment intent ${intent.providerPaymentId} created.`);
      router.push('/my-tickets');
    } catch (err: any) {
      setError(err.message || 'Payment failed');
    } finally {
      setLoading(false);
    }
  };

  if (done) {
    return (
      <div className="max-w-md mx-auto text-center py-12">
        <div className="text-4xl mb-4">✓</div>
        <h1 className="text-2xl font-bold mb-2">Tickets confirmed</h1>
        <p className="text-gray-600 mb-4">Order {done.orderId} — {done.ticketCount} ticket(s) issued.</p>
        <a href="/my-tickets" className="text-indigo-600 hover:underline">View my tickets →</a>
      </div>
    );
  }

  if (!data) return <p className="text-center py-12">Loading checkout…</p>;
  const total = ((data.tier.priceCents * data.quantity) / 100).toFixed(0);

  return (
    <div className="max-w-md mx-auto">
      <h1 className="text-2xl font-bold mb-2">Checkout</h1>
      <p className="text-sm text-amber-600 mb-6">Hold expires in {Math.floor(expiresIn / 60)}:{String(expiresIn % 60).padStart(2, '0')}</p>
      <div className="bg-white border rounded-xl p-5 mb-6">
        <h2 className="font-medium">{data.event?.title}</h2>
        <p className="text-sm text-gray-500 mt-1">{data.tier.name} × {data.quantity}</p>
        <p className="text-lg font-semibold mt-3">₹{total}</p>
      </div>
      <form onSubmit={handlePay} className="space-y-4 bg-white border rounded-xl p-5">
        {error && <p className="text-red-600 text-sm">{error}</p>}
        <div>
          <label className="block text-sm mb-1">Name</label>
          <input required value={name} onChange={(e) => setName(e.target.value)} className="w-full border rounded-lg px-3 py-2" />
        </div>
        <div>
          <label className="block text-sm mb-1">Email (for tickets)</label>
          <input type="email" required value={email} onChange={(e) => setEmail(e.target.value)} className="w-full border rounded-lg px-3 py-2" />
        </div>
        <button type="submit" disabled={loading || expiresIn <= 0}
          className="w-full bg-indigo-600 text-white py-2.5 rounded-lg font-medium hover:bg-indigo-700 disabled:opacity-50">
          {loading ? 'Processing…' : `Confirm ₹${total}`}
        </button>
        <p className="text-xs text-gray-400 text-center">Demo mode confirms instantly without a live payment provider.</p>
      </form>
    </div>
  );
}
