import { Analytics } from '@vercel/analytics/next'
import type { Metadata, Viewport } from 'next'
import type { ReactNode } from 'react'
import { Toaster } from '@/components/ui/sonner'
import { AuthProvider } from "@/lib/auth-context";
import './globals.css'

export const metadata: Metadata = {
  title: {
    default: 'DevTrack — Project and issue tracking',
    template: '%s | DevTrack',
  },
  description: 'Manage projects, issues, teams, and technology stacks in one clear workspace.',
  applicationName: 'DevTrack',
  generator: 'Next.js',
  icons: {
    icon: '/icon.svg',
  },
  openGraph: {
    title: 'DevTrack — Project and issue tracking',
    description: 'A focused workspace for projects, issues, and the technologies behind them.',
    type: 'website',
  },
}

export const viewport: Viewport = {
  width: 'device-width',
  initialScale: 1,
  viewportFit: 'cover',
  colorScheme: 'light',
  themeColor: '#f7f8fc',
}

export default function RootLayout({
  children,
}: Readonly<{
  children: ReactNode
}>) {
  return (
  <html lang="en">
    <body className="antialiased">
      <AuthProvider>
        {children}
      </AuthProvider>

      <Toaster position="top-right" richColors closeButton />
    </body>
  </html>
);
}
