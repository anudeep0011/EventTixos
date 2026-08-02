'use client';

import { useEffect, useState } from 'react';
import { useParams, useRouter } from 'next/navigation';
import { events as eventsApi, checkout } from '@/lib/api';

export default function EventDetailPage() {
  const { slug } = useParams<{ slug: string }>();
  const router = useRouter();
  const [detail, setDetail] = useState<any>(null);
  const [loading, setLoading] = useState(true);
  const [qty, setQty] = useState(1);
  const [selectedTier, setSelectedTier] = useState<any>(null);
  const [holding, setHolding] = useState(false);

  useEffect(() => {
    eventsApi.getBySlug(slug).then((res: any) => {
      setDetail(res);
      if (res.tiers?.length) setSelectedTier(res.tiers[0]);
    }).catch(console.error).finally(() => setLoading(false));
  }, [slug]);

  const handleGetTickets = async () => {
    if (!selectedTier) return;
    setHolding(true);
    try {
      const sessionId = crypto.randomUUID();
      const hold = await checkout.hold({
        tierId: selectedTier.id,
        quantity: qty,
        sessionId,
      });
      sessionStorage.setItem('checkout', JSON.stringify({
        holdKey: hold.holdKey,
        expiresAt: hold.expiresAt,
        tier: selectedTier,
        quantity: qty,
        event: detail.event,
      }));
      router.push('/checkout');
    } catch (e: any) {
      alert(e.message || 'Could not hold tickets');
    } finally {
      setHolding(false);
    }
  };

  if (loading) return <p className="py-12 text-center">Loading…</p>;
  if (!detail) return <p className="py-12 text-center">Event not found</p>;

  const { event, tiers } = detail;

  return (
    <div className="max-w-3xl mx-auto">
      <div className="h-56 bg-gradient-to-br from-indigo-500 to-purple-600 rounded-xl mb-6" />
      <h1 className="text-3xl font-bold mb-2">{event.title}</h1>
      <p className="text-gray-600 mb-4">
        {event.startAt ? new Date(event.startAt).toLocaleString() : 'Date TBA'}
      </p>
      <p className="text-gray-700 whitespace-pre-wrap mb-8">{event.description || 'No description.'}</p>
      <div className="bg-white border rounded-xl p-6">
        <h2 className="font-semibold mb-4">Tickets</h2>
        {tiers?.length ? (
          <div className="space-y-3">
            {tiers.map((t: any) => (
              <label key={t.id} className={`flex items-center justify-between p-3 border rounded-lg cursor-pointer ${
                selectedTier?.id === t.id ? 'border-indigo-500 bg-indigo-50' : ''
              }`}>
                <div className="flex items-center gap-3">
                  <input type="radio" name="tier" checked={selectedTier?.id === t.id}
                    onChange={() => setSelectedTier(t)} />
                  <div>
                    <div className="font-medium">{t.name}</div>
                    <div className="text-sm text-gray-500">
                      {(t.available ?? t.quantityTotal - t.quantitySold)} left
                    </div>
                  </div>
                </div>
                <div className="font-semibold">
                  {t.priceCents === 0 ? 'Free' : `₹${(t.priceCents / 100).toFixed(0)}`}
                </div>
              </label>
            ))}
            <div className="flex items-center gap-4 pt-4">
              <label className="text-sm">
                Qty:{' '}
                <input type="number" min={1} max={selectedTier?.maxPerOrder || 10} value={qty}
                  onChange={(e) => setQty(Number(e.target.value))} className="w-16 border rounded px-2 py-1 ml-1" />
              </label>
              <button onClick={handleGetTickets} disabled={holding || !selectedTier}
                className="flex-1 bg-indigo-600 text-white py-2.5 rounded-lg font-medium hover:bg-indigo-700 disabled:opacity-50">
                {holding ? 'Reserving…' : 'Get Tickets'}
              </button>
            </div>
          </div>
        ) : (
          <p className="text-gray-500">No ticket tiers yet.</p>
        )}
      </div>
    </div>
  );
}
