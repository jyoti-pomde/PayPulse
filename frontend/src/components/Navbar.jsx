import React from 'react';
import { CreditCard, Activity, ShieldCheck, Zap } from 'lucide-react';

export default function Navbar({ wallet, chaosStatus, onOpenAudit, auditCount }) {
  const isHealthy = chaosStatus?.circuitBreakerState === 'CLOSED' && !chaosStatus?.simulatedGatewayDown;

  return (
    <header className="navbar">
      <div className="navbar-container">
        {/* Brand */}
        <div className="nav-brand">
          <div className="brand-icon">
            <Zap size={22} color="#10B981" />
          </div>
          <div>
            <div className="brand-title">
              <span>Pay</span><span className="accent-text">Pulse</span>
            </div>
            <div className="brand-subtitle">Distributed Event-Driven Payment Engine</div>
          </div>
        </div>

        {/* Center: Circuit Breaker Live Indicator */}
        <div className="circuit-badge" title={`Resilience4j Circuit Breaker: ${chaosStatus?.circuitBreakerState || 'CLOSED'}`}>
          <span className={`status-dot ${isHealthy ? 'dot-green' : 'dot-red'}`}></span>
          <span className="circuit-label">Circuit Breaker:</span>
          <strong className={isHealthy ? 'text-emerald' : 'text-rose'}>
            {chaosStatus?.circuitBreakerState || 'CLOSED'}
          </strong>
        </div>

        {/* Right Actions */}
        <div className="nav-actions">
          {/* Wallet Balance Badge */}
          <div className="wallet-nav-badge">
            <CreditCard size={16} className="text-emerald" />
            <span className="wallet-balance-text">₹{wallet?.balance ? Number(wallet.balance).toLocaleString('en-IN', { minimumFractionDigits: 2 }) : '0.00'}</span>
          </div>

          {/* Audit Logs Drawer Button */}
          <button className="btn btn-secondary" onClick={onOpenAudit}>
            <Activity size={16} />
            <span>Audit Trail</span>
            {auditCount > 0 && <span className="badge-count">{auditCount}</span>}
          </button>

          {/* User Profile */}
          <div className="user-profile">
            <div className="user-avatar" title="Jyoti Pomde">JP</div>
            <div className="user-details">
              <div className="user-name">Jyoti Pomde</div>
              <div className="user-role">
                <ShieldCheck size={12} />
                <span>FINTECH LEAD</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </header>
  );
}
