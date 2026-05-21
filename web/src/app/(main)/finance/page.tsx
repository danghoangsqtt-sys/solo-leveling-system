"use client";

import { DEMO_USER } from "@/lib/types";

export default function FinancePage() {
  const user = DEMO_USER;

  return (
    <div className="flex flex-col gap-6 relative z-10">
      {/* Background Atmospheric Void for this specific page */}
      <div className="fixed inset-0 pointer-events-none z-[-1] bg-cosmic-void"></div>

      {/* Hero Card: Net Cashflow */}
      <section className="glass-panel rare-border rounded-xl p-6 relative overflow-hidden flex flex-col gap-4">
        {/* Shimmer Effect wrapper inside card */}
        <div className="absolute inset-0 bg-gradient-to-r from-transparent via-white/5 to-transparent -translate-x-full animate-[shimmer_4s_infinite]"></div>
        
        <div className="absolute inset-0 bg-gradient-to-br from-primary-container/20 to-surface-variant/5 opacity-50"></div>
        
        <div className="relative z-10 flex flex-col items-center text-center">
          <span className="font-label-caps text-label-caps text-on-surface-variant uppercase mb-2">Số Dư Hiện Tại</span>
          <div className="font-display-lg text-display-lg text-secondary-container drop-shadow-[0_0_15px_rgba(255,219,60,0.4)]">
            2,350,000 <span className="text-xl">G</span>
          </div>
        </div>
        
        <div className="relative z-10 flex justify-between gap-4 mt-2">
          <div className="flex-1 glass-panel rounded-lg p-3 flex flex-col items-center border-tertiary-fixed/30 border shadow-[0_0_10px_rgba(99,255,151,0.1)]">
            <span className="font-label-caps text-label-caps text-on-surface-variant mb-1">Thu Nhập</span>
            <span className="font-body-bold text-body-bold text-tertiary-fixed">+ 5,000K</span>
          </div>
          <div className="flex-1 glass-panel rounded-lg p-3 flex flex-col items-center border-error/30 border shadow-[0_0_10px_rgba(255,180,171,0.1)]">
            <span className="font-label-caps text-label-caps text-on-surface-variant mb-1">Chi Tiêu</span>
            <span className="font-body-bold text-body-bold text-error">- 2,650K</span>
          </div>
        </div>
      </section>

      {/* Quick Stats */}
      <section className="grid grid-cols-3 gap-3">
        <div className="glass-panel rounded-lg p-3 flex flex-col items-center justify-center text-center">
          <span className="font-label-caps text-label-caps text-on-surface-variant mb-1">Tiết Kiệm</span>
          <span className="font-headline-md-mobile text-headline-md-mobile text-primary">32%</span>
        </div>
        <div className="glass-panel rounded-lg p-3 flex flex-col items-center justify-center text-center">
          <span className="font-label-caps text-label-caps text-on-surface-variant mb-1">Chi TB</span>
          <span className="font-body-bold text-body-bold text-primary">150k/d</span>
        </div>
        <div className="glass-panel rounded-lg p-3 flex flex-col items-center justify-center text-center">
          <span className="font-label-caps text-label-caps text-on-surface-variant mb-1">Top Chi</span>
          <span className="font-body-bold text-body-bold text-error">Ăn uống</span>
        </div>
      </section>

      {/* Savings Goals */}
      <section className="flex flex-col gap-4">
        <h2 className="font-headline-md-mobile text-headline-md-mobile text-primary flex items-center gap-2">
          <span className="material-symbols-outlined text-secondary-fixed">target</span> Mục Tiêu Tích Lũy
        </h2>
        
        <div className="glass-panel rounded-lg p-4 flex flex-col gap-3">
          <div className="flex justify-between items-center">
            <div className="flex items-center gap-3">
              <div className="w-10 h-10 rounded-full bg-surface-variant flex items-center justify-center border border-white/12">
                <span className="material-symbols-outlined text-primary">laptop_mac</span>
              </div>
              <div>
                <div className="font-body-bold text-body-bold">Mua Laptop</div>
                <div className="font-label-caps text-label-caps text-on-surface-variant">15M / 20M G</div>
              </div>
            </div>
            <span className="font-body-bold text-body-bold text-primary">75%</span>
          </div>
          <div className="h-2 bg-surface-variant rounded-full overflow-hidden">
            <div className="h-full progress-bar-fill rounded-full" style={{ width: "75%" }}></div>
          </div>
        </div>
        
        <div className="glass-panel rounded-lg p-4 flex flex-col gap-3">
          <div className="flex justify-between items-center">
            <div className="flex items-center gap-3">
              <div className="w-10 h-10 rounded-full bg-surface-variant flex items-center justify-center border border-white/12">
                <span className="material-symbols-outlined text-primary">flight_takeoff</span>
              </div>
              <div>
                <div className="font-body-bold text-body-bold">Quỹ Du Lịch</div>
                <div className="font-label-caps text-label-caps text-on-surface-variant">2M / 10M G</div>
              </div>
            </div>
            <span className="font-body-bold text-body-bold text-primary">20%</span>
          </div>
          <div className="h-2 bg-surface-variant rounded-full overflow-hidden">
            <div className="h-full progress-bar-fill rounded-full" style={{ width: "20%" }}></div>
          </div>
        </div>
      </section>
    </div>
  );
}
