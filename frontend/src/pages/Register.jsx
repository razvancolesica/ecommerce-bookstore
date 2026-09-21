import React, { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'

export default function Register() {
  const { register } = useAuth()
  const navigate = useNavigate()
  const [form, setForm] = useState({ firstName: '', lastName: '', email: '', password: '' })
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  const set = k => e => setForm(p => ({ ...p, [k]: e.target.value }))

  const handleSubmit = async e => {
    e.preventDefault(); setError(''); setLoading(true)
    try {
      await register(form.firstName, form.lastName, form.email, form.password)
      navigate('/')
    } catch (err) {
      setError(err.response?.data?.message || 'Registration failed.')
    } finally { setLoading(false) }
  }

  return (
    <div style={styles.page}>
      <div style={styles.card}>
        <h1 style={styles.title}>Create Account</h1>
        <p style={styles.sub}>Join BookStore and get 100 welcome gift points!</p>
        <form onSubmit={handleSubmit} style={styles.form}>
          <div style={styles.nameRow}>
            <div style={{ flex: 1 }}>
              <label style={styles.label}>First Name</label>
              <input value={form.firstName} onChange={set('firstName')} required placeholder="Jane" />
            </div>
            <div style={{ flex: 1 }}>
              <label style={styles.label}>Last Name</label>
              <input value={form.lastName} onChange={set('lastName')} required placeholder="Doe" />
            </div>
          </div>
          <label style={styles.label}>Email</label>
          <input type="email" value={form.email} onChange={set('email')} required placeholder="you@example.com" />
          <label style={{ ...styles.label, marginTop: 8 }}>Password</label>
          <input type="password" value={form.password} onChange={set('password')} required minLength={6} placeholder="At least 6 characters" />
          {error && <p style={styles.error}>{error}</p>}
          <button type="submit" className="btn-primary" disabled={loading} style={styles.submit}>
            {loading ? 'Creating account…' : 'Create Account'}
          </button>
        </form>
        <p style={styles.foot}>Already have an account? <Link to="/login" style={{ color: '#3b82d4' }}>Sign in</Link></p>
      </div>
    </div>
  )
}

const styles = {
  page: { minHeight: 'calc(100vh - 60px)', display: 'flex', alignItems: 'center', justifyContent: 'center', padding: 16 },
  card: { background: '#fff', border: '1px solid #e5e7eb', borderRadius: 12, padding: 36, width: '100%', maxWidth: 440 },
  title: { fontSize: 26, fontWeight: 800, marginBottom: 6 },
  sub: { color: '#57606a', marginBottom: 24, fontSize: 14 },
  form: { display: 'flex', flexDirection: 'column', gap: 8 },
  nameRow: { display: 'flex', gap: 12 },
  label: { fontSize: 13, fontWeight: 600, color: '#374151', display: 'block', marginBottom: 4 },
  error: { color: '#e53e3e', fontSize: 13, marginTop: 4 },
  submit: { marginTop: 12, padding: '12px', fontSize: 15, width: '100%' },
  foot: { textAlign: 'center', fontSize: 13, marginTop: 20, color: '#57606a' },
}
