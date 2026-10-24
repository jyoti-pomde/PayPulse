import React, { useState } from 'react';
import { X, Activity, Clock, ShieldCheck, Terminal, Search, Filter } from 'lucide-react';

export default function AuditFeedDrawer({ isOpen, onClose, auditLogs, onRefresh }) {
  if (!isOpen) return null;

  const [filterQuery, setFilterQuery] = useState('');

  const filteredLogs = auditLogs.filter((log) => {
    if (!filterQuery) return true;
    const q = filterQuery.toLowerCase();
    return (
      (log.serviceName && log.serviceName.toLowerCase().includes(q)) ||
      (log.action && log.action.toLowerCase().includes(q)) ||
      (log.details && log.details.toLowerCase().includes(q)) ||
      (log.correlationId && log.correlationId.toLowerCase().includes(q))
    );
  });

  const getLevelBadgeClass = (level) => {
    switch (level) {
      case 'ERROR': return 'log-badge-error';
      case 'WARN': return 'log-badge-warn';
      case 'INFO':
      default: return 'log-badge-info';
    }
  };

  return (
    <div className="drawer-backdrop" onClick={onClose}>
      <div className="drawer-panel" onClick={(e) => e.stopPropagation()}>
        <div className="drawer-header">
          <div className="drawer-title-group">
            <Activity size={20} className="text-emerald" />
            <h3>Distributed Observability & Audit Feed</h3>
          </div>
          <button className="close-btn" onClick={onClose}><X size={18} /></button>
        </div>

        <p className="drawer-desc">
          Live stream of asynchronous event broadcasts, Resilience4j state transitions, and Correlation ID traces.
        </p>

        {/* Filter input */}
        <div className="drawer-search">
          <Search size={15} className="search-icon" />
          <input
            type="text"
            placeholder="Filter by Service, Action, or Correlation ID..."
            value={filterQuery}
            onChange={(e) => setFilterQuery(e.target.value)}
          />
        </div>

        <div className="audit-list">
          {filteredLogs.length === 0 ? (
            <div className="empty-audit">No audit records found.</div>
          ) : (
            filteredLogs.map((log) => (
              <div key={log.id} className="audit-item">
                <div className="audit-item-top">
                  <span className={`log-badge ${getLevelBadgeClass(log.level)}`}>{log.serviceName}</span>
                  <span className="audit-action-tag">{log.action}</span>
                  <span className="audit-time">
                    <Clock size={11} /> {log.timestamp ? new Date(log.timestamp).toLocaleTimeString() : 'Just now'}
                  </span>
                </div>

                <div className="audit-details">{log.details}</div>

                {log.correlationId && (
                  <div className="audit-correlation">
                    <span className="correlation-label">Correlation ID:</span>
                    <span className="correlation-val font-mono">{log.correlationId}</span>
                  </div>
                )}
              </div>
            ))
          )}
        </div>
      </div>
    </div>
  );
}
