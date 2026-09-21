import React, { useEffect, useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import api from '../services/api'
import { useAuth } from '../context/AuthContext'

export default function OrderHistory() {
  const { user } = useAuth()
  const navigate = useNavigate()
  const [orders, setOrders] = useState([])
  const [loading, setLoading] = useState(true)
  const [cancelling, setCancelling] = useState(null)

  useEffect(() => {
    if (!user) { navigate('/login'); return }
    api.get('/orders').then(r => setOrders(r.data.content || [])).finally(() => setLoading(false))
  }, [])

  const handleCancel = async (id) => {
    if (!window.confirm('Cancel this order?')) return
    setCancelling(id)
    try {
      const { data } = await api.post(`/orders/${id}/cancel`)
      setOrders(prev => prev.map(o => o.id === id ? data : o))
    } catch (e) {
      alert(e.response?.data?.message || 'Cannot cancel this order.')
    } finally {
      setCancelling(null)
    }
  }

  if (loading) return <div className="spinner">Loading…</div>

  return (
    <div className="page">
      <h1 style={{ fontSize: 28, fontWeight: 700, marginBottom: 24 }}>Order History</h1>

      {orders.length === 0 ? (
        <div style={{ textAlign: 'center', padding: 80 }}>
          <p style={{ fontSize: 18, marginBottom: 20, color: '#57606a' }}>You haven't placed any orders yet.</p>
          <Link to="/catalogue"><button className="btn-primary">Start Shopping</button></Link>
        </div>
      ) : (
        <div style={{ display: 'flex', flexDirection: 'column', gap: 20 }}>
          {orders.map(order => (
            <div key={order.id} className="card" style={{ padding: 24 }}>
              <div style={styles.orderHeader}>
                <div>
                  <span style={{ fontWeight: 700, fontSize: 16 }}>Order #{order.id}</span>
                  <span style={{ color: '#57606a', fontSize: 13, marginLeft: 12 }}>
                    {new Date(order.placedAt).toLocaleDateString()}
                  </span>
                </div>
                <div style={{ display: 'flex', alignItems: 'center', gap: 12 }}>
                  <span className="badge" style={statusColor(order.status)}>{order.status}</span>
                  <span style={{ fontWeight: 700, fontSize: 18 }}>${order.total?.toFixed(2)}</span>
                </div>
              </div>

              <div style={styles.items}>
                {order.items?.map(i => (
                  <div key={i.id} style={styles.itemRow}>
                    <span>{i.productTitle}</span>
                    <span style={{ color: '#57606a' }}>× {i.quantity} — ${i.subtotal?.toFixed(2)}</span>
                    {i.productId && (
                      <Link to={`/products/${i.productId}`}>
                        <button className="btn-outline" style={{ padding: '4px 10px', fontSize: 12 }}>
                          Buy Again
                        </button>
                      </Link>
                    )}
                  </div>
                ))}
              </div>

              {order.cancellable && (
                <button className="btn-danger"
                  onClick={() => handleCancel(order.id)}
                  disabled={cancelling === order.id}
                  style={{ marginTop: 16, padding: '8px 16px', fontSize: 13 }}>
                  {cancelling === order.id ? 'Cancelling…' : 'Cancel Order'}
                </button>
              )}
            </div>
          ))}
        </div>
      )}
    </div>
  )
}

function statusColor(status) {
  const map = {
    CONFIRMED: { background: '#dbeafe', color: '#1d4ed8' },
    SHIPPED:   { background: '#fef9c3', color: '#854d0e' },
    DELIVERED: { background: '#dcfce7', color: '#166534' },
    CANCELLED: { background: '#fee2e2', color: '#991b1b' },
    PENDING:   { background: '#f3f4f6', color: '#374151' },
  }
  return map[status] || {}
}

const styles = {
  orderHeader: { display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 16 },
  items: { display: 'flex', flexDirection: 'column', gap: 8 },
  itemRow: { display: 'flex', alignItems: 'center', gap: 12, padding: '8px 0', borderBottom: '1px solid #f0f0f0', fontSize: 14 },
}
