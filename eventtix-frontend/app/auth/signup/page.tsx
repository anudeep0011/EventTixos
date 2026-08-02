'use client';

import { useState } from 'react';
import { useRouter } from 'next/navigation';
import { auth } from '@/lib/api';

export default function SignupPage() {
  const router = useRouter();
  const [form, setForm] = useState({ email: '', password: '', fullName: '', phone: '' });
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setError('');
    try {
      const res: any = await auth.signup(form);
      localStorage.setItem('accessToken', res.accessToken);
      localStorage.setItem('user', JSON.stringify({
        id: res.userId, email: res.email, fullName: res.fullName, role: res.role,
      }));
      router.push('/');
    } catch (err: any) {
      setError(err.message || 'Signup failed');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="max-w-md mx-auto">
      <h1 className="text-2xl font-bold mb-6">Create account</h1>
      <form onSubmit={handleSubmit} className="space-y-4 bg-white p-6 rounded-xl border">
        {error && <p className="text-red-600 text-sm">{error}</p>}
        <div>
          <label className="block text-sm mb-1">Full name</label>
          <input required value={form.fullName} onChange={(e) => setForm({ ...form, fullName: e.target.value })}
            className="w-full border rounded-lg px-3 py-2" />
        </div>
        <div>
          <label className="block text-sm mb-1">Email</label>
          <input type="email" required value={form.email} onChange={(e) => setForm({ ...form, email: e.target.value })}
            className="w-full border rounded-lg px-3 py-2" />
        </div>
        <div>
          <label className="block text-sm mb-1">Password (min 8)</label>
          <input type="password" required minLength={8} value={form.password}
            onChange={(e) => setForm({ ...form, password: e.target.value })}
            className="w-full border rounded-lg px-3 py-2" />
        </div>
        <div>
          <label className="block text-sm mb-1">Phone (optional)</label>
          <input value={form.phone} onChange={(e) => setForm({ ...form, phone: e.target.value })}
            className="w-full border rounded-lg px-3 py-2" />
        </div>
        <button type="submit" disabled={loading}
          className="w-full bg-indigo-600 text-white py-2.5 rounded-lg font-medium hover:bg-indigo-700 disabled:opacity-50">
          {loading ? 'Creating…' : 'Create account'}
        </button>
      </form>
      <p className="text-center text-sm mt-4">
        Already have an account? <a href="/auth/login" className="text-indigo-600">Log in</a>
      </p>
    </div>
  );
}
