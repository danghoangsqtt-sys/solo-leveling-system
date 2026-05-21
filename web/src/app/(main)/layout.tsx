"use client";

import { useState, useEffect } from "react";
import { usePathname } from "next/navigation";
import Link from "next/link";
import { NAV_ITEMS } from "@/lib/types";
import { AuraChatPanel } from "@/components/aura";
import { logger } from "@/lib/logger";
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
    <div className="app-shell">
      {/* ── Desktop Sidebar ── */}
      <aside className="sidebar hide-mobile">
        <div className="sidebar__logo">
          <span className="sidebar__logo-icon">⚔️</span>
          <span>System Leveling</span>
        </div>

        {/* Mini character preview */}
        <div className="sidebar__character">
          <div className="sidebar__character-avatar">
            <div className="sidebar__character-glow" />
            <span className="sidebar__character-emoji">⚗️</span>
          </div>
          <div className="sidebar__character-info">
            <span className="sidebar__character-name">Hunter</span>
            <span className="sidebar__character-level">Lv. 1 • Alchemist</span>
          </div>
        </div>

        <nav className="sidebar__nav">
          {SIDEBAR_ITEMS.map((item) => {
            const isActive =
              pathname === item.path ||
              (item.path !== "/" && pathname.startsWith(item.path));
            return (
              <Link
                key={item.path}
                href={item.path}
                className={`sidebar__link ${
                  isActive ? "sidebar__link--active" : ""
                }`}
              >
                <span className="sidebar__link-icon">{item.icon}</span>
                <span>{item.label}</span>
                {item.badge && item.badge > 0 && (
                  <span className="sidebar__badge">{item.badge}</span>
                )}
              </Link>
            );
          })}
        </nav>

        {/* Aura NPC mini button */}
        <button 
          className="sidebar__aura-btn" 
          onClick={() => setIsAuraOpen(true)}
          aria-label="Talk to Aura"
        >
          <span className="sidebar__aura-icon">🌟</span>
          <span>Talk to Aura</span>
        </button>
      </aside>

      {/* ── Main Content ── */}
      <main className={`main-content ${isAuraOpen ? "main-content--aura-open" : ""}`}>
        <div className="page-container">{children}</div>
      </main>

      {/* ── Aura Global Panel ── */}
      <aside className={`aura-panel ${isAuraOpen ? "aura-panel--open" : ""}`}>
        <AuraChatPanel isOpen={isAuraOpen} onClose={() => setIsAuraOpen(false)} />
      </aside>

      {/* ── Mobile Bottom Navigation ── */}
      <nav className="bottom-nav hide-desktop">
        {BOTTOM_NAV_ITEMS.map((item) => {
          const isActive =
            pathname === item.path ||
            (item.path !== "/" && pathname.startsWith(item.path));
          return (
            <Link
              key={item.path}
              href={item.path}
              className={`bottom-nav__item ${
                isActive ? "bottom-nav__item--active" : ""
              }`}
            >
              <span className="bottom-nav__icon">{item.icon}</span>
              <span>{item.label}</span>
            </Link>
          );
        })}
      </nav>
    </div>
  );
}
