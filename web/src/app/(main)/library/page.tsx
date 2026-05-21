"use client";

import { DEMO_USER } from "@/lib/types";

export default function LibraryPage() {
  const user = DEMO_USER;

  return (
    <div className="col-span-12 flex flex-col gap-container-gap relative z-10 pt-16 md:pt-28">
      {/* Search Bar */}
      <div className="relative group">
        <div className="absolute inset-y-0 left-0 pl-4 flex items-center pointer-events-none text-on-surface-variant group-focus-within:text-primary transition-colors">
          <span className="material-symbols-outlined" style={{ fontVariationSettings: "'FILL' 0" }}>search</span>
        </div>
        <input 
          className="w-full bg-white/[0.06] border border-white/12 rounded-xl py-3 pl-12 pr-4 text-on-background font-body-base placeholder-on-surface-variant focus:outline-none focus:border-primary focus:ring-1 focus:ring-primary focus:bg-primary/5 transition-all shadow-[0_4px_20px_rgba(0,0,0,0.2)]" 
          placeholder="Search archives..." 
          type="text"
        />
      </div>

      {/* Recently Studied Bento Grid */}
      <section>
        <h2 className="font-headline-md-mobile text-headline-md-mobile text-on-surface flex items-center gap-2 mb-4">
          <span className="material-symbols-outlined text-tertiary" style={{ fontVariationSettings: "'FILL' 1" }}>history</span>
          Recently Studied
        </h2>
        
        <div className="flex overflow-x-auto gap-4 pb-4 scrollbar-hide -mx-margin-mobile px-margin-mobile md:mx-0 md:px-0" style={{ msOverflowStyle: 'none', scrollbarWidth: 'none' }}>
          {/* Card 1 (Legendary) */}
          <div className="glass-panel shimmer-legendary rounded-xl p-4 min-w-[240px] border-secondary-fixed/50 flex-shrink-0 cursor-pointer hover:scale-105 transition-transform duration-300">
            <div className="flex justify-between items-start mb-3 relative z-10">
              <div className="w-10 h-10 rounded-full bg-secondary-container/20 flex items-center justify-center text-secondary-fixed">
                <span className="material-symbols-outlined" style={{ fontVariationSettings: "'FILL' 1" }}>menu_book</span>
              </div>
              <div className="font-label-caps text-label-caps text-secondary-fixed bg-secondary-container/20 px-2 py-0.5 rounded">SSR</div>
            </div>
            <h3 className="font-body-bold text-body-bold text-on-background line-clamp-1 mb-1 relative z-10">IELTS Cambridge 18</h3>
            <p className="font-body-base text-sm text-on-surface-variant mb-4 relative z-10">Reading Practice Test 1</p>
            <div className="flex items-center gap-3 relative z-10">
              <div className="relative w-8 h-8">
                <svg className="w-full h-full transform -rotate-90" viewBox="0 0 36 36">
                  <path className="text-surface-variant" d="M18 2.0845 a 15.9155 15.9155 0 0 1 0 31.831 a 15.9155 15.9155 0 0 1 0 -31.831" fill="none" stroke="currentColor" strokeWidth="3"></path>
                  <path className="text-secondary-fixed transition-all duration-1000 ease-out" d="M18 2.0845 a 15.9155 15.9155 0 0 1 0 31.831 a 15.9155 15.9155 0 0 1 0 -31.831" fill="none" stroke="currentColor" strokeDasharray="75, 100" strokeWidth="3"></path>
                </svg>
                <span className="absolute inset-0 flex items-center justify-center font-label-caps text-[10px] text-secondary-fixed">75%</span>
              </div>
              <div className="flex-1 h-1 bg-surface-variant rounded-full overflow-hidden">
                <div className="h-full bg-secondary-fixed w-[75%] rounded-full shadow-[0_0_8px_rgba(255,225,109,0.8)]"></div>
              </div>
            </div>
          </div>
          
          {/* Card 2 */}
          <div className="glass-panel rounded-xl p-4 min-w-[240px] border-primary/30 flex-shrink-0 cursor-pointer hover:scale-105 transition-transform duration-300">
            <div className="flex justify-between items-start mb-3">
              <div className="w-10 h-10 rounded-full bg-primary-container/20 flex items-center justify-center text-primary">
                <span className="material-symbols-outlined" style={{ fontVariationSettings: "'FILL' 1" }}>memory</span>
              </div>
              <div className="font-label-caps text-label-caps text-primary bg-primary-container/20 px-2 py-0.5 rounded">SR</div>
            </div>
            <h3 className="font-body-bold text-body-bold text-on-background line-clamp-1 mb-1">IoT Tutorial</h3>
            <p className="font-body-base text-sm text-on-surface-variant mb-4">ESP32 Setup Guide</p>
            <div className="flex items-center gap-3">
              <div className="relative w-8 h-8">
                <svg className="w-full h-full transform -rotate-90" viewBox="0 0 36 36">
                  <path className="text-surface-variant" d="M18 2.0845 a 15.9155 15.9155 0 0 1 0 31.831 a 15.9155 15.9155 0 0 1 0 -31.831" fill="none" stroke="currentColor" strokeWidth="3"></path>
                  <path className="text-primary transition-all duration-1000 ease-out" d="M18 2.0845 a 15.9155 15.9155 0 0 1 0 31.831 a 15.9155 15.9155 0 0 1 0 -31.831" fill="none" stroke="currentColor" strokeDasharray="30, 100" strokeWidth="3"></path>
                </svg>
                <span className="absolute inset-0 flex items-center justify-center font-label-caps text-[10px] text-primary">30%</span>
              </div>
              <div className="flex-1 h-1 bg-surface-variant rounded-full overflow-hidden">
                <div className="h-full bg-primary w-[30%] rounded-full shadow-[0_0_8px_rgba(164,201,255,0.8)]"></div>
              </div>
            </div>
          </div>
        </div>
      </section>

      {/* Folder Tree Section */}
      <section className="glass-panel rounded-xl border-white/10 overflow-hidden">
        <div className="p-4 border-b border-white/10 bg-white/[0.02]">
          <h2 className="font-headline-md-mobile text-headline-md-mobile text-on-surface flex items-center gap-2">
            <span className="material-symbols-outlined text-primary" style={{ fontVariationSettings: "'FILL' 1" }}>account_tree</span>
            System Directory
          </h2>
        </div>
        
        <div className="p-2 space-y-1">
          {/* Folder 1 */}
          <div className="rounded-lg hover:bg-white/5 transition-colors cursor-pointer">
            <div className="flex items-center gap-3 p-3">
              <span className="material-symbols-outlined text-secondary-fixed" style={{ fontVariationSettings: "'FILL' 1" }}>folder</span>
              <span className="font-body-bold text-body-bold text-on-background flex-1">IELTS Materials</span>
              <span className="material-symbols-outlined text-on-surface-variant text-sm">expand_more</span>
            </div>
            {/* Sub-folder 1 */}
            <div className="pl-8 pr-2 pb-2 space-y-1">
              <div className="rounded-lg hover:bg-white/5 transition-colors cursor-pointer flex items-center gap-3 p-2 bg-white/[0.03]">
                <span className="material-symbols-outlined text-secondary-fixed/80" style={{ fontVariationSettings: "'FILL' 0" }}>folder_open</span>
                <span className="font-body-base text-body-base text-on-surface">Reading</span>
                <span className="material-symbols-outlined text-on-surface-variant text-sm">expand_more</span>
              </div>
              {/* Files in Sub-folder */}
              <div className="pl-10 space-y-1">
                <div className="rounded-lg hover:bg-white/10 transition-colors cursor-pointer flex items-center gap-3 p-2 group">
                  <span className="material-symbols-outlined text-error" style={{ fontVariationSettings: "'FILL' 1" }}>picture_as_pdf</span>
                  <span className="font-body-base text-body-base text-on-surface-variant group-hover:text-on-background transition-colors flex-1">Cambridge 18.pdf</span>
                  <span className="font-label-caps text-label-caps text-on-surface-variant">24 MB</span>
                </div>
                <div className="rounded-lg hover:bg-white/10 transition-colors cursor-pointer flex items-center gap-3 p-2 group">
                  <span className="material-symbols-outlined text-error" style={{ fontVariationSettings: "'FILL' 1" }}>picture_as_pdf</span>
                  <span className="font-body-base text-body-base text-on-surface-variant group-hover:text-on-background transition-colors flex-1">Practice Set 1.pdf</span>
                  <span className="font-label-caps text-label-caps text-on-surface-variant">5 MB</span>
                </div>
              </div>
            </div>
          </div>
          
          {/* Folder 2 */}
          <div className="rounded-lg hover:bg-white/5 transition-colors cursor-pointer">
            <div className="flex items-center gap-3 p-3">
              <span className="material-symbols-outlined text-primary" style={{ fontVariationSettings: "'FILL' 1" }}>folder</span>
              <span className="font-body-bold text-body-bold text-on-background flex-1">IoT & Embedded</span>
              <span className="material-symbols-outlined text-on-surface-variant text-sm">chevron_right</span>
            </div>
          </div>
        </div>
      </section>
      
      {/* Floating Action Button for Library */}
      <button className="fixed bottom-28 right-margin-mobile md:right-margin-desktop w-14 h-14 bg-gradient-to-tr from-primary to-primary-container rounded-full shadow-[0_0_20px_rgba(74,158,255,0.6)] flex items-center justify-center text-on-primary-container hover:scale-110 active:scale-95 transition-all z-40 border border-white/20">
        <span className="material-symbols-outlined text-3xl" style={{ fontVariationSettings: "'FILL' 0" }}>add</span>
      </button>
    </div>
  );
}
