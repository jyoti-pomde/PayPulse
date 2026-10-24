import React, { useState } from 'react';
import { AlertTriangle, ShieldAlert, RefreshCw, Zap, CopyCheck, Terminal } from 'lucide-react';
import api from '../api/client';

export default function ChaosCircuitBreakerCard({ chaosStatus, onToggleChaos, onRefreshAll }) {
  const [testingIdempotency, setTestingIdempotency] = useState(false);
  const [testLogs, setTestLogs] = useState([]);

  const isSimulatedDown = chaosStatus?.simulatedGatewayDown;
  const cbState = chaosStatus?.circuitBreakerState || 'CLOSED';

  const runIdempotencyRapidFireTest = async () => {
    setTestingIdempotency(true);
    setTestLogs([]);

    const idempotencyKey = 'IDEMP-TEST-' + Math.random().toString(36).substring(2, 9).toUpperCase();
    const orderId = 999;
    const amount = 500.00;

    const logEntry = (msg, type) => {
      setTestLogs((prev) => [...prev, { text: msg, type, time: new Date().toLocaleTimeString() }]);
    };

    logEntry(`Generating Idempotency-Key: ${idempotencyKey}`, 'info');
    logEntry(`Firing Request #1 (Amount: ₹${amount})...`, 'info');

    try {
      // First request
      const res1 = await api.post('/payments/process', {
        orderId,
        userEmail: 'jyoti@paypulse.io',
        amount,
        idempotencyKey
      });

      logEntry(`Req #1 Response: Status ${res1.data.status} | Txn: ${res1.data.transactionId} | Replay: ${res1.data.idempotentReplay}`, 'success');

      logEntry(`Firing Request #2 immediately with SAME Idempotency-Key...`, 'info');

      // Second identical request
      const res2 = await api.post('/payments/process', {
        orderId,
        userEmail: 'jyoti@paypulse.io',
        amount,
        idempotencyKey
      });

      if (res2.data.idempotentReplay) {
        logEntry(`Req #2 Result: 🛡️ IDEMPOTENT REPLAY DETECTED! Returned previous Txn without double-charging!`, 'highlight');
      } else {
        logEntry(`Req #2 Response: ${res2.data.status}`, 'warning');
      }

      onRefreshAll();
    } catch (err) {
      logEntry(`Test error: ${err.response?.data?.message || err.message}`, 'error');
    } finally {
      setTestingIdempotency(false);
    }
  };

  return (
    <div className="card chaos-card">
      <div className="chaos-header">
        <div className="chaos-title-group">
          <div className="chaos-icon-box">
            <ShieldAlert size={22} className={isSimulatedDown ? 'text-rose' : 'text-amber'} />
          </div>
          <div>
            <h3>Chaos Engineering & Circuit Breaker</h3>
            <span className="chaos-subtext">Resilience4j Fault Tolerance & FinTech Idempotency</span>
          </div>
        </div>
        <div className={`status-pill ${cbState === 'CLOSED' ? 'pill-green' : 'pill-red'}`}>
          {cbState}
        </div>
      </div>

      <div className="chaos-grid">
        {/* Gateway Outage Simulator */}
        <div className="chaos-feature-box">
          <div className="feature-label">
            <AlertTriangle size={16} className="text-amber" />
            <strong>Simulate Payment Gateway Failure</strong>
          </div>
          <p className="feature-desc">
            Simulates external bank gateway timeout. When tripped, Resilience4j transitions from <strong>CLOSED</strong> to <strong>OPEN</strong> and serves graceful fallback responses.
          </p>

          <button
            className={`btn toggle-chaos-btn ${isSimulatedDown ? 'btn-danger' : 'btn-warning'}`}
            onClick={() => onToggleChaos(!isSimulatedDown)}
          >
            <Zap size={16} />
            <span>{isSimulatedDown ? 'Restore Gateway (Reset to Normal)' : 'Trigger Simulated Gateway Outage'}</span>
          </button>
        </div>

        {/* Rapid-Fire Idempotency Simulator */}
        <div className="chaos-feature-box">
          <div className="feature-label">
            <CopyCheck size={16} className="text-emerald" />
            <strong>Double-Deduction Guard (Idempotency)</strong>
          </div>
          <p className="feature-desc">
            Sends two identical payment requests simultaneously with the same <code>Idempotency-Key</code> to verify that duplicate charges are blocked.
          </p>

          <button
            className="btn btn-secondary idempotency-test-btn"
            onClick={runIdempotencyRapidFireTest}
            disabled={testingIdempotency}
          >
            <RefreshCw size={16} className={testingIdempotency ? 'spin-icon' : ''} />
            <span>{testingIdempotency ? 'Simulating Rapid Requests...' : 'Test Rapid-Fire Duplicate Payment'}</span>
          </button>
        </div>
      </div>

      {/* Real-Time Test Output Console */}
      {testLogs.length > 0 && (
        <div className="test-console">
          <div className="console-header">
            <Terminal size={14} />
            <span>Live Idempotency Test Execution Logs</span>
          </div>
          <div className="console-body">
            {testLogs.map((log, index) => (
              <div key={index} className={`console-line log-${log.type}`}>
                <span className="log-time">[{log.time}]</span>
                <span className="log-text">{log.text}</span>
              </div>
            ))}
          </div>
        </div>
      )}
    </div>
  );
}
