import React from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useCart } from '../context/CartContext'
import { useAuth } from '../context/AuthContext'

export default function Cart() {
  const { cart, removeItem, updateItem } = useCart()
  const { user } = useAuth()
  const navigate = useNavigate()

  if (!user) return (
    <div className="page" style={{ textAlign: 'center', paddingTop: 80 }}>
      <p style={{ fontSize: 18, marginBottom: 20 }}>Please log in to view your cart.</p>
      <Link to="/login"><button className="btn-primary">Login</button></Link>
    </div>
  )

  if (cart.items.length === 0) return (
    <div className="page" style={{ textAlign: 'center', paddingTop: 80 }}>
      <div style={{ fontSize: 64, marginBottom: 16 }}>🛒</div>
      <h2 style={{ marginBottom: 12 }}>Your cart is empty</h2>
      <Link to="/catalogue"><button className="btn-primary">Browse Books</button></Link>
    </div>
  )

  return (
    <div className="page">
      <h1 style={{ fontSize: 28, fontWeight: 700, marginBottom: 24 }}>Shopping Cart</h1>
      <div style={styles.layout}>
        {/* Items */}
        <div style={styles.items}>
          {cart.items.map(item => (
            <div key={item.id} style={styles.item}>
              <img
                src={item.product?.coverImageUrl || `https://picsum.photos/seed/${item.product?.id}/80/120`}
                alt={item.product?.title}
                style={styles.itemImg}
                onError={e => { e.target.src = `https://picsum.photos/seed/${item.product?.id}/80/120` }}
              />
              <div style={styles.itemInfo}>
                <Link to={`/products/${item.product?.id}`} style={{ fontWeight: 700, fontSize: 16 }}>
                  {item.product?.title}
                </Link>
                <p style={{ color: '#57606a', fontSize: 13 }}>{item.product?.author}</p>
                <p style={{ color: '#57606a', fontSize: 13 }}>Unit: ${item.unitPrice?.toFixed(2)}</p>
              </div>
              <div style={styles.itemControls}>
                <div style={styles.qtyWrap}>
                  <button style={styles.qtyBtn}
                    onClick={() => item.quantity > 1 ? updateItem(item.id, item.quantity - 1) : removeItem(item.id)}>−</button>
                  <span style={styles.qtyVal}>{item.quantity}</span>
                  <button style={styles.qtyBtn}
                    onClick={() => updateItem(item.id, item.quantity + 1)}>+</button>
                </div>
                <div style={styles.itemSubtotal}>${item.subtotal?.toFixed(2)}</div>
                <button className="btn-danger" onClick={() => removeItem(item.id)}
                  style={{ padding: '6px 12px', fontSize: 12 }}>Remove</button>
              </div>
            </div>
          ))}
        </div>

        {/* Summary */}
        <div style={styles.summary} className="card">
          <h3 style={{ fontSize: 18, fontWeight: 700, marginBottom: 16 }}>Order Summary</h3>
          <div style={styles.summaryRow}>
            <span>Items ({cart.totalItems})</span>
            <span>${cart.subtotal?.toFixed(2)}</span>
          </div>
          <div style={styles.summaryRow}>
            <span>Shipping</span>
            <span style={{ color: '#2d7d46' }}>FREE</span>
          </div>
          <div style={{ ...styles.summaryRow, fontWeight: 700, fontSize: 18, borderTop: '2px solid #e5e7eb', paddingTop: 12, marginTop: 8 }}>
            <span>Total</span>
            <span>${cart.total?.toFixed(2)}</span>
          </div>
          <button className="btn-primary" onClick={() => navigate('/checkout')}
            style={{ width: '100%', marginTop: 20, fontSize: 16, padding: '14px' }}>
            Proceed to Checkout
          </button>
          <Link to="/catalogue">
            <button className="btn-outline" style={{ width: '100%', marginTop: 10 }}>Continue Shopping</button>
          </Link>
        </div>
      </div>
    </div>
  )
}

const styles = {
  layout: { display: 'grid', gridTemplateColumns: '1fr 320px', gap: 32, alignItems: 'start' },
  items: { display: 'flex', flexDirection: 'column', gap: 16 },
  item: { display: 'flex', gap: 16, background: '#fff', border: '1px solid #e5e7eb', borderRadius: 10, padding: 16, alignItems: 'center' },
  itemImg: { width: 80, height: 110, objectFit: 'cover', borderRadius: 4 },
  itemInfo: { flex: 1 },
  itemControls: { display: 'flex', flexDirection: 'column', alignItems: 'flex-end', gap: 10 },
  qtyWrap: { display: 'flex', alignItems: 'center', border: '1px solid #e5e7eb', borderRadius: 6, overflow: 'hidden' },
  qtyBtn: { width: 32, height: 32, background: '#f7f8fa', border: 'none', fontSize: 16, cursor: 'pointer', borderRadius: 0 },
  qtyVal: { width: 36, textAlign: 'center', fontWeight: 700 },
  itemSubtotal: { fontSize: 18, fontWeight: 700 },
  summary: { padding: 24, position: 'sticky', top: 80 },
  summaryRow: { display: 'flex', justifyContent: 'space-between', padding: '8px 0' },
}
