import React, { useEffect, useState } from 'react'
import { useParams, Link } from 'react-router-dom'
import api from '../services/api'

export default function OrderConfirmation() {
  const { id } = useParams()
  const [order, setOrder] = useState(null)

  useEffect(() => { api.get(`/orders/${id}`).then(r => setOrder(r.data)) }, [id])

  if (!order) return <div className="spinner">Loading…</div>

  return (
    <div className="page" style={{ maxWidth: 680, margin: '0 auto' }}>
      {/* Banner */}
      <div style={styles.banner}>
        <div style={styles.checkCircle}>✓</div>
        <h1 style={styles.bannerTitle}>Order Confirmed!</h1>
        <p style={styles.bannerSub}>Thank you for your purchase. Your order #{order.id} has been placed.</p>
      </div>

      {/* Details card */}
      <div className="card" style={{ padding: 28, marginTop: 24 }}>
        <div style={styles.row}>
          <span style={styles.label}>Order #</span>
          <span style={styles.value}>{order.id}</span>
        </div>
        <div style={styles.row}>
          <span style={styles.label}>Status</span>
          <span className="badge">{order.status}</span>
        </div>
        <div style={styles.row}>
          <span style={styles.label}>Date</span>
          <span style={styles.value}>{new Date(order.placedAt).toLocaleString()}</span>
        </div>
        <div style={styles.row}>
          <span style={styles.label}>Payment</span>
          <span style={styles.value}>{order.paymentMethod?.replace('_', ' ')}</span>
        </div>
        <div style={styles.row}>
          <span style={styles.label}>Deliver to</span>
          <span style={styles.value}>
            {order.address?.fullName}, {order.address?.line1}, {order.address?.city} {order.address?.postcode}
          </span>
        </div>

        <hr style={{ margin: '20px 0', border: 'none', borderTop: '1px solid #e5e7eb' }} />

        <h3 style={{ fontWeight: 700, marginBottom: 12 }}>Items Ordered</h3>
        {order.items?.map(i => (
          <div key={i.id} style={styles.item}>
            <span>{i.productTitle} × {i.quantity}</span>
            <span>${i.subtotal?.toFixed(2)}</span>
          </div>
        ))}

        <hr style={{ margin: '16px 0', border: 'none', borderTop: '1px solid #e5e7eb' }} />
        {order.discountFromPoints > 0 && (
          <div style={{ ...styles.item, color: '#2d7d46' }}>
            <span>Gift Points Discount</span>
            <span>-${order.discountFromPoints?.toFixed(2)}</span>
          </div>
        )}
        <div style={{ ...styles.item, fontWeight: 800, fontSize: 18 }}>
          <span>Total Paid</span>
          <span>${order.total?.toFixed(2)}</span>
        </div>

        {order.cancellable && (
          <p style={{ marginTop: 16, fontSize: 13, color: '#57606a' }}>
            ℹ You can cancel this order within 48 hours from your <Link to="/orders" style={{ color: '#3b82d4' }}>Order History</Link>.
          </p>
        )}
      </div>

      <div style={{ display: 'flex', gap: 16, marginTop: 24 }}>
        <Link to="/catalogue"><button className="btn-outline">Continue Shopping</button></Link>
        <Link to="/orders"><button className="btn-primary">View All Orders</button></Link>
      </div>
    </div>
  )
}

const styles = {
  banner: { background: '#f0fdf4', border: '1px solid #bbf7d0', borderRadius: 12, padding: '32px 24px', textAlign: 'center' },
  checkCircle: { width: 60, height: 60, borderRadius: '50%', background: '#22c55e', color: '#fff', fontSize: 28, display: 'flex', alignItems: 'center', justifyContent: 'center', margin: '0 auto 16px' },
  bannerTitle: { fontSize: 28, fontWeight: 800, color: '#166534' },
  bannerSub: { color: '#166534', marginTop: 8 },
  row: { display: 'flex', justifyContent: 'space-between', alignItems: 'center', padding: '10px 0', borderBottom: '1px solid #f0f0f0' },
  label: { color: '#57606a', fontSize: 13, fontWeight: 600 },
  value: { fontWeight: 500 },
  item: { display: 'flex', justifyContent: 'space-between', padding: '8px 0', fontSize: 15 },
}
