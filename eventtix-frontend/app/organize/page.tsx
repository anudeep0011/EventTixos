'use client';

import { useState } from 'react';
import { useRouter } from 'next/navigation';
import { events, auth } from '@/lib/api';

export default function OrganizePage() {
  const router = useRouter();
  const [form, setForm] = useState({ title: '', description: '', startAt: '', categoryId: 1 });
  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState('');

  const handleCreate = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setMessage('');
    try {
      try {
        await auth.becomeOrganizer({ displayName: form.title || 'My Org' });
      } catch {
        /* already organizer or not logged in */
      }
      const event: any = await events.create({
        title: form.title,
        description: form.description,
        startAt: new Date(form.startAt).toISOString(),
        categoryId: form.categoryId,
        visibility: 'PUBLIC',
      });
      await events.addTier(event.id, {
        name: 'General Admission',
        priceCents: 0,
        quantityTotal: 100,
        minPerOrder: 1,
        maxPerOrder: 5,
      });
      await events.publish(event.id);
      setMessage(`Published! View at /events/${event.slug}`);
      setTimeout(() => router.push(`/events/${event.slug}`), 1200);
    } catch (err: any) {
      setMessage(err.message || 'Failed — log in first');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="max-w-lg mx-auto">
      <h1 className="text-2xl font-bold mb-2">Create an event</h1>
      <p className="text-sm text-gray-500 mb-6">Log in first, then create & publish.</p>
      <form onSubmit={handleCreate} className="space-y-4 bg-white border rounded-xl p-6">
        <div>
          <label className="block text-sm mb-1">Event title</label>
          <input required value={form.title} onChange={(e) => setForm({ ...form, title: e.target.value })}
            className="w-full border rounded-lg px-3 py-2" placeholder="Hyderabad Comedy Night" />
        </div>
        <div>
          <label className="block text-sm mb-1">Description</label>
          <textarea value={form.description} onChange={(e) => setForm({ ...form, description: e.target.value })}
            rows={3} className="w-full border rounded-lg px-3 py-2" />
        </div>
        <div>
          <label className="block text-sm mb-1">Start date & time</label>
          <input type="datetime-local" required value={form.startAt}
            onChange={(e) => setForm({ ...form, startAt: e.target.value })}
            className="w-full border rounded-lg px-3 py-2" />
        </div>
        <button type="submit" disabled={loading}
          className="w-full bg-indigo-600 text-white py-2.5 rounded-lg font-medium hover:bg-indigo-700 disabled:opacity-50">
          {loading ? 'Creating…' : 'Create & Publish'}
        </button>
        {message && <p className="text-sm text-center mt-2">{message}</p>}
      </form>
    </div>
  );
}
