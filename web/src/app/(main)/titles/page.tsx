"use client";

import { DEMO_USER } from "@/lib/types";

export default function TitlesPage() {
  const user = DEMO_USER;

  return (
    <div className="flex flex-col gap-container-gap relative z-10">
      {/* Background Atmospheric Void for Titles */}
      <div className="fixed inset-0 pointer-events-none z-[-1]" style={{
        backgroundImage: "radial-gradient(circle at 50% 0%, rgba(74, 158, 255, 0.15), transparent 60%), radial-gradient(circle at 80% 100%, rgba(255, 219, 60, 0.05), transparent 50%)"
      }}></div>

      {/* Screen Title */}
      <div className="flex items-center gap-3 mb-2">
        <span className="material-symbols-outlined text-primary text-3xl drop-shadow-[0_0_10px_rgba(164,121,255,0.8)]" style={{ fontVariationSettings: "'FILL' 1" }}>military_tech</span>
        <h2 className="font-headline-md-mobile md:font-headline-md text-headline-md-mobile md:text-headline-md text-on-surface">KHO DANH HIỆU</h2>
      </div>

      {/* Section: Danh Hiệu Đang Sử Dụng (Equipped Title) */}
      <section className="flex flex-col gap-4">
        <h3 className="font-label-caps text-label-caps text-on-surface-variant tracking-widest uppercase">Danh Hiệu Đang Sử Dụng</h3>
        
        {/* Hero Card: Mythic/Legendary */}
        <div className="shimmer-wrapper relative bg-surface-container/65 backdrop-blur-[40px] border border-secondary-fixed rounded-xl p-6 md:p-8 system-glow-gold flex flex-col md:flex-row items-center gap-6 overflow-hidden z-10 transition-transform duration-300 hover:scale-[1.01]">
          {/* Radiant Background Effect behind icon */}
          <div className="absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 w-64 h-64 bg-secondary-fixed/20 blur-[80px] rounded-full z-0 pointer-events-none"></div>
          
          {/* Icon/Badge */}
          <div className="relative z-10 w-24 h-24 rounded-full bg-surface-container-highest border border-secondary-fixed flex items-center justify-center shadow-[0_0_30px_rgba(255,219,60,0.3)] floating flex-shrink-0">
            <span className="material-symbols-outlined text-5xl text-secondary-fixed drop-shadow-[0_0_15px_rgba(255,219,60,0.8)]" style={{ fontVariationSettings: "'FILL' 1" }}>local_fire_department</span>
          </div>
          
          {/* Details */}
          <div className="relative z-10 flex flex-col text-center md:text-left gap-2 w-full">
            <div className="flex items-center justify-center md:justify-start gap-2 mb-1">
              <span className="px-2 py-0.5 bg-secondary-container text-on-secondary-container font-label-caps text-[10px] rounded-full uppercase font-bold shadow-[0_0_10px_rgba(255,219,60,0.5)]">
                Thần Thoại (Mythic)
              </span>
            </div>
            <h4 className="font-headline-md text-[28px] leading-tight text-secondary-fixed drop-shadow-[0_0_5px_rgba(255,219,60,0.5)] uppercase tracking-wide">
              Kẻ Nỗ Lực Không Ngừng
            </h4>
            <p className="font-body-base text-body-base text-on-surface-variant max-w-2xl mt-1">
              Một minh chứng cho ý chí sắt đá. Tăng toàn bộ chỉ số cơ bản lên 15% và giảm 10% sát thương nhận vào khi HP dưới 30%.
            </p>
            
            {/* Stats preview */}
            <div className="flex flex-wrap justify-center md:justify-start gap-3 mt-4">
              <div className="bg-white/5 border border-white/10 rounded-lg px-3 py-1.5 flex items-center gap-2">
                <span className="material-symbols-outlined text-tertiary text-sm" style={{ fontVariationSettings: "'FILL' 1" }}>arrow_upward</span>
                <span className="font-body-bold text-sm text-tertiary">All Stats +15%</span>
              </div>
              <div className="bg-white/5 border border-white/10 rounded-lg px-3 py-1.5 flex items-center gap-2">
                <span className="material-symbols-outlined text-primary text-sm" style={{ fontVariationSettings: "'FILL' 1" }}>shield</span>
                <span className="font-body-bold text-sm text-primary">Damage Reduction</span>
              </div>
            </div>
          </div>
        </div>
      </section>

      {/* Section: Bộ Sưu Tập (Collection Grid) */}
      <section className="flex flex-col gap-4 mt-8">
        <div className="flex justify-between items-end border-b border-white/10 pb-2">
          <h3 className="font-label-caps text-label-caps text-on-surface-variant tracking-widest uppercase">Bộ Sưu Tập</h3>
          <span className="font-body-bold text-sm text-primary-container">Đã mở khóa: 12/45</span>
        </div>
        
        {/* Bento Grid Layout for Collection */}
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-container-gap">
          {/* Card 1: Unlocked - Epic */}
          <div className="bg-surface-container/65 backdrop-blur-[40px] border border-primary-container/50 rounded-xl p-5 flex flex-col gap-3 relative overflow-hidden group hover:bg-surface-container-highest/80 transition-all duration-300">
            <div className="absolute inset-0 bg-gradient-to-br from-primary-container/0 to-primary-container/10 opacity-0 group-hover:opacity-100 transition-opacity duration-300"></div>
            <div className="flex justify-between items-start relative z-10">
              <div className="w-12 h-12 rounded-full bg-surface-dim border border-primary-container flex items-center justify-center">
                <span className="material-symbols-outlined text-primary-container" style={{ fontVariationSettings: "'FILL' 1" }}>swords</span>
              </div>
              <span className="font-label-caps text-[10px] text-primary-container border border-primary-container/30 px-2 py-0.5 rounded-sm uppercase tracking-wider">
                Sử Thi (Epic)
              </span>
            </div>
            <div className="relative z-10 mt-2">
              <h4 className="font-body-bold text-lg text-on-surface mb-1 group-hover:text-primary-container transition-colors">Thợ Săn Huyết Nguyệt</h4>
              <p className="font-body-base text-sm text-on-surface-variant line-clamp-2">
                Tiêu diệt 100 quái vật tinh anh trong sự kiện Huyết Nguyệt. Tăng 5% tỉ lệ bạo kích.
              </p>
            </div>
          </div>
          
          {/* Card 2: Unlocked - Rare */}
          <div className="bg-surface-container/65 backdrop-blur-[40px] border border-primary/30 rounded-xl p-5 flex flex-col gap-3 relative overflow-hidden group hover:bg-surface-container-highest/80 transition-all duration-300">
            <div className="flex justify-between items-start relative z-10">
              <div className="w-12 h-12 rounded-full bg-surface-dim border border-primary flex items-center justify-center">
                <span className="material-symbols-outlined text-primary" style={{ fontVariationSettings: "'FILL' 1" }}>bolt</span>
              </div>
              <span className="font-label-caps text-[10px] text-primary border border-primary/30 px-2 py-0.5 rounded-sm uppercase tracking-wider">
                Hiếm (Rare)
              </span>
            </div>
            <div className="relative z-10 mt-2">
              <h4 className="font-body-bold text-lg text-on-surface mb-1 group-hover:text-primary transition-colors">Tia Chớp Xanh</h4>
              <p className="font-body-base text-sm text-on-surface-variant line-clamp-2">
                Hoàn thành hầm ngục dưới 5 phút. Tăng 10 tốc độ di chuyển cơ bản.
              </p>
            </div>
          </div>
          
          {/* Card 3: Unlocked - Uncommon */}
          <div className="bg-surface-container/65 backdrop-blur-[40px] border border-tertiary/30 rounded-xl p-5 flex flex-col gap-3 relative overflow-hidden group hover:bg-surface-container-highest/80 transition-all duration-300">
            <div className="flex justify-between items-start relative z-10">
              <div className="w-12 h-12 rounded-full bg-surface-dim border border-tertiary flex items-center justify-center">
                <span className="material-symbols-outlined text-tertiary" style={{ fontVariationSettings: "'FILL' 1" }}>eco</span>
              </div>
              <span className="font-label-caps text-[10px] text-tertiary border border-tertiary/30 px-2 py-0.5 rounded-sm uppercase tracking-wider">
                Khá (Uncommon)
              </span>
            </div>
            <div className="relative z-10 mt-2">
              <h4 className="font-body-bold text-lg text-on-surface mb-1 group-hover:text-tertiary transition-colors">Người Sống Sót</h4>
              <p className="font-body-base text-sm text-on-surface-variant line-clamp-2">
                Sống sót qua đợt tấn công đầu tiên. Tăng nhẹ khả năng hồi phục ngoài giao tranh.
              </p>
            </div>
          </div>
          
          {/* Card 4: Locked */}
          <div className="bg-surface-container-low/40 backdrop-blur-[20px] border border-white/5 rounded-xl p-5 flex flex-col gap-3 relative overflow-hidden opacity-75 grayscale-[30%]">
            <div className="absolute top-4 right-4 z-20">
              <span className="material-symbols-outlined text-outline text-xl">lock</span>
            </div>
            <div className="flex justify-between items-start relative z-10">
              <div className="w-12 h-12 rounded-full bg-surface-dim border border-outline flex items-center justify-center opacity-50">
                <span className="material-symbols-outlined text-outline">skull</span>
              </div>
            </div>
            <div className="relative z-10 mt-2">
              <h4 className="font-body-bold text-lg text-outline-variant mb-1">Chúa Tể Vực Thẳm</h4>
              <p className="font-body-base text-sm text-outline line-clamp-2 mb-4">
                Đánh bại 50 Boss hạng S. Mở khóa kỹ năng đặc biệt.
              </p>
              <div className="w-full bg-surface-container-highest rounded-full h-1.5 mb-1">
                <div className="bg-outline h-1.5 rounded-full" style={{ width: "60%" }}></div>
              </div>
              <div className="flex justify-between font-label-caps text-[10px] text-outline">
                <span>Tiến độ</span>
                <span>30 / 50</span>
              </div>
            </div>
          </div>
          
          {/* Card 5: Locked */}
          <div className="bg-surface-container-low/40 backdrop-blur-[20px] border border-white/5 rounded-xl p-5 flex flex-col gap-3 relative overflow-hidden opacity-50 grayscale-[50%]">
            <div className="absolute top-4 right-4 z-20">
              <span className="material-symbols-outlined text-outline text-xl">lock</span>
            </div>
            <div className="flex justify-between items-start relative z-10">
              <div className="w-12 h-12 rounded-full bg-surface-dim border border-outline flex items-center justify-center opacity-30">
                <span className="material-symbols-outlined text-outline">question_mark</span>
              </div>
            </div>
            <div className="relative z-10 mt-2">
              <h4 className="font-body-bold text-lg text-outline-variant mb-1">???</h4>
              <p className="font-body-base text-sm text-outline line-clamp-2">
                Điều kiện chưa được khám phá. Hãy tiếp tục hành trình.
              </p>
            </div>
          </div>
        </div>
      </section>
    </div>
  );
}
