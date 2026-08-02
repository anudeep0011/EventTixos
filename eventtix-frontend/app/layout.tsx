import type { Metadata } from 'next';
import './globals.css';

export const metadata: Metadata = {
  title: 'EventTix — Discover & Book Events',
  description: 'Event discovery and ticketing platform',
};

export default function RootLayout({ children }: { children: React.ReactNode }) {
  return (
    <html lang="en">
      <body className="min-h-screen bg-gray-50 text-gray-900 antialiased">
        <header className="border-b bg-white sticky top-0 z-50">
          <div className="max-w-6xl mx-auto px-4 h-14 flex items-center justify-between">
            <a href="/" className="text-xl font-bold text-indigo-600">
              EventTix
            </a>
            <nav className="flex items-center gap-4 text-sm">
              <a href="/events" className="hover:text-indigo-600">Browse</a>
              <a href="/organize" className="hover:text-indigo-600">Organize</a>
              <a href="/my-tickets" className="hover:text-indigo-600">My Tickets</a>
              <a href="/checkin" className="hover:text-indigo-600">Scanner</a>
              <a
                href="/auth/login"
                className="bg-indigo-600 text-white px-3 py-1.5 rounded-md hover:bg-indigo-700"
              >
                Login
              </a>
            </nav>
          </div>
        </header>
        <main className="max-w-6xl mx-auto px-4 py-8">{children}</main>
        <footer className="border-t mt-16 py-8 text-center text-sm text-gray-500">
          EventTix · Built for independent organizers
        </footer>
      </body>
    </html>
  );
}
