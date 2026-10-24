import React, { useEffect } from 'react';
import { CheckCircle, XCircle, AlertCircle, Loader2, ArrowRight, X } from 'lucide-react';
import confetti from 'canvas-confetti';

export default function OrderProcessingModal({ isOpen, onClose, currentOrder, processingStep, errorMsg }) {
  if (!isOpen) return null;

  useEffect(() => {
    if (processingStep === 3 && currentOrder?.status === 'CONFIRMED') {
      confetti({
        particleCount: 80,
        spread: 60,
        origin: { y: 0.6 }
      });
    }
  }, [processingStep, currentOrder]);

  return (
    <div className="modal-backdrop" onClick={onClose}>
      <div className="modal-dialog order-modal" onClick={(e) => e.stopPropagation()}>
        <div className="modal-header">
          <div className="modal-title-group">
            <h3>Event-Driven Order Execution</h3>
            <span className="modal-sub">Distributed saga workflow in real-time</span>
          </div>
          <button className="close-btn" onClick={onClose}><X size={18} /></button>
        </div>

        <div className="modal-body">
          {/* Order Details Header */}
          {currentOrder && (
            <div className="order-summary-box">
              <div className="summary-row">
                <span className="summary-label">Order Number:</span>
                <span className="summary-val font-mono">{currentOrder.orderNumber}</span>
              </div>
              <div className="summary-row">
                <span className="summary-label">Item:</span>
                <span className="summary-val">{currentOrder.productName} (x{currentOrder.quantity})</span>
              </div>
              <div className="summary-row">
                <span className="summary-label">Total Amount:</span>
                <span className="summary-val text-emerald font-semibold">₹{Number(currentOrder.amount).toLocaleString('en-IN', { minimumFractionDigits: 2 })}</span>
              </div>
            </div>
          )}

          {/* Stepper Timeline */}
          <div className="stepper-timeline">
            {/* Step 1 */}
            <div className={`step-item ${processingStep >= 1 ? 'step-active' : ''} ${processingStep > 1 ? 'step-done' : ''}`}>
              <div className="step-circle">
                {processingStep > 1 ? <CheckCircle size={16} /> : <span>1</span>}
              </div>
              <div className="step-info">
                <strong>Step 1: Order Created & Event Dispatched</strong>
                <p>Order Service saved state as PENDING and emitted <code>OrderPlacedEvent</code>.</p>
              </div>
            </div>

            {/* Step 2 */}
            <div className={`step-item ${processingStep >= 2 ? 'step-active' : ''} ${processingStep > 2 ? 'step-done' : ''}`}>
              <div className="step-circle">
                {processingStep === 2 ? <Loader2 size={16} className="spin-icon" /> : (processingStep > 2 ? <CheckCircle size={16} /> : <span>2</span>)}
              </div>
              <div className="step-info">
                <strong>Step 2: Idempotency Check & Wallet Deduction</strong>
                <p>Payment Service verified Idempotency-Key and checked wallet balance.</p>
              </div>
            </div>

            {/* Step 3 */}
            <div className={`step-item ${processingStep >= 3 ? 'step-active' : ''}`}>
              <div className="step-circle">
                {processingStep === 3 ? (
                  currentOrder?.status === 'CONFIRMED' ? (
                    <CheckCircle size={16} className="text-emerald" />
                  ) : (
                    <XCircle size={16} className="text-rose" />
                  )
                ) : (
                  <span>3</span>
                )}
              </div>
              <div className="step-info">
                <strong>
                  Step 3: {currentOrder?.status === 'CONFIRMED' ? 'Payment Completed & Order Confirmed' : (currentOrder?.status === 'FAILED' ? 'Payment Failed / Circuit Tripped' : 'Reconciliation Outcome')}
                </strong>
                <p>
                  {currentOrder?.status === 'CONFIRMED'
                    ? 'Payment transaction approved! Funds deducted, ledger entry created.'
                    : (currentOrder?.failureReason || errorMsg || 'Transaction could not be completed.')}
                </p>
              </div>
            </div>
          </div>

          {processingStep === 3 && (
            <div className="modal-actions">
              <button className="btn btn-primary btn-block" onClick={onClose}>
                Done & Return to Dashboard
              </button>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
