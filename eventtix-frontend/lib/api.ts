const API_BASE = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080/api';

export async function api<T>(path: string, options: RequestInit = {}): Promise<T> {
  const token = typeof window !== 'undefined' ? localStorage.getItem('accessToken') : null;
  const res = await fetch(`${API_BASE}${path}`, {
    ...options,
    headers: {
      'Content-Type': 'application/json',
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
      ...options.headers,
    },
  });
  if (!res.ok) {
    const err = await res.json().catch(() => ({ message: res.statusText }));
    throw new Error(err.message || `HTTP ${res.status}`);
  }
  return res.json();
}

export const auth = {
  signup: (body: { email: string; password: string; fullName: string; phone?: string }) =>
    api('/auth/signup', { method: 'POST', body: JSON.stringify(body) }),
  login: (body: { email: string; password: string }) =>
    api('/auth/login', { method: 'POST', body: JSON.stringify(body) }),
  becomeOrganizer: (body: { displayName: string; bio?: string }) =>
    api('/auth/become-organizer', { method: 'POST', body: JSON.stringify(body) }),
};

export const events = {
  search: (params: Record<string, string | number | undefined>) => {
    const q = new URLSearchParams();
    Object.entries(params).forEach(([k, v]) => v != null && q.set(k, String(v)));
    return api(`/events?${q}`);
  },
  getBySlug: (slug: string) => api(`/events/${slug}`),
  create: (body: any) => api('/organizer/events', { method: 'POST', body: JSON.stringify(body) }),
  publish: (id: string) => api(`/organizer/events/${id}/publish`, { method: 'POST' }),
  addTier: (id: string, body: any) =>
    api(`/organizer/events/${id}/ticket-tiers`, { method: 'POST', body: JSON.stringify(body) }),
  myEvents: () => api('/organizer/events'),
  dashboard: (id: string) => api(`/organizer/events/${id}/dashboard`),
};

export const categories = { list: () => api('/categories') };

export const checkout = {
  hold: (body: { tierId: string; quantity: number; sessionId: string }) =>
    api('/checkout/hold', { method: 'POST', body: JSON.stringify(body) }),
  createIntent: (body: any) =>
    api('/checkout/create-payment-intent', { method: 'POST', body: JSON.stringify(body) }),
  confirmMock: (body: { orderId: string }) =>
    api('/checkout/confirm-mock', { method: 'POST', body: JSON.stringify(body) }),
};

export const checkin = {
  scan: (body: { qrPayload: string; eventId: string }) =>
    api('/checkin/scan', { method: 'POST', body: JSON.stringify(body) }),
};
