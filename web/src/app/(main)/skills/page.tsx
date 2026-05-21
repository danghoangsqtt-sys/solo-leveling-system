"use client";

import { DEMO_USER } from "@/lib/types";

export default function SkillsPage() {
  return (
    <div className="col-span-12 relative z-10 pt-16 md:pt-28">
      {/* Particle Background just for this page */}
      <div className="particle-bg"></div>

      {/* Equipped Skills Section */}
      <section className="mb-8 relative z-10">
        <h2 className="font-label-caps text-label-caps text-primary tracking-widest mb-4 flex items-center gap-2">
          <span className="w-2 h-2 rounded-full bg-primary animate-pulse"></span>
          KỸ NĂNG TRANG BỊ
        </h2>
        <div className="grid grid-cols-3 gap-3">
          {/* Slot 1 */}
          <div className="holographic-glass rounded-xl p-3 flex flex-col items-center justify-center aspect-square border-secondary-container/50 shadow-[0_0_20px_rgba(255,219,60,0.15)] shimmer-legendary relative cursor-pointer hover:scale-105 transition-transform">
            <div className="w-12 h-12 rounded-full bg-secondary-container/20 border-2 border-secondary-container flex items-center justify-center mb-2">
              <span className="material-symbols-outlined text-secondary-container" style={{ fontSize: "24px" }}>swords</span>
            </div>
            <span className="font-body-bold text-body-bold text-secondary-container text-xs text-center line-clamp-1">Trảm Phong</span>
          </div>
          
          {/* Slot 2 */}
          <div className="holographic-glass rounded-xl p-3 flex flex-col items-center justify-center aspect-square border-tertiary/50 shadow-[0_0_20px_rgba(64,225,126,0.15)] relative cursor-pointer hover:scale-105 transition-transform">
            <div className="w-12 h-12 rounded-full bg-tertiary/20 border-2 border-tertiary flex items-center justify-center mb-2">
              <span className="material-symbols-outlined text-tertiary" style={{ fontSize: "24px" }}>bolt</span>
            </div>
            <span className="font-body-bold text-body-bold text-tertiary text-xs text-center line-clamp-1">Lôi Phạt</span>
          </div>
          
          {/* Slot 3 (Empty) */}
          <div className="holographic-glass rounded-xl p-3 flex flex-col items-center justify-center aspect-square border-dashed border-white/20 opacity-60 cursor-pointer hover:opacity-100 transition-opacity">
            <div className="w-10 h-10 rounded-full border border-white/20 flex items-center justify-center mb-2">
              <span className="material-symbols-outlined text-on-surface-variant">add</span>
            </div>
            <span className="font-body-base text-body-base text-on-surface-variant text-xs">Trống</span>
          </div>
        </div>
      </section>

      {/* Skill Tabs */}
      <div className="flex overflow-x-auto gap-4 mb-6 pb-2 scrollbar-hide relative z-10" style={{ msOverflowStyle: 'none', scrollbarWidth: 'none' }}>
        <button className="px-6 py-2 rounded-full bg-primary-container text-on-primary-container font-label-caps text-label-caps whitespace-nowrap shadow-[0_0_15px_rgba(74,158,255,0.3)] transition-transform hover:scale-105">
          TẤT CẢ
        </button>
        <button className="px-6 py-2 rounded-full holographic-glass text-on-surface-variant font-label-caps text-label-caps whitespace-nowrap transition-transform hover:scale-105">
          SỨC MẠNH
        </button>
        <button className="px-6 py-2 rounded-full holographic-glass text-on-surface-variant font-label-caps text-label-caps whitespace-nowrap transition-transform hover:scale-105">
          TRÍ TUỆ
        </button>
        <button className="px-6 py-2 rounded-full holographic-glass text-on-surface-variant font-label-caps text-label-caps whitespace-nowrap transition-transform hover:scale-105">
          BỊ ĐỘNG
        </button>
      </div>

      {/* Skill Grid */}
      <div className="grid grid-cols-2 md:grid-cols-4 gap-4 relative z-10">
        {/* Skill Card 1 (Epic) */}
        <div className="holographic-glass rounded-xl p-4 holo-card-epic flex flex-col relative overflow-hidden group">
          <div className="absolute top-0 right-0 w-16 h-16 bg-tertiary/10 rounded-bl-full blur-xl"></div>
          <div className="flex justify-between items-start mb-3">
            <div className="w-10 h-10 rounded-lg bg-tertiary/20 border border-tertiary flex items-center justify-center floating">
              <span className="material-symbols-outlined text-tertiary">ac_unit</span>
            </div>
            <span className="font-label-caps text-label-caps bg-tertiary-container text-on-tertiary-container px-2 py-0.5 rounded text-[10px]">LV. 7</span>
          </div>
          <h3 className="font-headline-md-mobile text-headline-md-mobile text-on-surface mb-1">Băng Giá</h3>
          <p className="font-body-base text-body-base text-on-surface-variant text-xs mb-3 line-clamp-2">Làm chậm mục tiêu và gây sát thương diện rộng.</p>
          <div className="mt-auto">
            <div className="flex justify-between text-[10px] text-tertiary mb-1 font-body-bold">
              <span>SP</span>
              <span>85%</span>
            </div>
            <div className="h-1.5 w-full bg-surface-container-highest rounded-full overflow-hidden">
              <div className="h-full bg-tertiary shadow-[0_0_10px_rgba(64,225,126,0.8)]" style={{ width: "85%" }}></div>
            </div>
          </div>
        </div>

        {/* Skill Card 2 (Rare) */}
        <div className="holographic-glass rounded-xl p-4 holo-card-rare flex flex-col relative overflow-hidden group">
          <div className="absolute top-0 right-0 w-16 h-16 bg-primary/10 rounded-bl-full blur-xl"></div>
          <div className="flex justify-between items-start mb-3">
            <div className="w-10 h-10 rounded-lg bg-primary/20 border border-primary flex items-center justify-center">
              <span className="material-symbols-outlined text-primary">directions_run</span>
            </div>
            <span className="font-label-caps text-label-caps bg-primary-container text-on-primary-container px-2 py-0.5 rounded text-[10px]">LV. 4</span>
          </div>
          <h3 className="font-headline-md-mobile text-headline-md-mobile text-on-surface mb-1">Tốc Biến</h3>
          <p className="font-body-base text-body-base text-on-surface-variant text-xs mb-3 line-clamp-2">Di chuyển tức thời một khoảng ngắn. Tránh né đòn đánh.</p>
          <div className="mt-auto">
            <div className="flex justify-between text-[10px] text-primary mb-1 font-body-bold">
              <span>SP</span>
              <span>40%</span>
            </div>
            <div className="h-1.5 w-full bg-surface-container-highest rounded-full overflow-hidden">
              <div className="h-full bg-primary shadow-[0_0_10px_rgba(164,201,255,0.8)]" style={{ width: "40%" }}></div>
            </div>
          </div>
        </div>

        {/* Skill Card 3 (Common) */}
        <div className="holographic-glass rounded-xl p-4 flex flex-col relative overflow-hidden group">
          <div className="flex justify-between items-start mb-3">
            <div className="w-10 h-10 rounded-lg bg-white/5 border border-white/20 flex items-center justify-center">
              <span className="material-symbols-outlined text-on-surface-variant">fitness_center</span>
            </div>
            <span className="font-label-caps text-label-caps bg-surface-variant text-on-surface-variant px-2 py-0.5 rounded text-[10px]">LV. MAX</span>
          </div>
          <h3 className="font-headline-md-mobile text-headline-md-mobile text-on-surface mb-1">Cường Lực</h3>
          <p className="font-body-base text-body-base text-on-surface-variant text-xs mb-3 line-clamp-2">Tăng vĩnh viễn 10 điểm sức mạnh vật lý cơ bản.</p>
          <div className="mt-auto">
            <div className="flex justify-between text-[10px] text-on-surface-variant mb-1 font-body-bold">
              <span>MAX LEVEL</span>
              <span>--</span>
            </div>
            <div className="h-1.5 w-full bg-surface-container-highest rounded-full overflow-hidden">
              <div className="h-full bg-on-surface-variant w-full"></div>
            </div>
          </div>
        </div>

        {/* Skill Card 4 (Legendary - Locked) */}
        <div className="holographic-glass rounded-xl p-4 flex flex-col relative overflow-hidden opacity-50 grayscale border-dashed border-white/20">
          <div className="absolute inset-0 flex items-center justify-center z-10">
            <span className="material-symbols-outlined text-white text-3xl drop-shadow-lg">lock</span>
          </div>
          <div className="flex justify-between items-start mb-3 blur-sm">
            <div className="w-10 h-10 rounded-lg bg-secondary-container/20 border border-secondary-container flex items-center justify-center">
              <span className="material-symbols-outlined text-secondary-container">visibility</span>
            </div>
            <span className="font-label-caps text-label-caps bg-surface-variant text-on-surface-variant px-2 py-0.5 rounded text-[10px]">LV. ?</span>
          </div>
          <h3 className="font-headline-md-mobile text-headline-md-mobile text-on-surface mb-1 blur-sm">Thẩm Định Mắt</h3>
          <p className="font-body-base text-body-base text-on-surface-variant text-xs mb-3 line-clamp-2 blur-sm">Yêu cầu cấp bậc S để mở khóa kỹ năng này.</p>
          <div className="mt-auto blur-sm">
            <div className="h-1.5 w-full bg-surface-container-highest rounded-full overflow-hidden">
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
