import React, { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import api from '../services/api'
import { useCart } from '../context/CartContext'
import { useAuth } from '../context/AuthContext'

export default function Checkout() {
  const { cart, clearCart } = useCart()
  const { user } = useAuth()
  const navigate = useNavigate()

  const [addresses, setAddresses] = useState([])
  const [selectedAddress, setSelectedAddress] = useState('')
  const [newAddress, setNewAddress] = useState({ fullName: '', line1: '', line2: '', city: '', postcode: '', country: '' })
  const [showNewAddr, setShowNewAddr] = useState(false)
  const [giftPoints, setGiftPoints] = useState(null)
  const [usePoints, setUsePoints] = useState(false)
  const [paymentMethod, setPaymentMethod] = useState('CREDIT_CARD')
  const [cardNumber, setCardNumber] = useState('')
  const [cardHolder, setCardHolder] = useState('')
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')

  useEffect(() => {
    if (!user) { navigate('/login'); return }
    api.get('/addresses').then(r => {
      setAddresses(r.data)
      if (r.data.length > 0) setSelectedAddress(r.data[0].id)
    })
    api.get('/gift-points').then(r => setGiftPoints(r.data))
  }, [])

  const handleSaveAddress = async () => {
    const { data } = await api.post('/addresses', newAddress)
    setAddresses(prev => [...prev, data])
    setSelectedAddress(data.id)
    setShowNewAddr(false)
  }

  const discount = usePoints && giftPoints ? Math.min(giftPoints.equivalentValue, cart.subtotal) : 0
  const finalTotal = (cart.subtotal - discount).toFixed(2)

  const handlePlaceOrder = async () => {
    if (!selectedAddress) { setError('Please select a delivery address.'); return }
    if (paymentMethod !== 'GIFT_POINTS' && !cardNumber) { setError('Please enter card number.'); return }
    setLoading(true); setError('')
    try {
      const { data } = await api.post('/orders', {
        addressId: selectedAddress,
        paymentMethod,
        useGiftPoints: usePoints,
        cardNumber: cardNumber.slice(-4),
        cardHolderName: cardHolder,
      })
      clearCart()
      navigate(`/orders/confirmation/${data.id}`)
    } catch (e) {
      setError(e.response?.data?.message || 'Failed to place order.')
    } finally {
      setLoading(false)
    }
  }

  if (!user || cart.items.length === 0) {
    navigate('/cart'); return null
  }

  return (
    <div className="page">
      <h1 style={{ fontSize: 28, fontWeight: 700, marginBottom: 24 }}>Checkout</h1>
      <div style={styles.layout}>
        <div style={{ display: 'flex', flexDirection: 'column', gap: 24 }}>

          {/* Address */}
          <div className="card" style={{ padding: 24 }}>
            <h3 style={styles.sectionHead}>📍 Delivery Address</h3>
            {addresses.map(a => (
              <label key={a.id} style={styles.addrOption}>
                <input type="radio" name="address" value={a.id}
                  checked={selectedAddress === a.id}
                  onChange={() => setSelectedAddress(a.id)} />
                <span><strong>{a.fullName}</strong>, {a.line1}{a.line2 ? ', ' + a.line2 : ''}, {a.city} {a.postcode}, {a.country}</span>
              </label>
            ))}
            <button className="btn-outline" style={{ marginTop: 12, fontSize: 13 }}
              onClick={() => setShowNewAddr(!showNewAddr)}>
              + Add New Address
            </button>
            {showNewAddr && (
              <div style={{ marginTop: 16, display: 'flex', flexDirection: 'column', gap: 10 }}>
                {['fullName', 'line1', 'line2', 'city', 'postcode', 'country'].map(f => (
                  <input key={f} placeholder={f.charAt(0).toUpperCase() + f.slice(1)}
                    value={newAddress[f]}
                    onChange={e => setNewAddress(p => ({ ...p, [f]: e.target.value }))} />
                ))}
                <button className="btn-primary" onClick={handleSaveAddress}>Save Address</button>
              </div>
            )}
          </div>

          {/* Gift Points */}
          {giftPoints && giftPoints.balance > 0 && (
            <div className="card" style={{ padding: 24 }}>
              <h3 style={styles.sectionHead}>🎁 Gift Points</h3>
              <label style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
                <input type="checkbox" checked={usePoints} onChange={e => setUsePoints(e.target.checked)} />
                Use {giftPoints.balance} points (worth ${giftPoints.equivalentValue?.toFixed(2)})
              </label>
            </div>
          )}

          {/* Payment */}
          <div className="card" style={{ padding: 24 }}>
            <h3 style={styles.sectionHead}>💳 Payment Method</h3>
            {['CREDIT_CARD', 'DEBIT_CARD', 'GIFT_POINTS'].map(m => (
              <label key={m} style={styles.addrOption}>
                <input type="radio" name="payment" value={m}
                  checked={paymentMethod === m}
                  onChange={() => setPaymentMethod(m)} />
                {m.replace('_', ' ')}
              </label>
            ))}
            {paymentMethod !== 'GIFT_POINTS' && (
              <div style={{ marginTop: 16, display: 'flex', flexDirection: 'column', gap: 10 }}>
                <input placeholder="Card Number (mock — no real data stored)"
                  value={cardNumber} onChange={e => setCardNumber(e.target.value)} maxLength={19} />
                <input placeholder="Cardholder Name"
                  value={cardHolder} onChange={e => setCardHolder(e.target.value)} />
              </div>
            )}
          </div>
        </div>

        {/* Order Summary */}
        <div className="card" style={{ padding: 24, alignSelf: 'start', position: 'sticky', top: 80 }}>
          <h3 style={styles.sectionHead}>Order Summary</h3>
          {cart.items.map(i => (
            <div key={i.id} style={{ display: 'flex', justifyContent: 'space-between', padding: '6px 0', fontSize: 14 }}>
              <span>{i.product?.title} × {i.quantity}</span>
              <span>${i.subtotal?.toFixed(2)}</span>
            </div>
          ))}
          <div style={styles.divider} />
          <div style={styles.summaryRow}><span>Subtotal</span><span>${cart.subtotal?.toFixed(2)}</span></div>
          {discount > 0 && <div style={{ ...styles.summaryRow, color: '#2d7d46' }}><span>Gift Points</span><span>-${discount.toFixed(2)}</span></div>}
          <div style={{ ...styles.summaryRow, fontWeight: 800, fontSize: 18 }}><span>Total</span><span>${finalTotal}</span></div>
          {error && <p style={{ color: '#e53e3e', fontSize: 13, marginTop: 8 }}>{error}</p>}
          <button className="btn-primary" onClick={handlePlaceOrder} disabled={loading}
            style={{ width: '100%', marginTop: 16, padding: '14px', fontSize: 16 }}>
            {loading ? 'Placing Order…' : 'Place Order'}
          </button>
        </div>
      </div>
    </div>
  )
}

const styles = {
  layout: { display: 'grid', gridTemplateColumns: '1fr 320px', gap: 32 },
  sectionHead: { fontSize: 16, fontWeight: 700, marginBottom: 16 },
  addrOption: { display: 'flex', alignItems: 'center', gap: 10, padding: '10px 0', borderBottom: '1px solid #f0f0f0', cursor: 'pointer' },
  divider: { borderTop: '1px solid #e5e7eb', margin: '12px 0' },
  summaryRow: { display: 'flex', justifyContent: 'space-between', padding: '6px 0' },
}
