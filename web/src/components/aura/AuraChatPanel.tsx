"use client";

import { useState, useRef, useEffect } from "react";
import type { AuraMessage } from "@/lib/types";
import "./aura.css";

interface AuraChatPanelProps {
  isOpen: boolean;
  onClose: () => void;
}

const INITIAL_MESSAGES: AuraMessage[] = [
  {
    id: "1",
    role: "assistant",
    content: "Xin chào! Tôi là Aura. Hôm nay bạn muốn quản lý nhiệm vụ, xem tiến độ học tập, hay kiểm tra tài chính?",
    created_at: new Date().toISOString(),
  },
];

export default function AuraChatPanel({ isOpen, onClose }: AuraChatPanelProps) {
  const [messages, setMessages] = useState<AuraMessage[]>(INITIAL_MESSAGES);
  const [inputValue, setInputValue] = useState("");
  const [isTyping, setIsTyping] = useState(false);
  const messagesEndRef = useRef<HTMLDivElement>(null);

  // Auto-scroll to bottom
  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: "smooth" });
  }, [messages, isTyping]);

  if (!isOpen) return null;

  const handleSend = () => {
    if (!inputValue.trim()) return;

    const newUserMsg: AuraMessage = {
      id: Date.now().toString(),
      role: "user",
      content: inputValue,
      created_at: new Date().toISOString(),
    };

    setMessages((prev) => [...prev, newUserMsg]);
    setInputValue("");
    setIsTyping(true);

    // Simulate AI response (to be replaced with actual Supabase Edge Function call to Gemini)
    setTimeout(() => {
      const newAuraMsg: AuraMessage = {
        id: (Date.now() + 1).toString(),
        role: "assistant",
        content: "Hệ thống AI hiện đang trong chế độ demo. Tính năng kết nối Gemini API sẽ được kích hoạt ở Phase 5.",
        created_at: new Date().toISOString(),
      };
      setMessages((prev) => [...prev, newAuraMsg]);
      setIsTyping(false);
    }, 1500);
  };

  const handleKeyDown = (e: React.KeyboardEvent) => {
    if (e.key === "Enter" && !e.shiftKey) {
      e.preventDefault();
      handleSend();
    }
  };

  return (
    <div className="aura-chat">
      <div className="aura-chat__header">
        <div className="aura-chat__title">
          <span>🌟</span> Aura Assistant
        </div>
        <button className="btn btn-secondary btn-sm" onClick={onClose}>
          ✕
        </button>
      </div>

      <div className="aura-chat__messages">
        {messages.map((msg) => (
          <div
            key={msg.id}
            className={`aura-msg ${
              msg.role === "user" ? "aura-msg--user" : "aura-msg--aura"
            }`}
          >
            <div className="aura-msg__avatar">
              {msg.role === "user" ? "🧑" : "🌟"}
            </div>
            <div className="aura-msg__bubble">{msg.content}</div>
          </div>
        ))}
        {isTyping && (
          <div className="aura-msg aura-msg--aura">
            <div className="aura-msg__avatar">🌟</div>
            <div className="aura-msg__bubble">
              <span className="typing-dots">...</span>
            </div>
          </div>
        )}
        <div ref={messagesEndRef} />
      </div>

      <div className="aura-chat__input-area">
        <button className="aura-chat__btn aura-chat__btn--voice" title="Voice Input">
          🎤
        </button>
        <input
          type="text"
          className="aura-chat__input"
          placeholder="Nhập lệnh hoặc hỏi Aura..."
          value={inputValue}
          onChange={(e) => setInputValue(e.target.value)}
          onKeyDown={handleKeyDown}
        />
        <button className="aura-chat__btn" onClick={handleSend}>
          ➤
        </button>
      </div>
    </div>
  );
}
