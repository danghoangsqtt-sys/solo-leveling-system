import type { Metadata } from "next";
import "./globals.css";

export const metadata: Metadata = {
  title: "System Leveling — Gamified Life Management",
  description:
    "Biến cuộc sống thành RPG. Quản lý nhiệm vụ, kỹ năng, tài chính và học tập với NPC trợ lý ảo Aura.",
  keywords: [
    "solo leveling",
    "gamification",
    "life management",
    "RPG",
    "productivity",
    "quest system",
  ],
  openGraph: {
    title: "System Leveling — Gamified Life Management",
    description:
      "Biến cuộc sống thành RPG. Quản lý nhiệm vụ, kỹ năng, tài chính và học tập.",
    type: "website",
  },
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="vi" className="dark" data-theme="dark">
      <head>
        <link rel="preconnect" href="https://fonts.googleapis.com" />
        <link rel="preconnect" href="https://fonts.gstatic.com" crossOrigin="anonymous" />
        <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;600&family=Rajdhani:wght@600;700&display=swap" rel="stylesheet" />
        <link href="https://fonts.googleapis.com/css2?family=Material+Symbols+Outlined:wght,FILL@100..700,0..1&display=swap" rel="stylesheet" />
      </head>
      <body
        className="bg-[#0c0c1d] text-on-background min-h-[100dvh] flex flex-col font-body-base overflow-hidden md:items-center"
      >
        <div className="w-full md:max-w-[430px] h-[100dvh] bg-background system-grid-bg relative md:shadow-[0_0_50px_rgba(74,158,255,0.15)] md:border-x md:border-white/10 flex flex-col overflow-hidden">
          {children}
        </div>
      </body>
    </html>
  );
}
