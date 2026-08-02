'use client';

import { useEffect, useState } from 'react';
import { useSearchParams } from 'next/navigation';
import { events as eventsApi } from '@/lib/api';

export default function EventsPage() {
  const searchParams = useSearchParams();
  const [data, setData] = useState<any>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    setLoading(true);
    const params: Record<string, string> = {};
    searchParams.forEach((v, k) => (params[k] = v));
    eventsApi
      .search(params)
      .then((res: any) => setData(res))
      .catch((e) => setError(e.message))
      .finally(() => setLoading(false));
  }, [searchParams]);

  if (loading) return <p className="text-center py-12">Loading events…</p>;
  if (error) return <p className="text-center py-12 text-red-600">Error: {error}. Is the backend running?</p>;

  const list = data?.content || data || [];

  return (
    <div>
      <h1 className="text-2xl font-bold mb-6">Events</h1>
      {list.length === 0 ? (
        <p className="text-gray-500">No events found. Create one from the Organize page after logging in.</p>
      ) : (
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
          {list.map((ev: any) => (
            <a
              key={ev.id || ev.slug}
              href={`/events/${ev.slug}`}
              className="bg-white rounded-xl border overflow-hidden hover:shadow-md transition"
            >
              <div className="h-36 bg-gradient-to-br from-indigo-400 to-purple-500" />
              <div className="p-4">
                <h3 className="font-semibold line-clamp-2">{ev.title}</h3>
                <p className="text-sm text-gray-500 mt-1">
                  {ev.startAt ? new Date(ev.startAt).toLocaleString() : 'TBA'}
                </p>
                <p className="text-xs text-gray-400 mt-1 capitalize">{ev.status}</p>
              </div>
            </a>
          ))}
        </div>
      )}
    </div>
  );
}
