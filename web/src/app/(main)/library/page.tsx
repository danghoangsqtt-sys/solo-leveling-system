"use client";

import { useState } from "react";
import "./library.css";

// ── Demo Library Data ──
const DEMO_BOOKS = [
  { id: "b1", title: "Clean Architecture", author: "Robert C. Martin", total: 432, read: 432, status: "completed", type: "book" },
  { id: "b2", title: "Designing Data-Intensive Applications", author: "Martin Kleppmann", total: 616, read: 120, status: "reading", type: "book" },
  { id: "b3", title: "Refactoring UI", author: "Adam Wathan & Steve Schoger", total: 252, read: 0, status: "want_to_read", type: "book" },
];

const DEMO_COURSES = [
  { id: "c1", title: "Epic React", provider: "Kent C. Dodds", total: 8, read: 8, status: "completed", type: "course" },
  { id: "c2", title: "Three.js Journey", provider: "Bruno Simon", total: 72, read: 30, status: "reading", type: "course" },
];

export default function LibraryPage() {
  const [activeTab, setActiveTab] = useState<"books" | "courses">("books");

  const items = activeTab === "books" ? DEMO_BOOKS : DEMO_COURSES;
  const reading = items.filter(i => i.status === "reading");
  const completed = items.filter(i => i.status === "completed");
  const planned = items.filter(i => i.status === "want_to_read");

  const renderCard = (item: { id: string; title: string; author?: string; provider?: string; total: number; read: number; status: string; type: string }) => {
    const percent = Math.round((item.read / item.total) * 100);
    
    return (
      <div key={item.id} className="library-card game-card">
        <div className="library-card__cover">
          {item.type === 'book' ? '📖' : '💻'}
        </div>
        <div className="library-card__info">
          <h3 className="library-card__title">{item.title}</h3>
          <div className="library-card__author">{item.author || item.provider}</div>
          
          <div className="library-card__progress-text font-mono">
            {item.read} / {item.total} {item.type === 'book' ? 'pages' : 'modules'} ({percent}%)
          </div>
          <div className="progress-bar">
            <div 
              className="progress-bar__fill"
              style={{ 
                width: `${percent}%`,
                background: percent === 100 ? 'var(--system-green)' : 'var(--system-blue)' 
              }}
            />
          </div>
        </div>
      </div>
    );
  };

  return (
    <div className="library-page">
      <div className="library-page__header animate-fadeIn">
        <h1 className="font-heading" style={{ fontSize: 'var(--text-2xl)', color: 'var(--system-blue)' }}>
          📚 THƯ VIỆN KHO TÀNG
        </h1>
        <p style={{ color: 'var(--text-secondary)' }}>Tri thức là sức mạnh tuyệt đối</p>
      </div>

      <div className="library__tabs">
        <button 
          className={`finance__tab ${activeTab === 'books' ? 'finance__tab--active' : ''}`}
          onClick={() => setActiveTab('books')}
        >
          SÁCH (PDF/EPUB)
        </button>
        <button 
          className={`finance__tab ${activeTab === 'courses' ? 'finance__tab--active' : ''}`}
          onClick={() => setActiveTab('courses')}
        >
          KHÓA HỌC (ONLINE)
        </button>
      </div>

      <div className="library__content animate-slideInUp">
        
        {reading.length > 0 && (
          <section className="library__section">
            <h2 className="library__section-title font-heading">🔥 ĐANG TIẾN HÀNH</h2>
            <div className="library__grid">
              {reading.map(renderCard)}
            </div>
          </section>
        )}

        {planned.length > 0 && (
          <section className="library__section">
            <h2 className="library__section-title font-heading text-muted">📌 KẾ HOẠCH</h2>
            <div className="library__grid">
              {planned.map(renderCard)}
            </div>
          </section>
        )}

        {completed.length > 0 && (
          <section className="library__section">
            <h2 className="library__section-title font-heading text-success">🏆 ĐÃ HOÀN THÀNH</h2>
            <div className="library__grid opacity-80">
              {completed.map(renderCard)}
            </div>
          </section>
        )}
        
      </div>
    </div>
  );
}
