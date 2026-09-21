import React, { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'

export default function Login() {
  const { login } = useAuth()
  const navigate = useNavigate()
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  const handleSubmit = async e => {
    e.preventDefault(); setError(''); setLoading(true)
    try {
      await login(email, password)
      navigate('/')
    } catch (err) {
      setError(err.response?.data?.message || 'Login failed. Please check your credentials.')
    } finally { setLoading(false) }
  }

  return (
    <div style={styles.page}>
      <div style={styles.card}>
        <h1 style={styles.title}>Welcome Back</h1>
        <p style={styles.sub}>Sign in to your BookStore account</p>
        <form onSubmit={handleSubmit} style={styles.form}>
          <label style={styles.label}>Email</label>
          <input type="email" value={email} onChange={e => setEmail(e.target.value)} required placeholder="you@example.com" />
          <label style={{ ...styles.label, marginTop: 12 }}>Password</label>
          <input type="password" value={password} onChange={e => setPassword(e.target.value)} required placeholder="••••••••" />
          {error && <p style={styles.error}>{error}</p>}
          <button type="submit" className="btn-primary" disabled={loading} style={styles.submit}>
            {loading ? 'Signing in…' : 'Sign In'}
          </button>
        </form>
        <p style={styles.foot}>Don't have an account? <Link to="/register" style={{ color: '#3b82d4' }}>Register</Link></p>
      </div>
    </div>
  )
}

const styles = {
  page: { minHeight: 'calc(100vh - 60px)', display: 'flex', alignItems: 'center', justifyContent: 'center', padding: 16 },
  card: { background: '#fff', border: '1px solid #e5e7eb', borderRadius: 12, padding: 36, width: '100%', maxWidth: 420 },
  title: { fontSize: 26, fontWeight: 800, marginBottom: 6 },
  sub: { color: '#57606a', marginBottom: 24, fontSize: 14 },
  form: { display: 'flex', flexDirection: 'column', gap: 6 },
  label: { fontSize: 13, fontWeight: 600, color: '#374151' },
  error: { color: '#e53e3e', fontSize: 13, marginTop: 4 },
  submit: { marginTop: 16, padding: '12px', fontSize: 15, width: '100%' },
  foot: { textAlign: 'center', fontSize: 13, marginTop: 20, color: '#57606a' },
}
