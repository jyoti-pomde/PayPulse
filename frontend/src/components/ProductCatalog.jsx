import React from 'react';
import { ShoppingBag, ArrowRight, CheckCircle2 } from 'lucide-react';

export default function ProductCatalog({ products, onBuyProduct, purchasingId }) {
  return (
    <div className="catalog-section">
      <div className="section-header">
        <div className="section-title-wrapper">
          <ShoppingBag size={20} className="text-emerald" />
          <h2>Developer Hardware & Peripherals Catalog</h2>
        </div>
        <span className="section-subtitle">Click "Buy Now" to trigger the distributed Order & Payment saga</span>
      </div>

      <div className="product-grid">
        {products.map((product) => {
          const isPurchasing = purchasingId === product.id;

          return (
            <div key={product.id} className="product-card">
              <div className="product-image-container">
                <img src={product.imageUrl} alt={product.name} className="product-image" />
                <span className="product-category-badge">{product.category}</span>
              </div>

              <div className="product-content">
                <h3 className="product-title">{product.name}</h3>
                <p className="product-desc">{product.description}</p>

                <div className="product-footer">
                  <div className="product-price">
                    <span className="price-currency">₹</span>
                    <span className="price-value">{Number(product.price).toLocaleString('en-IN', { minimumFractionDigits: 2 })}</span>
                  </div>

                  <button
                    className="btn btn-primary buy-btn"
                    onClick={() => onBuyProduct(product)}
                    disabled={isPurchasing}
                  >
                    <span>{isPurchasing ? 'Processing...' : 'Buy Now'}</span>
                    <ArrowRight size={15} />
                  </button>
                </div>
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
}
