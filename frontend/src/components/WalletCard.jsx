import React, { useState } from 'react';
import { Wallet as WalletIcon, ArrowUpRight, Plus, Sparkles, Check } from 'lucide-react';

export default function WalletCard({ wallet, onTopup, loading }) {
  const [customAmount, setCustomAmount] = useState('');
  const [successMsg, setSuccessMsg] = useState('');

  const handleQuickTopup = async (amount) => {
    await onTopup(amount);
    showSuccess(`Added ₹${amount} to wallet`);
  };

  const handleCustomSubmit = async (e) => {
    e.preventDefault();
    if (!customAmount || Number(customAmount) <= 0) return;
    await onTopup(Number(customAmount));
    showSuccess(`Added ₹${customAmount} to wallet`);
    setCustomAmount('');
  };

  const showSuccess = (msg) => {
    setSuccessMsg(msg);
    setTimeout(() => setSuccessMsg(''), 3000);
  };

  return (
    <div className="card wallet-card">
      <div className="wallet-header">
        <div className="wallet-title-group">
          <div className="wallet-icon-box">
            <WalletIcon size={22} className="text-emerald" />
          </div>
          <div>
            <h3>Enterprise Customer Wallet</h3>
            <span className="wallet-subtext">{wallet?.userEmail || 'jyoti@paypulse.io'}</span>
          </div>
        </div>
        <span className="wallet-tag">IDEMPOTENT LEDGER</span>
      </div>

      <div className="balance-display">
        <span className="currency-symbol">₹</span>
        <span className="balance-amount">
          {wallet?.balance ? Number(wallet.balance).toLocaleString('en-IN', { minimumFractionDigits: 2 }) : '0.00'}
        </span>
        <span className="balance-currency">INR Available</span>
      </div>

      {successMsg && (
        <div className="topup-success-banner">
          <Check size={14} />
          <span>{successMsg}</span>
        </div>
      )}

      {/* Quick Top-Up Buttons */}
      <div className="topup-section">
        <span className="section-label">1-Click Fast Top-Up:</span>
        <div className="quick-amount-buttons">
          <button
            className="quick-btn"
            onClick={() => handleQuickTopup(500)}
            disabled={loading}
          >
            +₹500
          </button>
          <button
            className="quick-btn"
            onClick={() => handleQuickTopup(1000)}
            disabled={loading}
          >
            +₹1,000
          </button>
          <button
            className="quick-btn"
            onClick={() => handleQuickTopup(2500)}
            disabled={loading}
          >
            +₹2,500
          </button>
        </div>
      </div>

      {/* Custom Topup Form */}
      <form onSubmit={handleCustomSubmit} className="custom-topup-form">
        <input
          type="number"
          min="1"
          placeholder="Custom Amount (₹)"
          value={customAmount}
          onChange={(e) => setCustomAmount(e.target.value)}
          className="topup-input"
        />
        <button type="submit" className="btn btn-primary topup-submit-btn" disabled={loading}>
          <Plus size={16} />
          <span>Top Up</span>
        </button>
      </form>
    </div>
  );
}
