import React from 'react';
import { Package, Clock, CheckCircle2, XCircle, AlertCircle, Hash } from 'lucide-react';

export default function LiveOrderList({ orders, onRefresh }) {
  const getStatusBadge = (status) => {
    switch (status) {
      case 'CONFIRMED':
        return (
          <span className="badge badge-success">
            <CheckCircle2 size={13} />
            <span>CONFIRMED</span>
          </span>
        );
      case 'FAILED':
        return (
          <span className="badge badge-danger">
            <XCircle size={13} />
            <span>FAILED</span>
          </span>
        );
      case 'PENDING':
      default:
        return (
          <span className="badge badge-warning">
            <Clock size={13} />
            <span>PENDING</span>
          </span>
        );
    }
  };

  return (
    <div className="card orders-card">
      <div className="orders-header">
        <div className="orders-title-group">
          <div className="orders-icon-box">
            <Package size={22} className="text-indigo" />
          </div>
          <div>
            <h3>Distributed Order Processing Ledger</h3>
            <span className="orders-subtext">Live order lifecycle state machine & reconciliation records</span>
          </div>
        </div>
        <span className="order-count-badge">{orders.length} Total Orders</span>
      </div>

      <div className="orders-table-wrapper">
        <table className="orders-table">
          <thead>
            <tr>
              <th>Order Ref</th>
              <th>Product</th>
              <th>Amount</th>
              <th>Status</th>
              <th>Idempotency Key</th>
              <th>Placed At</th>
            </tr>
          </thead>
          <tbody>
            {orders.length === 0 ? (
              <tr>
                <td colSpan="6" className="empty-table-cell">No orders placed yet. Choose a product above to test!</td>
              </tr>
            ) : (
              orders.map((ord) => (
                <tr key={ord.id} className="order-row">
                  <td className="font-mono text-emerald font-semibold">{ord.orderNumber}</td>
                  <td>
                    <div className="order-product-name">{ord.productName}</div>
                    <div className="order-qty">Qty: {ord.quantity}</div>
                  </td>
                  <td className="font-semibold text-white">
                    ₹{Number(ord.amount).toLocaleString('en-IN', { minimumFractionDigits: 2 })}
                  </td>
                  <td>
                    {getStatusBadge(ord.status)}
                    {ord.failureReason && (
                      <div className="failure-reason-hint" title={ord.failureReason}>
                        <AlertCircle size={12} />
                        <span>{ord.failureReason}</span>
                      </div>
                    )}
                  </td>
                  <td>
                    <span className="idemp-key-text font-mono" title={ord.idempotencyKey}>
                      <Hash size={11} /> {ord.idempotencyKey ? ord.idempotencyKey.substring(0, 16) + '...' : 'N/A'}
                    </span>
                  </td>
                  <td className="text-muted text-sm">
                    {ord.createdAt ? new Date(ord.createdAt).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit', second: '2-digit' }) : 'Just now'}
                  </td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
}
