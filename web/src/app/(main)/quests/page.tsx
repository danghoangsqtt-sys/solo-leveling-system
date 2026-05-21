"use client";

import { DEMO_USER } from "@/lib/types";

export default function QuestsPage() {
  const user = DEMO_USER;

  // Giả lập danh sách nhiệm vụ từ giao diện cũ, bọc vào thiết kế mới
  const quests = [
    {
      id: "1",
      title: "Morning Hydration",
      time: "06:00",
      rank: "E",
      status: "completed",
      rewards: [],
      rankColor: "text-white",
      bgRank: "bg-white/10",
      borderRank: "border-white/20",
      icon: "check",
      iconColor: "text-white",
      glowClass: "glow-white"
    },
    {
      id: "2",
      title: "Morning Warrior Training",
      time: "06:30-07:15",
      rank: "C",
      status: "in_progress",
      rewards: [
        { text: "+150 EXP", color: "text-tertiary" },
        { text: "+10 SP (Cardio)", color: "text-secondary-fixed-dim" }
      ],
      rankColor: "text-primary",
      bgRank: "bg-primary/10",
      borderRank: "border-primary/30",
      icon: "autorenew",
      iconColor: "text-primary",
      glowClass: "glow-blue"
    },
    {
      id: "3",
      title: "Deep Focus: IELTS Reading",
      time: "08:00",
      rank: "B",
      status: "pending",
      rewards: [
        { text: "+250 EXP", color: "text-tertiary" },
        { text: "+15 SP (Reading)", color: "text-secondary-fixed-dim" }
      ],
      rankColor: "text-[#c864ff]",
      bgRank: "bg-[#c864ff]/10",
      borderRank: "border-[#c864ff]/30",
      icon: "menu_book",
      iconColor: "text-[#c864ff]",
      glowClass: "glow-purple"
    },
    {
      id: "4",
      title: "Code Practice: MQTT Protocol",
      time: "10:00",
      rank: "C",
      status: "pending",
      rewards: [],
      rankColor: "text-primary",
      bgRank: "bg-primary/10",
      borderRank: "border-primary/30",
      icon: "code",
      iconColor: "text-primary",
      glowClass: "glow-blue"
    },
    {
      id: "5",
      title: "Vocabulary Builder",
      time: "12:00",
      rank: "D",
      status: "pending",
      rewards: [],
      rankColor: "text-tertiary",
      bgRank: "bg-tertiary/10",
      borderRank: "border-tertiary/30",
      icon: "translate",
      iconColor: "text-tertiary",
      glowClass: "glow-green"
    }
  ];

  return (
    <div className="col-span-12 relative z-10 pt-16 md:pt-28">
      {/* Dashboard Header */}
      <section className="mb-container-gap relative">
        <div className="bg-surface/65 backdrop-blur-[40px] border border-white/12 rounded-xl p-6 glow-blue relative overflow-hidden">
          {/* Shimmer Effect */}
          <div className="absolute inset-0 bg-gradient-to-r from-transparent via-white/5 to-transparent -translate-x-full animate-[shimmer_4s_infinite]"></div>
          
          <div className="flex flex-col md:flex-row justify-between items-start md:items-end gap-4 relative z-10">
            <div>
              <h2 className="font-display-lg text-display-lg text-primary tracking-widest uppercase shadow-sm">DAILY QUESTS</h2>
              <div className="flex items-center gap-3 mt-2">
                <p className="font-body-base text-body-base text-on-surface-variant">Tuesday, May 20, 2026</p>
                <span className="bg-secondary-container text-on-secondary-container font-label-caps text-label-caps px-2 py-1 rounded-sm flex items-center gap-1 border border-secondary-fixed">
                  <span className="material-symbols-outlined text-[14px]">local_fire_department</span> Streak: 12
                </span>
              </div>
            </div>
            
            <div className="w-full md:w-64">
              <div className="flex justify-between text-label-caps font-label-caps text-on-surface mb-2">
                <span>Quest Progress</span>
                <span className="text-primary">3/8 (37.5%)</span>
              </div>
              <div className="h-2 w-full bg-surface-container-high rounded-full overflow-hidden border border-white/5">
                <div className="h-full bg-primary pulse-energy rounded-full" style={{ width: "37.5%" }}></div>
              </div>
            </div>
          </div>
        </div>
      </section>

      {/* Quest Timeline */}
      <section className="relative">
        {/* Vertical Timeline Line */}
        <div className="absolute left-6 top-4 bottom-0 w-px bg-white/10 md:left-8"></div>
        
        <div className="flex flex-col gap-6 relative">
          {quests.map((quest) => (
            <div key={quest.id} className="flex gap-4 relative group">
              {/* Timeline Icon */}
              <div className="w-12 h-12 flex-shrink-0 flex items-center justify-center relative z-10 mt-1">
                <div className={`w-8 h-8 rounded-full bg-surface border ${quest.status === 'completed' ? 'border-white/30 glow-white' : `border-${quest.rankColor.split('-')[1]} ${quest.glowClass}`} flex items-center justify-center transition-transform group-hover:scale-110`}>
                  <span className={`material-symbols-outlined ${quest.iconColor} text-[16px] ${quest.status === 'in_progress' ? 'animate-spin' : ''}`} style={quest.status === 'completed' ? { fontVariationSettings: "'FILL' 1" } : {}}>
                    {quest.icon}
                  </span>
                </div>
              </div>

              {/* Quest Card */}
              <div className={`flex-1 bg-surface-container-low/80 backdrop-blur-[40px] border ${quest.status === 'completed' ? 'border-white/30 glow-white opacity-70' : quest.status === 'in_progress' ? 'border-primary glow-blue' : `border-${quest.rankColor.split('-')[1]}/30 hover:border-${quest.rankColor.split('-')[1]} hover:${quest.glowClass}`} rounded-lg p-4 transition-all hover:bg-surface-container/90`}>
                <div className="flex justify-between items-start mb-2">
                  <div className="flex items-center gap-2">
                    <span className={`${quest.bgRank} ${quest.rankColor} font-label-caps text-[10px] px-2 py-0.5 rounded border ${quest.borderRank}`}>
                      [RANK {quest.rank}]
                    </span>
                    <h3 className={`font-body-bold text-body-bold ${quest.status === 'completed' ? 'text-on-surface line-through' : quest.status === 'in_progress' ? 'text-primary' : 'text-on-surface'}`}>
                      {quest.title}
                    </h3>
                  </div>
                  <span className={`font-label-caps text-label-caps ${quest.status === 'in_progress' ? 'text-primary' : 'text-on-surface-variant'} flex items-center gap-1`}>
                    <span className="material-symbols-outlined text-[14px]">schedule</span> {quest.time}
                  </span>
                </div>
                
                <div className="flex flex-col gap-3">
                  <div className="flex items-center gap-2">
                    {quest.status === 'completed' && (
                      <span className="font-label-caps text-[10px] text-tertiary-fixed-dim bg-tertiary-fixed-dim/10 px-2 py-0.5 rounded-sm border border-tertiary-fixed-dim/30">COMPLETED</span>
                    )}
                    {quest.status === 'in_progress' && (
                      <span className="font-label-caps text-[10px] text-secondary-fixed-dim bg-secondary-fixed-dim/10 px-2 py-0.5 rounded-sm border border-secondary-fixed-dim/30 animate-pulse">IN PROGRESS</span>
                    )}
                    {quest.status === 'pending' && (
                      <span className="font-label-caps text-[10px] text-on-surface-variant bg-surface-variant/50 px-2 py-0.5 rounded-sm border border-white/10">PENDING</span>
                    )}
                  </div>
                  
                  {quest.rewards.length > 0 && (
                    <div className="bg-surface/50 border border-white/5 rounded p-3 flex gap-4">
                      <div>
                        <span className="block font-label-caps text-[10px] text-on-surface-variant mb-1">REWARDS</span>
                        <div className="flex gap-2">
                          {quest.rewards.map((r, idx) => (
                            <span key={idx} className={`${r.color} text-[12px] font-body-bold`}>{r.text}</span>
                          ))}
                        </div>
                      </div>
                    </div>
                  )}
                </div>
              </div>
            </div>
          ))}
        </div>
      </section>
    </div>
  );
}
