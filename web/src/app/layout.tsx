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
      <body
        className="bg-background text-on-background system-grid-bg min-h-screen pb-24 md:pb-8 flex flex-col font-body-base overflow-x-hidden"
      >
        {children}
      </body>
    </html>
  );
}
