"use client";

const DEMO_USER = {
  name: "Vua Bóng Tối",
  jobClass: "Giả Kim Thuật Sư",
  experience: 8450,
  maxExperience: 10000,
  gold: 2350,
  crystals: 15,
  stats: {
    str: 142,
    int: 205,
    agi: 98,
    vit: 160,
    wis: 185,
    cha: 65
  }
};

export default function HomePage() {
  const user = DEMO_USER;

  return (
    <>
      {/* Left Pane: Character & Core Info (Spans 5 cols on desktop) */}
      <section className="md:col-span-5 flex flex-col gap-container-gap">
        {/* Player Nameplate (Glass Card) */}
        <div className="glass-panel-rare rounded-xl p-6 relative overflow-hidden flex flex-col items-center text-center">
          <div className="absolute top-0 w-full h-1 bg-gradient-to-r from-transparent via-secondary-container to-transparent opacity-50"></div>
          <span className="font-label-caps text-label-caps text-secondary-fixed px-3 py-1 rounded-full bg-secondary-container/10 border border-secondary-container/30 mb-2 shimmer-wrapper">
            <span className="material-symbols-outlined text-[14px] align-middle mr-1" style={{ fontVariationSettings: "'FILL' 1" }}>stars</span>Chiến Binh Nhàn Hạ
          </span>
          <h2 className="font-display-lg text-display-lg text-on-surface tracking-tight leading-tight mb-1">{user?.name || "Vua Bóng Tối"}</h2>
          <p className="font-body-base text-body-base text-primary">Class: {user?.jobClass || "Giả Kim Thuật Sư"}</p>
        </div>

        {/* Central NPC Display */}
        <div className="relative w-full aspect-square flex items-center justify-center">
          {/* Orbital decorations */}
          <div className="absolute w-[120%] h-[120%] border border-primary/20 rounded-full animate-[spin_20s_linear_infinite] border-dashed"></div>
          <div className="absolute w-[90%] h-[90%] border border-secondary-fixed/10 rounded-full animate-[spin_15s_linear_infinite_reverse]"></div>
          {/* Glowing Base */}
          <div className="absolute bottom-10 w-48 h-12 bg-primary/20 blur-2xl rounded-full"></div>
          {/* NPC Image */}
          <img 
            alt="Aura NPC Guide floating in AR interface" 
            className="w-[80%] h-auto object-contain z-10 animate-float drop-shadow-[0_20px_30px_rgba(0,0,0,0.8)]" 
            src="https://lh3.googleusercontent.com/aida-public/AB6AXuChmP0ou5ba51c6SQUPAKv2TAAnBTmFQjJdb2ttEQtcGrL34sufFHFYNCQWeBYfj5AQK0HN0QyVWpb_Yqe0sC45RVLjEWimBB-MAY3QOvzrzZbk6Ajp7_BSTBAkJPY8h6fMXTE8LVNKRDR_EgvJgFVIV_gvjqjcLz2EPTrIPWorxf41J9-o26hUomkUAOe1acEL9zBRXSaTagixM6Ytn_GmAiIwFzD3bVBKTl5LDP9r8wdL_WCGvZKZ_1htWhDFukzCc4QI6XVgPQ"
          />
        </div>

        {/* EXP & Currency Block */}
        <div className="glass-panel rounded-xl p-5 flex flex-col gap-4">
          {/* EXP Bar */}
          <div>
            <div className="flex justify-between items-end mb-2">
              <span className="font-label-caps text-label-caps text-on-surface-variant">KINH NGHIỆM</span>
              <span className="font-label-caps text-label-caps text-secondary-fixed">{user?.experience.toLocaleString()} / {user?.maxExperience.toLocaleString()}</span>
            </div>
            <div className="w-full h-2 bg-surface-container-highest rounded-full overflow-hidden shimmer-wrapper">
              <div className="h-full exp-bar-fill rounded-full" style={{ width: `${(user ? (user.experience / user.maxExperience) * 100 : 84.5)}%` }}></div>
            </div>
          </div>
          {/* Currency */}
          <div className="flex gap-4 pt-2 border-t border-white/5">
            <div className="flex-1 flex items-center gap-2 bg-surface-container-low p-2 rounded-lg border border-white/5">
              <span className="material-symbols-outlined text-secondary-fixed" style={{ fontVariationSettings: "'FILL' 1" }}>monetization_on</span>
              <div className="flex flex-col">
                <span className="font-label-caps text-label-caps text-on-surface-variant text-[10px]">VÀNG</span>
                <span className="font-body-bold text-body-bold text-on-surface leading-none">{user?.gold.toLocaleString() || "2,350"}</span>
              </div>
            </div>
            <div className="flex-1 flex items-center gap-2 bg-surface-container-low p-2 rounded-lg border border-white/5">
              <span className="material-symbols-outlined text-tertiary-fixed" style={{ fontVariationSettings: "'FILL' 1" }}>diamond</span>
              <div className="flex flex-col">
                <span className="font-label-caps text-label-caps text-on-surface-variant text-[10px]">NGỌC</span>
                <span className="font-body-bold text-body-bold text-on-surface leading-none">{user?.crystals.toLocaleString() || "15"}</span>
              </div>
            </div>
          </div>
        </div>
      </section>

      {/* Right Pane: Attributes & Quests (Spans 7 cols on desktop) */}
      <section className="md:col-span-7 flex flex-col gap-container-gap">
        {/* Attributes Panel */}
        <div className="glass-panel rounded-xl p-6 flex flex-col gap-6">
          <div className="flex items-center gap-2 mb-2 border-b border-white/10 pb-4">
            <span className="material-symbols-outlined text-primary">bar_chart</span>
            <h3 className="font-headline-md-mobile text-headline-md-mobile text-on-surface uppercase tracking-widest">Chỉ Số Cơ Bản</h3>
          </div>
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-x-6 gap-y-5">
            
            <StatRow icon="fitness_center" label="SỨC MẠNH (STR)" value={user?.stats.str || 142} bonus="+12" fillPercent="70%" />
            <StatRow icon="psychology" label="TRÍ LỰC (INT)" value={user?.stats.int || 205} bonus="+45" fillPercent="90%" />
            <StatRow icon="speed" label="NHANH NHẸN (AGI)" value={user?.stats.agi || 98} bonus="+5" fillPercent="45%" />
            <StatRow icon="favorite" label="THỂ LỰC (VIT)" value={user?.stats.vit || 160} bonus="+20" fillPercent="75%" />
            <StatRow icon="menu_book" label="THÔNG THÁI (WIS)" value={user?.stats.wis || 185} bonus="+30" fillPercent="85%" />
            <StatRow icon="auto_awesome" label="MỊ LỰC (CHA)" value={user?.stats.cha || 65} bonus="+0" fillPercent="30%" />
            
          </div>
        </div>

        {/* Today's Quest Mini-Card */}
        <div className="glass-panel rounded-xl p-5 border-l-4 border-l-secondary-fixed relative overflow-hidden">
          {/* Background ambient glow */}
          <div className="absolute right-0 top-0 w-32 h-32 bg-secondary-fixed/5 blur-2xl rounded-full"></div>
          <div className="flex justify-between items-start mb-4 relative z-10">
            <div>
              <span className="font-label-caps text-label-caps text-secondary-fixed">NHIỆM VỤ HÀNG NGÀY</span>
              <h4 className="font-body-bold text-body-bold text-on-surface">Thu thập tinh thể Mana</h4>
            </div>
            <span className="material-symbols-outlined text-secondary-fixed bg-secondary-fixed/10 p-2 rounded-lg">assignment_turned_in</span>
          </div>
          <div className="relative z-10">
            <div className="flex justify-between items-center mb-2">
              <span className="font-body-base text-body-base text-on-surface-variant text-sm">Tiến độ</span>
              <span className="font-body-bold text-body-bold text-on-surface text-sm">8 / 10</span>
            </div>
            <div className="w-full h-2 bg-surface-container-highest rounded-full overflow-hidden">
              <div className="h-full bg-secondary-fixed rounded-full" style={{ width: "80%" }}></div>
            </div>
          </div>
        </div>
      </section>
    </>
  );
}

function StatRow({ icon, label, value, bonus, fillPercent }: { icon: string, label: string, value: number, bonus: string, fillPercent: string }) {
  return (
    <div className="flex flex-col gap-1.5 group cursor-pointer">
      <div className="flex justify-between items-center">
        <span className="font-label-caps text-label-caps text-on-surface-variant flex items-center gap-1 group-hover:text-primary transition-colors">
          <span className="material-symbols-outlined text-[14px]">{icon}</span> {label}
        </span>
        <div className="flex items-baseline gap-1">
          <span className="font-body-bold text-body-bold text-on-surface">{value}</span>
          <span className="font-label-caps text-label-caps text-tertiary-fixed">{bonus}</span>
        </div>
      </div>
      <div className="w-full h-1.5 bg-surface-container-highest rounded-full overflow-hidden">
        <div className="h-full stat-bar-fill rounded-full" style={{ width: fillPercent }}></div>
      </div>
    </div>
  );
}
