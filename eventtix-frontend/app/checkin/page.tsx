'use client';

import { useState } from 'react';
import { checkin } from '@/lib/api';

export default function CheckinPage() {
  const [qrPayload, setQrPayload] = useState('');
  const [eventId, setEventId] = useState('');
  const [result, setResult] = useState<any>(null);
  const [loading, setLoading] = useState(false);

  const handleScan = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setResult(null);
    try {
      const res = await checkin.scan({ qrPayload, eventId });
      setResult(res);
    } catch (err: any) {
      setResult({ valid: false, message: err.message });
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="max-w-md mx-auto">
      <h1 className="text-2xl font-bold mb-2">Ticket Scanner</h1>
      <p className="text-sm text-gray-500 mb-6">For door staff. Paste QR payload to validate.</p>
      <form onSubmit={handleScan} className="space-y-4 bg-white border rounded-xl p-5">
        <div>
          <label className="block text-sm mb-1">Event ID</label>
          <input required value={eventId} onChange={(e) => setEventId(e.target.value)}
            placeholder="UUID of the event" className="w-full border rounded-lg px-3 py-2 text-sm" />
        </div>
        <div>
          <label className="block text-sm mb-1">QR Payload</label>
          <textarea required value={qrPayload} onChange={(e) => setQrPayload(e.target.value)}
            rows={3} placeholder="ticketId|eventId|signature"
            className="w-full border rounded-lg px-3 py-2 text-sm font-mono" />
        </div>
        <button type="submit" disabled={loading}
          className="w-full bg-black text-white py-3 rounded-lg font-medium hover:bg-gray-800 disabled:opacity-50">
          {loading ? 'Validating…' : 'Validate Ticket'}
        </button>
      </form>
      {result && (
        <div className={`mt-6 p-6 rounded-xl text-center text-white ${result.valid ? 'bg-green-600' : 'bg-red-600'}`}>
          <div className="text-3xl font-bold mb-2">{result.valid ? 'VALID' : 'INVALID'}</div>
          <p className="text-sm opacity-90">{result.message}</p>
        </div>
      )}
    </div>
  );
}
