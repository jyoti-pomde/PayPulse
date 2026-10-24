import React, { useState, useEffect, useCallback } from 'react';
import Navbar from './components/Navbar';
import WalletCard from './components/WalletCard';
import ChaosCircuitBreakerCard from './components/ChaosCircuitBreakerCard';
import ProductCatalog from './components/ProductCatalog';
import LiveOrderList from './components/LiveOrderList';
import OrderProcessingModal from './components/OrderProcessingModal';
import AuditFeedDrawer from './components/AuditFeedDrawer';
import api from './api/client';
import './App.css';

export default function App() {
  const [wallet, setWallet] = useState(null);
  const [products, setProducts] = useState([]);
  const [orders, setOrders] = useState([]);
  const [chaosStatus, setChaosStatus] = useState(null);
  const [auditLogs, setAuditLogs] = useState([]);

  // Modals & Drawers
  const [auditDrawerOpen, setAuditDrawerOpen] = useState(false);
  const [orderModalOpen, setOrderModalOpen] = useState(false);
  const [currentOrder, setCurrentOrder] = useState(null);
  const [processingStep, setProcessingStep] = useState(1);
  const [purchasingId, setPurchasingId] = useState(null);
  const [loading, setLoading] = useState(false);

  // Load all initial data
  const loadAll = useCallback(async () => {
    try {
      const [walletRes, productsRes, ordersRes, chaosRes, auditRes] = await Promise.all([
        api.get('/wallet'),
        api.get('/products'),
        api.get('/orders'),
        api.get('/chaos/status'),
        api.get('/audit/logs')
      ]);

      setWallet(walletRes.data);
      setProducts(productsRes.data || []);
      setOrders(ordersRes.data || []);
      setChaosStatus(chaosRes.data);
      setAuditLogs(auditRes.data || []);
    } catch (err) {
      console.warn('Initial data load pending backend initialization', err);
    }
  }, []);

  useEffect(() => {
    loadAll();
    const interval = setInterval(loadAll, 6000);
    return () => clearInterval(interval);
  }, [loadAll]);

  // Wallet Top-Up handler
  const handleTopup = async (amount) => {
    setLoading(true);
    try {
      const res = await api.post('/wallet/topup', {
        userEmail: 'jyoti@paypulse.io',
        amount: Number(amount)
      });
      setWallet(res.data);
      loadAll();
    } catch (err) {
      console.error('Failed to top up wallet', err);
    } finally {
      setLoading(false);
    }
  };

  // Chaos Toggle handler
  const handleToggleChaos = async (enable) => {
    try {
      const res = await api.post('/chaos/toggle', { enable });
      setChaosStatus(res.data);
      loadAll();
    } catch (err) {
      console.error('Failed to toggle chaos simulation', err);
    }
  };

  // Buy Product & Trigger Order Saga
  const handleBuyProduct = async (product) => {
    setPurchasingId(product.id);
    const idempotencyKey = 'IDEMP-' + Math.random().toString(36).substring(2, 9).toUpperCase();

    // Open modal at Step 1
    setCurrentOrder({
      orderNumber: 'ORD-PENDING...',
      productName: product.name,
      quantity: 1,
      amount: product.price,
      status: 'PENDING'
    });
    setProcessingStep(1);
    setOrderModalOpen(true);

    try {
      // Step 2: In Flight
      setTimeout(() => setProcessingStep(2), 400);

      const res = await api.post('/orders', {
        productId: product.id,
        quantity: 1,
        customerEmail: 'jyoti@paypulse.io',
        idempotencyKey
      });

      // Step 3: Completed
      setTimeout(() => {
        setProcessingStep(3);
        setCurrentOrder(res.data);
        loadAll();
      }, 900);
    } catch (err) {
      console.error('Order creation failed', err);
      setTimeout(() => {
        setProcessingStep(3);
        setCurrentOrder((prev) => ({
          ...prev,
          status: 'FAILED',
          failureReason: err.response?.data?.message || err.message
        }));
        loadAll();
      }, 900);
    } finally {
      setPurchasingId(null);
    }
  };

  return (
    <div className="paypulse-app">
      <Navbar
        wallet={wallet}
        chaosStatus={chaosStatus}
        onOpenAudit={() => setAuditDrawerOpen(true)}
        auditCount={auditLogs.length}
      />

      <main className="main-content">
        {/* Top Control Panels */}
        <section className="top-grid">
          <WalletCard
            wallet={wallet}
            onTopup={handleTopup}
            loading={loading}
          />

          <ChaosCircuitBreakerCard
            chaosStatus={chaosStatus}
            onToggleChaos={handleToggleChaos}
            onRefreshAll={loadAll}
          />
        </section>

        {/* Product Catalog */}
        <section className="catalog-wrapper">
          <ProductCatalog
            products={products}
            onBuyProduct={handleBuyProduct}
            purchasingId={purchasingId}
          />
        </section>

        {/* Live Orders Table */}
        <section className="orders-wrapper">
          <LiveOrderList
            orders={orders}
            onRefresh={loadAll}
          />
        </section>
      </main>

      {/* Stepper Modal */}
      <OrderProcessingModal
        isOpen={orderModalOpen}
        onClose={() => setOrderModalOpen(false)}
        currentOrder={currentOrder}
        processingStep={processingStep}
      />

      {/* Observability Drawer */}
      <AuditFeedDrawer
        isOpen={auditDrawerOpen}
        onClose={() => setAuditDrawerOpen(false)}
        auditLogs={auditLogs}
        onRefresh={loadAll}
      />
    </div>
  );
}
