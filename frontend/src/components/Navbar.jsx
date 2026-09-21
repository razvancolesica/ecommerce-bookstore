import React from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import { useCart } from '../context/CartContext'

export default function Navbar() {
  const { user, logout } = useAuth()
  const { cart } = useCart()
  const navigate = useNavigate()

  const handleLogout = () => { logout(); navigate('/') }

  return (
    <nav style={styles.nav}>
      <div style={styles.inner}>
        <Link to="/" style={styles.brand}>📚 BookStore</Link>
        <div style={styles.links}>
          <Link to="/catalogue" style={styles.link}>Catalogue</Link>
          {user && <Link to="/orders" style={styles.link}>My Orders</Link>}
        </div>
        <div style={styles.right}>
          <Link to="/cart" style={styles.cartBtn}>
            🛒 {cart.totalItems > 0 && <span style={styles.badge}>{cart.totalItems}</span>}
          </Link>
          {user ? (
            <div style={styles.userArea}>
              <span style={styles.userName}>Hi, {user.firstName}</span>
              <button onClick={handleLogout} style={styles.logoutBtn}>Logout</button>
            </div>
          ) : (
            <>
              <Link to="/login" style={styles.link}>Login</Link>
              <Link to="/register" style={{ ...styles.link, ...styles.registerBtn }}>Register</Link>
            </>
          )}
        </div>
      </div>
    </nav>
  )
}

const styles = {
  nav: { background: '#1f2328', color: '#fff', position: 'sticky', top: 0, zIndex: 100, boxShadow: '0 2px 8px rgba(0,0,0,0.2)' },
  inner: { maxWidth: 1200, margin: '0 auto', padding: '0 16px', height: 60, display: 'flex', alignItems: 'center', gap: 24 },
  brand: { fontSize: 22, fontWeight: 700, color: '#fff', flexShrink: 0 },
  links: { display: 'flex', gap: 20, flex: 1 },
  link: { color: '#c9d1d9', fontSize: 14, fontWeight: 500 },
  right: { display: 'flex', alignItems: 'center', gap: 16 },
  cartBtn: { fontSize: 22, color: '#fff', position: 'relative' },
  badge: { position: 'absolute', top: -6, right: -8, background: '#3b82d4', color: '#fff', borderRadius: '50%', width: 18, height: 18, fontSize: 11, display: 'flex', alignItems: 'center', justifyContent: 'center' },
  userArea: { display: 'flex', alignItems: 'center', gap: 10 },
  userName: { color: '#c9d1d9', fontSize: 14 },
  logoutBtn: { background: 'transparent', color: '#c9d1d9', border: '1px solid #444', padding: '4px 12px', fontSize: 13 },
  registerBtn: { background: '#3b82d4', color: '#fff', padding: '6px 14px', borderRadius: 6 },
}
