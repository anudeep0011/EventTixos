'use client';

import { useState } from 'react';
import { useRouter } from 'next/navigation';

export default function HomePage() {
  const [q, setQ] = useState('');
  const router = useRouter();

  const handleSearch = (e: React.FormEvent) => {
    e.preventDefault();
    router.push(`/events?q=${encodeURIComponent(q)}`);
  };

  const categories = [
    'Music', 'Comedy', 'Workshops', 'Business', 'Food & Drink',
    'Arts & Culture', 'Sports & Fitness', 'Networking',
  ];

  return (
    <div>
      <section className="text-center py-16">
        <h1 className="text-4xl md:text-5xl font-bold tracking-tight mb-4">
          Discover events near you
        </h1>
        <p className="text-lg text-gray-600 mb-8 max-w-xl mx-auto">
          Concerts, comedy nights, workshops and more. Buy tickets in under 2 minutes.
        </p>
        <form onSubmit={handleSearch} className="max-w-lg mx-auto flex gap-2">
          <input
            type="search"
            value={q}
            onChange={(e) => setQ(e.target.value)}
            placeholder="Search events, cities, categories..."
            className="flex-1 border rounded-lg px-4 py-3 text-base focus:outline-none focus:ring-2 focus:ring-indigo-500"
          />
          <button type="submit" className="bg-indigo-600 text-white px-6 py-3 rounded-lg font-medium hover:bg-indigo-700">
            Search
          </button>
        </form>
      </section>
      <section className="mb-12">
        <h2 className="text-xl font-semibold mb-4">Browse by category</h2>
        <div className="flex flex-wrap gap-2">
          {categories.map((c) => (
            <a
              key={c}
              href={`/events?category=${encodeURIComponent(c.toLowerCase().replace(/ & /g, '-').replace(/ /g, '-'))}`}
              className="px-4 py-2 bg-white border rounded-full text-sm hover:border-indigo-500 hover:text-indigo-600 transition"
            >
              {c}
            </a>
          ))}
        </div>
      </section>
      <section>
        <h2 className="text-xl font-semibold mb-4">Featured this week</h2>
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
          {[1, 2, 3].map((i) => (
            <a key={i} href="/events" className="bg-white rounded-xl border overflow-hidden hover:shadow-md transition">
              <div className="h-40 bg-gradient-to-br from-indigo-400 to-purple-500" />
              <div className="p-4">
                <h3 className="font-semibold">Sample Event {i}</h3>
                <p className="text-sm text-gray-500 mt-1">Hyderabad · Coming soon</p>
                <p className="text-sm font-medium mt-2">From ₹299</p>
              </div>
            </a>
          ))}
        </div>
      </section>
    </div>
  );
}
