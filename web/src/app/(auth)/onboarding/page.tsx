"use client";

import { useState, useEffect } from "react";
import { useRouter } from "next/navigation";
import { CLASS_ICONS } from "@/lib/types";
import type { ClassName } from "@/lib/types";
import "./onboarding.css";

const CLASSES: ClassName[] = [
  "Warrior", "Mage", "Ranger", "Guardian",
  "Bard", "Alchemist", "Healer", "Scholar"
];

export default function OnboardingPage() {
  const router = useRouter();
  const [step, setStep] = useState(0); // 0: Intro, 1: Name, 2: Class, 3: Final
  const [nickname, setNickname] = useState("");
  const [selectedClass, setSelectedClass] = useState<ClassName | null>(null);
  const [introText, setIntroText] = useState("");
  
  const fullIntro = "SYSTEM INITIALIZING...\nNgười chơi mới được phát hiện.\nĐang tiến hành kết nối đồng bộ hóa...";

  useEffect(() => {
    if (step === 0) {
      let i = 0;
      const interval = setInterval(() => {
        setIntroText(fullIntro.substring(0, i));
        i++;
        if (i > fullIntro.length) {
          clearInterval(interval);
          setTimeout(() => setStep(1), 1500);
        }
      }, 50);
      return () => clearInterval(interval);
    }
  }, [step]);

  const handleNext = () => {
    if (step === 1 && nickname.trim()) setStep(2);
    else if (step === 2 && selectedClass) setStep(3);
    else if (step === 3) {
      // Typically save to Supabase here, then redirect
      router.push("/");
    }
  };

  return (
    <div className="onboarding">
      {/* ── Step 0: Terminal Intro ── */}
      {step === 0 && (
        <div className="onboarding__terminal font-mono">
          <div className="onboarding__terminal-text">{introText}<span className="cursor">_</span></div>
        </div>
      )}

      {/* ── Step 1: Nickname ── */}
      {step === 1 && (
        <div className="onboarding__card game-card animate-scaleIn">
          <h2 className="font-heading glow-text--blue">IDENTIFICATION</h2>
          <p className="onboarding__desc">Hệ thống yêu cầu xác nhận danh tính. Vui lòng nhập tên gọi của bạn.</p>
          <input
            type="text"
            className="input onboarding__input"
            placeholder="Tên người chơi..."
            value={nickname}
            onChange={(e) => setNickname(e.target.value)}
            onKeyDown={(e) => e.key === 'Enter' && handleNext()}
            autoFocus
          />
          <button 
            className="btn btn-primary" 
            onClick={handleNext}
            disabled={!nickname.trim()}
          >
            Xác nhận
          </button>
        </div>
      )}

      {/* ── Step 2: Class Selection ── */}
      {step === 2 && (
        <div className="onboarding__card game-card animate-scaleIn" style={{ width: '100%', maxWidth: '800px' }}>
          <h2 className="font-heading glow-text--purple">CHOOSE YOUR PATH</h2>
          <p className="onboarding__desc">Mỗi Class sẽ định hình hệ thống kỹ năng và nhiệm vụ khởi đầu của bạn.</p>
          
          <div className="onboarding__classes">
            {CLASSES.map((cls) => (
              <button
                key={cls}
                className={`onboarding__class-card game-card ${selectedClass === cls ? 'onboarding__class-card--selected' : ''}`}
                onClick={() => setSelectedClass(cls)}
              >
                <span className="onboarding__class-icon">{CLASS_ICONS[cls]}</span>
                <span className="onboarding__class-name font-heading">{cls}</span>
              </button>
            ))}
          </div>
          
          <div className="onboarding__actions">
            <button className="btn btn-secondary" onClick={() => setStep(1)}>Quay lại</button>
            <button 
              className="btn btn-primary" 
              onClick={handleNext}
              disabled={!selectedClass}
            >
              Tiến Hóa
            </button>
          </div>
        </div>
      )}

      {/* ── Step 3: Final ── */}
      {step === 3 && (
        <div className="onboarding__card game-card animate-scaleIn" style={{ textAlign: 'center' }}>
          <div className="onboarding__success-icon">✨</div>
          <h2 className="font-heading glow-text--gold">AWAKENING COMPLETE</h2>
          <p className="onboarding__desc" style={{ marginBottom: '2rem' }}>
            Chào mừng, <strong>{nickname}</strong> — <strong>{selectedClass}</strong>.<br/>
            Hệ thống Solo Leveling đã sẵn sàng đồng hành cùng bạn.
          </p>
          <button className="btn btn-primary btn-lg" onClick={handleNext}>
            ENTER THE SYSTEM
          </button>
        </div>
      )}
    </div>
  );
}
