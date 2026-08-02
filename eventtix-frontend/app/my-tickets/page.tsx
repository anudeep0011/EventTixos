'use client';

export default function MyTicketsPage() {
  return (
    <div>
      <h1 className="text-2xl font-bold mb-6">My Tickets</h1>
      <div className="bg-white border rounded-xl p-8 text-center text-gray-500">
        <p className="mb-2">Your tickets will appear here after a successful purchase.</p>
        <p className="text-sm">
          Once the webhook confirms payment, tickets with QR codes are generated and emailed.
        </p>
        <a href="/events" className="inline-block mt-4 text-indigo-600 hover:underline">
          Browse events →
        </a>
      </div>
    </div>
  );
}
