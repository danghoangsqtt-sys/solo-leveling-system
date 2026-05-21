"use client";

import { useState } from "react";
import "./finance.css";

// ── Demo Finance Data (DHEbook concept) ──
const DEMO_TRANSACTIONS = [
  { id: "tx1", date: "2026-05-21T08:00:00Z", category: "Food", amount: -45000, desc: "Phở sáng" },
  { id: "tx2", date: "2026-05-20T19:00:00Z", category: "Transport", amount: -12000, desc: "Gửi xe tháng" },
  { id: "tx3", date: "2026-05-20T12:00:00Z", category: "Income", amount: 5000000, desc: "Freelance Project" },
  { id: "tx4", date: "2026-05-19T20:00:00Z", category: "System", amount: 50, desc: "Gold Reward (Quest Rank C)" }, // Virtual Currency
];

export default function FinancePage() {
  const [activeTab, setActiveTab] = useState<"real" | "virtual">("real");

  // Summary logic
  const totalBalance = 15500000;
  const spentThisMonth = 4250000;
  const budget = 5000000;
  const budgetPercent = (spentThisMonth / budget) * 100;

  return (
    <div className="finance-page">
      <div className="finance-page__header animate-fadeIn">
        <h1 className="font-heading" style={{ fontSize: 'var(--text-2xl)', color: 'var(--system-blue)' }}>
          💳 DHEBOOK SYSTEM
        </h1>
        <p style={{ color: 'var(--text-secondary)' }}>Trạm kiểm soát tài chính & tài sản hệ thống</p>
      </div>

      <div className="finance__tabs">
        <button 
          className={`finance__tab ${activeTab === 'real' ? 'finance__tab--active' : ''}`}
          onClick={() => setActiveTab('real')}
        >
          TIỀN THẬT (VND)
        </button>
        <button 
          className={`finance__tab ${activeTab === 'virtual' ? 'finance__tab--active' : ''}`}
          onClick={() => setActiveTab('virtual')}
        >
          TÀI SẢN ẢO (GOLD/GEM)
        </button>
      </div>

      <div className="finance__content animate-scaleIn">
        {activeTab === 'real' ? (
          <>
            {/* Real World Dashboard */}
            <div className="finance__summary">
              <div className="game-card finance__card">
                <div className="finance__card-label">TỔNG TÀI SẢN</div>
                <div className="finance__card-value glow-text--blue font-mono">
                  {totalBalance.toLocaleString()} ₫
                </div>
              </div>
              <div className="game-card finance__card">
                <div className="finance__card-label">CHI TIÊU THÁNG</div>
                <div className="finance__card-value text-danger font-mono">
                  -{spentThisMonth.toLocaleString()} ₫
                </div>
              </div>
              <div className="game-card finance__card finance__card--budget">
                <div className="finance__card-label">NGÂN SÁCH CÒN LẠI</div>
                <div className="finance__card-value text-success font-mono">
                  {(budget - spentThisMonth).toLocaleString()} ₫
                </div>
                <div className="progress-bar" style={{ marginTop: 'var(--space-2)' }}>
                  <div 
                    className="progress-bar__fill" 
                    style={{ 
                      width: `${budgetPercent}%`, 
                      background: budgetPercent > 80 ? 'var(--system-red)' : 'var(--system-green)' 
                    }} 
                  />
                </div>
              </div>
            </div>

            <div className="finance__history game-card">
              <div className="finance__history-header">
                <h3 className="font-heading">Lịch sử giao dịch</h3>
                <button className="btn btn-primary btn-sm">+ THÊM GIAO DỊCH</button>
              </div>
              
              <div className="finance__list">
                {DEMO_TRANSACTIONS.filter(t => t.category !== 'System').map((tx) => (
                  <div key={tx.id} className="finance-item">
                    <div className="finance-item__icon">
                      {tx.amount > 0 ? '📈' : '📉'}
                    </div>
                    <div className="finance-item__info">
                      <div className="finance-item__desc">{tx.desc}</div>
                      <div className="finance-item__meta">
                        {tx.category} • {new Date(tx.date).toLocaleDateString('vi-VN')}
                      </div>
                    </div>
                    <div className={`finance-item__amount font-mono ${tx.amount > 0 ? 'text-success' : 'text-danger'}`}>
                      {tx.amount > 0 ? '+' : ''}{tx.amount.toLocaleString()} ₫
                    </div>
                  </div>
                ))}
              </div>
            </div>
          </>
        ) : (
          <>
            {/* Virtual Currency Dashboard */}
            <div className="finance__summary" style={{ gridTemplateColumns: '1fr 1fr' }}>
              <div className="game-card finance__card finance__card--gold">
                <div className="finance__card-label">GOLD</div>
                <div className="finance__card-value glow-text--gold font-mono">
                  12,500 🪙
                </div>
                <div style={{ fontSize: 'var(--text-xs)', color: 'var(--text-muted)', marginTop: 'var(--space-2)' }}>
                  Dùng để mua vật phẩm hỗ trợ trong Shop hệ thống.
                </div>
              </div>
              <div className="game-card finance__card finance__card--gem">
                <div className="finance__card-label">GEMS</div>
                <div className="finance__card-value glow-text--purple font-mono">
                  150 💎
                </div>
                <div style={{ fontSize: 'var(--text-xs)', color: 'var(--text-muted)', marginTop: 'var(--space-2)' }}>
                  Dùng để mở rộng kho đồ hoặc mua vé xoá phạt cao cấp.
                </div>
              </div>
            </div>

            <div className="finance__history game-card">
              <div className="finance__history-header">
                <h3 className="font-heading">Lịch sử nhận thưởng</h3>
                <button className="btn btn-secondary btn-sm" disabled>SHOP HỆ THỐNG (Khóa)</button>
              </div>
              
              <div className="finance__list">
                {DEMO_TRANSACTIONS.filter(t => t.category === 'System').map((tx) => (
                  <div key={tx.id} className="finance-item">
                    <div className="finance-item__icon">🎁</div>
                    <div className="finance-item__info">
                      <div className="finance-item__desc">{tx.desc}</div>
                      <div className="finance-item__meta">
                        Hệ thống • {new Date(tx.date).toLocaleDateString('vi-VN')}
                      </div>
                    </div>
                    <div className="finance-item__amount font-mono text-warning">
                      +{tx.amount} 🪙
                    </div>
                  </div>
                ))}
              </div>
            </div>
          </>
        )}
      </div>
    </div>
  );
}
