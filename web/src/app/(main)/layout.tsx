"use client";

import { usePathname } from "next/navigation";
import Link from "next/link";
import { useState, useEffect } from "react";
import { logger } from "@/lib/logger";
import { NAV_ITEMS } from "@/lib/types";
import { AuraChatPanel } from "@/components/aura";
import "./main-layout.css";

const BOTTOM_NAV_ITEMS = NAV_ITEMS.slice(0, 5); // Home, Quests, Skills, Finance, Library
const SIDEBAR_ITEMS = NAV_ITEMS;

export default function MainLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  const pathname = usePathname();
  const [isAuraOpen, setIsAuraOpen] = useState(false);

  useEffect(() => {
    logger.info("Navigation", `User navigated to: ${pathname}`);
  }, [pathname]);

  return (
    <>
      {/* Ambient Glow Effects */}
      <div className="absolute top-0 left-0 w-full h-full pointer-events-none overflow-hidden z-0">
        <div className="absolute top-[-10%] left-[-10%] w-[50%] h-[50%] bg-primary/10 rounded-full blur-[100px]"></div>
        <div className="absolute bottom-[20%] right-[-10%] w-[40%] h-[40%] bg-tertiary/5 rounded-full blur-[100px]"></div>
      </div>

      {/* TopAppBar */}
      <header className="absolute top-0 w-full z-50 bg-surface/60 backdrop-blur-3xl border-b border-white/10 shadow-[0_4px_30px_rgba(0,0,0,0.5)] flex justify-between items-center px-4 py-4">
        <div className="flex items-center gap-3">
          <div className="w-10 h-10 rounded-full bg-surface-container-high border border-white/20 overflow-hidden flex items-center justify-center">
            <span className="material-symbols-outlined text-on-surface-variant">person</span>
          </div>
          <h1 className="font-headline-md text-[20px] tracking-widest text-primary drop-shadow-[0_0_8px_rgba(164,121,255,0.8)] uppercase">SYSTEM LEVELING</h1>
        </div>
        <div className="flex items-center">
          <span className="font-body-bold text-body-bold text-secondary-fixed">Lvl. 42</span>
        </div>
      </header>

      {/* Main Canvas */}
      <main className="flex-1 w-full px-4 pt-20 pb-28 flex flex-col gap-6 overflow-y-auto" style={{ scrollbarWidth: 'none', msOverflowStyle: 'none' }}>
        {children}
      </main>

      {/* Aura Global Panel (Keep existing modal logic) */}
      <aside className={`aura-panel ${isAuraOpen ? "aura-panel--open" : ""}`}>
        <AuraChatPanel isOpen={isAuraOpen} onClose={() => setIsAuraOpen(false)} />
      </aside>
      
      {/* Aura FAB (Floating Action Button) */}
      <button 
        onClick={() => setIsAuraOpen(true)}
        className="absolute bottom-24 right-4 w-14 h-14 bg-primary-container text-on-primary-container rounded-full shadow-[0_0_20px_rgba(74,158,255,0.4)] flex items-center justify-center z-40 hover:scale-110 active:scale-95 transition-all"
      >
        <span className="material-symbols-outlined text-3xl">smart_toy</span>
      </button>

      {/* BottomNavBar (Mobile) */}
      <nav className="absolute bottom-6 left-1/2 -translate-x-1/2 w-[90%] rounded-full border border-white/12 shadow-[0_8px_32px_0_rgba(0,0,0,0.8)] bg-surface-container/65 backdrop-blur-[40px] flex justify-around items-center px-4 py-2 z-50">
        {BOTTOM_NAV_ITEMS.map((item) => {
          const isActive =
            pathname === item.path ||
            (item.path !== "/" && pathname.startsWith(item.path));
            
          if (isActive) {
            return (
              <Link key={item.path} href={item.path} className="flex items-center justify-center bg-primary-container text-on-primary-container rounded-full w-12 h-12 shadow-[0_0_15px_rgba(74,158,255,0.6)] scale-110 active:scale-95 duration-200">
                <span className="material-symbols-outlined" style={{ fontVariationSettings: "'FILL' 1" }}>{getMaterialIcon(item.path)}</span>
              </Link>
            );
          }
          return (
            <Link key={item.path} href={item.path} className="flex items-center justify-center text-on-surface-variant w-12 h-12 hover:text-primary transition-transform hover:brightness-125 transition-all active:scale-90 duration-150">
              <span className="material-symbols-outlined">{getMaterialIcon(item.path)}</span>
            </Link>
          );
        })}
      </nav>
    </>
  );
}

function getMaterialIcon(path: string): string {
  switch (path) {
    case "/": return "home";
    case "/quests": return "swords";
    case "/skills": return "bolt";
    case "/finance": return "payments";
    case "/library": return "menu_book";
    case "/inventory": return "backpack";
    case "/titles": return "military_tech";
    default: return "more_horiz";
  }
}
