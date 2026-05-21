"use client";

import { DEMO_USER } from "@/lib/types";
import { useState } from "react";

export default function InventoryPage() {
  const user = DEMO_USER;
  const [selectedItem, setSelectedItem] = useState("mythic_dagger");

  return (
    <div className="col-span-12 flex flex-col gap-container-gap md:grid md:grid-cols-12 md:items-start relative z-10 pt-16 md:pt-28">
      {/* Left/Top Pane: Character Mini-View & Categories */}
      <div className="md:col-span-4 flex flex-col gap-4">
        {/* Character Mini-View */}
        <div className="bg-surface-container/65 backdrop-blur-[40px] rounded-xl border border-white/12 p-4 flex items-center gap-4 shadow-[0_8px_32px_0_rgba(0,0,0,0.4)]">
          <div className="relative">
            <div className="w-16 h-16 rounded-lg border-2 border-primary-container object-cover bg-surface-variant flex items-center justify-center overflow-hidden">
              <span className="material-symbols-outlined text-4xl text-on-surface-variant">person</span>
            </div>
            <div className="absolute -bottom-2 -right-2 bg-primary-container text-on-primary-container font-label-caps text-[10px] px-1.5 py-0.5 rounded shadow-[0_0_10px_rgba(74,158,255,0.6)]">Lvl. 42</div>
          </div>
          <div className="flex flex-col w-full">
            <span className="font-body-bold text-body-bold text-primary">{user.nickname}</span>
            <span className="font-body-base text-body-base text-on-surface-variant text-sm">{user.class_name}</span>
            <div className="w-full bg-surface-variant h-1.5 mt-2 rounded-full overflow-hidden border border-white/5">
              <div className="bg-primary-container h-full shadow-[0_0_10px_#4a9eff]" style={{ width: "85%" }}></div>
            </div>
          </div>
        </div>

        {/* Categories (Scrollable horizontally on mobile, list on desktop) */}
        <div className="overflow-x-auto hide-scrollbar w-full py-1 -mx-margin-mobile px-margin-mobile md:mx-0 md:px-0" style={{ msOverflowStyle: 'none', scrollbarWidth: 'none' }}>
          <div className="flex md:flex-col gap-2 min-w-max md:min-w-0">
            <button className="flex items-center gap-2 px-4 py-2.5 rounded-lg bg-primary-container/20 border border-primary text-primary font-body-bold text-body-bold shadow-[0_0_15px_rgba(74,158,255,0.2)] whitespace-nowrap transition-all">
              <span className="material-symbols-outlined text-[20px]">category</span> Tất cả
            </button>
            <button className="flex items-center gap-2 px-4 py-2.5 rounded-lg bg-surface-container border border-white/5 text-on-surface-variant font-body-bold text-body-bold hover:bg-white/5 transition-all whitespace-nowrap">
              <span className="material-symbols-outlined text-[20px]">swords</span> Trang bị
            </button>
            <button className="flex items-center gap-2 px-4 py-2.5 rounded-lg bg-surface-container border border-white/5 text-on-surface-variant font-body-bold text-body-bold hover:bg-white/5 transition-all whitespace-nowrap">
              <span className="material-symbols-outlined text-[20px]">water_drop</span> Tiêu thụ
            </button>
            <button className="flex items-center gap-2 px-4 py-2.5 rounded-lg bg-surface-container border border-white/5 text-on-surface-variant font-body-bold text-body-bold hover:bg-white/5 transition-all whitespace-nowrap">
              <span className="material-symbols-outlined text-[20px]">diamond</span> Nguyên liệu
            </button>
            <button className="flex items-center gap-2 px-4 py-2.5 rounded-lg bg-surface-container border border-white/5 text-on-surface-variant font-body-bold text-body-bold hover:bg-white/5 transition-all whitespace-nowrap">
              <span className="material-symbols-outlined text-[20px]">assignment</span> Nhiệm vụ
            </button>
          </div>
        </div>
      </div>

      {/* Right Pane: Item Grid & Detail */}
      <div className="md:col-span-8 flex flex-col gap-4 h-full">
        {/* Item Grid */}
        <div className="bg-surface-container/65 backdrop-blur-[40px] rounded-xl border border-white/12 p-4 shadow-[0_8px_32px_0_rgba(0,0,0,0.4)]">
          <div className="flex justify-between items-center mb-4">
            <span className="font-label-caps text-label-caps text-on-surface-variant">SỨC CHỨA: 45 / 100</span>
            <button className="text-primary hover:text-primary-fixed transition-colors">
              <span className="material-symbols-outlined">filter_list</span>
            </button>
          </div>
          
          <div className="grid grid-cols-4 sm:grid-cols-5 md:grid-cols-6 lg:grid-cols-8 gap-3">
            {/* Mythic Item (Selected) */}
            <div onClick={() => setSelectedItem("mythic_dagger")} className={`aspect-square relative rounded-lg border-2 ${selectedItem === 'mythic_dagger' ? 'border-error shadow-[0_0_15px_rgba(255,180,171,0.5)] animate-pulse' : 'border-error/50 opacity-80'} bg-surface-container-low overflow-hidden flex items-center justify-center shimmer-legendary cursor-pointer`} style={{ "--glow-color": "rgba(255,180,171,0.4)" } as any}>
              <span className="material-symbols-outlined text-error text-3xl drop-shadow-[0_0_8px_#ffb4ab]">swords</span>
              <div className="absolute bottom-1 right-1 font-label-caps text-[10px] text-white bg-black/60 px-1 rounded backdrop-blur-sm">1</div>
              <div className="absolute inset-0 bg-error/10 pointer-events-none"></div>
            </div>
            
            {/* Legendary Item */}
            <div onClick={() => setSelectedItem("legendary_feather")} className={`aspect-square relative rounded-lg border ${selectedItem === 'legendary_feather' ? 'border-[#ff8c00] shadow-[0_0_10px_rgba(255,140,0,0.3)] animate-pulse' : 'border-[#ff8c00]/50 opacity-80'} bg-surface-container-low overflow-hidden flex items-center justify-center cursor-pointer`}>
              <span className="material-symbols-outlined text-[#ff8c00] text-3xl drop-shadow-[0_0_6px_#ff8c00]">spa</span>
              <div className="absolute bottom-1 right-1 font-label-caps text-[10px] text-white bg-black/60 px-1 rounded backdrop-blur-sm">3</div>
              <div className="absolute inset-0 bg-[#ff8c00]/5 pointer-events-none"></div>
            </div>
            
            {/* Epic Item */}
            <div className="aspect-square relative rounded-lg border border-[#a855f7] bg-surface-container-low shadow-[0_0_8px_rgba(168,85,247,0.2)] overflow-hidden flex items-center justify-center cursor-pointer">
              <span className="material-symbols-outlined text-[#a855f7] text-3xl drop-shadow-[0_0_5px_#a855f7]">front_hand</span>
              <div className="absolute bottom-1 right-1 font-label-caps text-[10px] text-white bg-black/60 px-1 rounded backdrop-blur-sm">1</div>
            </div>
            
            {/* Rare Item */}
            <div className="aspect-square relative rounded-lg border border-primary container-low overflow-hidden flex items-center justify-center cursor-pointer">
              <span className="material-symbols-outlined text-[32px] text-primary drop-shadow-[0_0_5px_#a4c9ff]">menu_book</span>
              <div className="absolute bottom-1 right-1 font-label-caps text-[10px] text-white bg-black/60 px-1 rounded backdrop-blur-sm">5</div>
            </div>
            
            {/* Uncommon Item */}
            <div className="aspect-square relative rounded-lg border border-tertiary-fixed-dim bg-surface-container-low overflow-hidden flex items-center justify-center cursor-pointer">
              <span className="material-symbols-outlined text-[32px] text-tertiary-fixed-dim" style={{ fontVariationSettings: "'FILL' 1" }}>water_drop</span>
              <div className="absolute bottom-1 right-1 font-label-caps text-[10px] text-white bg-black/60 px-1 rounded backdrop-blur-sm">25</div>
            </div>
            
            {/* Common Items */}
            <div className="aspect-square relative rounded-lg border border-white/20 bg-surface-container-low overflow-hidden flex items-center justify-center opacity-80 cursor-pointer">
              <span className="material-symbols-outlined text-[28px] text-on-surface-variant">eco</span>
              <div className="absolute bottom-1 right-1 font-label-caps text-[10px] text-white bg-black/60 px-1 rounded backdrop-blur-sm">99</div>
            </div>
            <div className="aspect-square relative rounded-lg border border-white/20 bg-surface-container-low overflow-hidden flex items-center justify-center opacity-80 cursor-pointer">
              <span className="material-symbols-outlined text-[28px] text-on-surface-variant">lens</span>
              <div className="absolute bottom-1 right-1 font-label-caps text-[10px] text-white bg-black/60 px-1 rounded backdrop-blur-sm">12</div>
            </div>
            
            {/* Empty Slots */}
            {Array.from({ length: 9 }).map((_, i) => (
              <div key={i} className={`aspect-square rounded-lg border border-white/5 bg-surface-container-lowest/50 ${i > 4 ? 'hidden md:block' : ''}`}></div>
            ))}
          </div>
        </div>

        {/* Item Detail Panel */}
        <div className={`bg-surface-container/80 backdrop-blur-[50px] rounded-xl border p-5 mt-auto relative overflow-hidden transition-all duration-300 ${selectedItem === 'mythic_dagger' ? 'border-error/30 shadow-[0_10px_40px_rgba(255,180,171,0.15)]' : 'border-[#ff8c00]/30 shadow-[0_10px_40px_rgba(255,140,0,0.15)]'}`}>
          {/* Decorative background glow for selected item */}
          <div className={`absolute top-0 right-0 w-32 h-32 rounded-full blur-[40px] pointer-events-none ${selectedItem === 'mythic_dagger' ? 'bg-error/20' : 'bg-[#ff8c00]/20'}`}></div>
          
          <div className="flex items-start gap-4 mb-4 relative z-10">
            <div className={`w-16 h-16 rounded-lg border-2 bg-surface-container-lowest flex items-center justify-center flex-shrink-0 ${selectedItem === 'mythic_dagger' ? 'border-error shadow-[0_0_15px_rgba(255,180,171,0.4)]' : 'border-[#ff8c00] shadow-[0_0_15px_rgba(255,140,0,0.4)]'}`}>
              <span className={`material-symbols-outlined text-4xl ${selectedItem === 'mythic_dagger' ? 'text-error' : 'text-[#ff8c00]'}`}>
                {selectedItem === 'mythic_dagger' ? 'swords' : 'spa'}
              </span>
            </div>
            <div>
              <div className="flex items-center gap-2 mb-1">
                <h2 className={`font-headline-md text-headline-md ${selectedItem === 'mythic_dagger' ? 'text-error' : 'text-[#ff8c00]'}`}>
                  {selectedItem === 'mythic_dagger' ? 'Shadow Dagger' : 'Phoenix Feather'}
                </h2>
              </div>
              <span className={`inline-block px-2 py-0.5 rounded text-[10px] font-label-caps border ${selectedItem === 'mythic_dagger' ? 'bg-error/20 text-error border-error/50' : 'bg-[#ff8c00]/20 text-[#ff8c00] border-[#ff8c00]/50'}`}>
                {selectedItem === 'mythic_dagger' ? 'THẦN THOẠI (MYTHIC)' : 'HUYỀN THOẠI (LEGENDARY)'}
              </span>
              <div className="text-xs text-on-surface-variant mt-1 font-body-base">
                {selectedItem === 'mythic_dagger' ? 'Vũ khí • Cấp yêu cầu: 40' : 'Nguyên liệu quý'}
              </div>
            </div>
          </div>
          
          <div className="font-body-base text-body-base text-on-surface-variant mb-6 relative z-10">
            <p className="mb-2">
              {selectedItem === 'mythic_dagger' ? '"Một con dao găm được rèn từ bóng tối thuần khiết. Nó dường như hấp thụ ánh sáng xung quanh."' : '"Một chiếc lông vũ tỏa ra sức nóng ấm áp, tương truyền có thể hồi sinh người chết."'}
            </p>
            {selectedItem === 'mythic_dagger' ? (
              <ul className="text-sm space-y-1 mt-4">
                <li className="flex items-center gap-2"><span className="text-error font-bold">+ 150</span> Sức mạnh tấn công</li>
                <li className="flex items-center gap-2"><span className="text-primary">+ 15%</span> Tỷ lệ bạo kích</li>
                <li className="flex items-center gap-2 text-secondary-fixed-dim">Hiệu ứng: Xuyên giáp bóng tối (Bỏ qua 20% phòng thủ)</li>
              </ul>
            ) : (
              <ul className="text-sm space-y-1 mt-4">
                <li className="flex items-center gap-2"><span className="text-[#ff8c00] font-bold">Dùng để:</span> Chế tạo trang bị Thần Thoại</li>
                <li className="flex items-center gap-2 text-secondary-fixed-dim">Hiệu ứng: Hồi sinh hoàn toàn khi HP chạm mốc 0</li>
              </ul>
            )}
          </div>
          
          <div className="flex gap-3 relative z-10">
            <button className="flex-1 bg-surface-variant border border-white/10 text-on-surface-variant font-label-caps py-3 rounded-lg hover:bg-white/5 transition-colors">BỎ ĐI</button>
            <button className={`flex-[2] text-[#3a0001] font-headline-md py-3 rounded-lg hover:brightness-125 transition-all active:scale-95 relative overflow-hidden ${selectedItem === 'mythic_dagger' ? 'bg-gradient-to-r from-[#93000a] to-[#ffb4ab] shadow-[0_0_20px_rgba(255,180,171,0.4)]' : 'bg-gradient-to-r from-[#cc5500] to-[#ffdaaa] shadow-[0_0_20px_rgba(255,140,0,0.4)]'}`}>
              <span className="relative z-10 text-lg">
                {selectedItem === 'mythic_dagger' ? 'TRANG BỊ' : 'SỬ DỤNG'}
              </span>
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}
