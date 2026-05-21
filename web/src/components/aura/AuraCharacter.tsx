"use client";

import { useEffect, useState } from "react";
import "./aura.css";

interface AuraCharacterProps {
  mood?: "idle" | "happy" | "thinking" | "warning";
  isTalking?: boolean;
}

export default function AuraCharacter({
  mood = "idle",
  isTalking = false,
}: AuraCharacterProps) {
  const [blink, setBlink] = useState(false);

  // Random blinking effect
  useEffect(() => {
    const blinkInterval = setInterval(() => {
      setBlink(true);
      setTimeout(() => setBlink(false), 200); // Blink duration
    }, Math.random() * 3000 + 2000); // Blink every 2-5s

    return () => clearInterval(blinkInterval);
  }, []);

  return (
    <div className={`aura-npc aura-npc--${mood} ${isTalking ? "aura-npc--talking" : ""}`}>
      {/* Glow behind character */}
      <div className="aura-npc__glow" />

      {/* Main Character Body (using SVG to simulate the anime character for now) */}
      <div className="aura-npc__body animate-breathe">
        {/* Placeholder SVG for Aura character. In a real app, use multi-layer PNGs. */}
        <svg
          viewBox="0 0 200 300"
          fill="none"
          xmlns="http://www.w3.org/2000/svg"
          className="aura-npc__svg"
        >
          {/* Hair back */}
          <path
            d="M50 150 C30 200, 30 280, 50 300 L150 300 C170 280, 170 200, 150 150 Z"
            fill="var(--system-purple)"
            opacity="0.8"
          />
          
          {/* Body/Clothes */}
          <path
            d="M60 220 C60 180, 140 180, 140 220 L180 300 L20 300 Z"
            fill="var(--bg-elevated)"
            stroke="var(--system-blue)"
            strokeWidth="2"
          />
          
          {/* Head */}
          <circle cx="100" cy="120" r="45" fill="#FFE0C8" />
          
          {/* Hair front */}
          <path
            d="M55 120 C55 60, 145 60, 145 120 C145 140, 120 100, 100 100 C80 100, 55 140, 55 120 Z"
            fill="var(--system-purple)"
          />

          {/* Eyes */}
          <g className={blink ? "aura-npc__eye--blink" : ""}>
            {/* Left Eye */}
            <ellipse cx="85" cy="125" rx="6" ry="8" fill={mood === "warning" ? "var(--system-red)" : "var(--system-blue)"} />
            {/* Right Eye */}
            <ellipse cx="115" cy="125" rx="6" ry="8" fill={mood === "warning" ? "var(--system-red)" : "var(--system-blue)"} />
          </g>

          {/* Mouth */}
          <path
            d={
              mood === "happy"
                ? "M90 145 Q100 155 110 145" // Smile
                : mood === "warning"
                ? "M90 148 Q100 142 110 148" // Frown
                : "M95 145 L105 145" // Neutral
            }
            stroke="#D980FA"
            strokeWidth="2"
            fill="transparent"
            className={isTalking ? "aura-npc__mouth--talking" : ""}
          />
          
          {/* Accessory (Floating Crystal) */}
          <path
            d="M95 50 L100 40 L105 50 L100 60 Z"
            fill="var(--system-cyan)"
            className="animate-float"
            style={{ transformOrigin: "100px 50px" }}
          />
        </svg>

        {/* Particles */}
        <div className="aura-npc__particles">
          {[
            { left: "10%", delay: "0s", duration: "2s" },
            { left: "30%", delay: "1.2s", duration: "3s" },
            { left: "50%", delay: "0.5s", duration: "2.5s" },
            { left: "70%", delay: "1.8s", duration: "3.2s" },
            { left: "90%", delay: "0.8s", duration: "2.8s" },
          ].map((particle, i) => (
            <div
              key={i}
              className="aura-npc__particle"
              style={{
                left: particle.left,
                animationDelay: particle.delay,
                animationDuration: particle.duration,
              }}
            />
          ))}
        </div>
      </div>
    </div>
  );
}
