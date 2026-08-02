'use client';

import { useEffect, useState } from 'react';
import { api } from '@/lib/api';

export default function AdminPage() {
  const [metrics, setMetrics] = useState<any>(null);
  const [error, setError] = useState('');

  useEffect(() => {
    api('/admin/metrics')
      .then(setMetrics)
      .catch((e) => setError(e.message || 'Admin access required'));
  }, []);

  if (error) {
    return (
      <div className="text-center py-16">
        <p className="text-red-600 mb-2">{error}</p>
        <p className="text-sm text-gray-500">You need an ADMIN role account.</p>
      </div>
    );
  }

  if (!metrics) return <p className="py-12 text-center">Loading metrics…</p>;

  return (
    <div>
      <h1 className="text-2xl font-bold mb-6">Admin panel</h1>
      <div className="grid grid-cols-2 md:grid-cols-4 gap-4 mb-10">
        <Metric label="Users" value={metrics.totalUsers} />
        <Metric label="Events" value={metrics.totalEvents} />
        <Metric label="Orders" value={metrics.totalOrders} />
        <Metric label="GMV" value={`₹${((metrics.gmvCents || 0) / 100).toFixed(0)}`} />
        <Metric label="Active organizers" value={metrics.activeOrganizers} />
      </div>
      <div className="bg-white border rounded-xl p-6 text-sm text-gray-600">
        <p className="font-medium text-gray-900 mb-2">Admin APIs available at /api/admin/*</p>
        <p className="text-xs mt-2">Promote a user to ADMIN in the database for full access.</p>
      </div>
    </div>
  );
}

function Metric({ label, value }: { label: string; value: any }) {
  return (
    <div className="bg-white border rounded-xl p-4">
      <p className="text-xs text-gray-500">{label}</p>
      <p className="text-2xl font-bold mt-1">{value ?? '—'}</p>
    </div>
  );
}
