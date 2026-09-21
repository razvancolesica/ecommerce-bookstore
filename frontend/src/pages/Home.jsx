import React, { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import api from '../services/api'
import ProductCard from '../components/ProductCard'

export default function Home() {
  const [featured, setFeatured] = useState([])
  const [categories, setCategories] = useState([])
  const [recommendations, setRecommendations] = useState([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    Promise.all([
      api.get('/products?size=8&sort=newest'),
      api.get('/categories'),
      api.get('/recommendations').catch(() => ({ data: [] })),
    ]).then(([prod, cats, recs]) => {
      setFeatured(prod.data.content || [])
      setCategories(cats.data || [])
      setRecommendations(recs.data || [])
    }).finally(() => setLoading(false))
  }, [])

  if (loading) return <div className="spinner">Loading…</div>

  return (
    <div>
      {/* Hero */}
      <div style={styles.hero}>
        <div style={styles.heroContent}>
          <h1 style={styles.heroTitle}>Your Next Great Read Awaits</h1>
          <p style={styles.heroSub}>Browse thousands of books across every genre, delivered fast.</p>
          <Link to="/catalogue">
            <button className="btn-primary" style={{ fontSize: 16, padding: '14px 32px' }}>
              Browse Catalogue
            </button>
          </Link>
        </div>
      </div>

      <div className="page">
        {/* Categories */}
        <section style={styles.section}>
          <h2 style={styles.sectionTitle}>Shop by Category</h2>
          <div style={styles.catGrid}>
            {categories.map(c => (
              <Link key={c.id} to={`/catalogue?categoryId=${c.id}`} style={styles.catCard}>
                <img src={c.imageUrl} alt={c.name} style={styles.catImg}
                  onError={e => { e.target.src = `https://picsum.photos/seed/${c.id}cat/400/200` }} />
                <div style={styles.catName}>{c.name}</div>
              </Link>
            ))}
          </div>
        </section>

        {/* Featured */}
        <section style={styles.section}>
          <h2 style={styles.sectionTitle}>New Arrivals</h2>
          <div className="grid-4">
            {featured.map(p => <ProductCard key={p.id} product={p} />)}
          </div>
        </section>

        {/* Recommendations */}
        {recommendations.length > 0 && (
          <section style={styles.section}>
            <h2 style={styles.sectionTitle}>Recommended for You</h2>
            <div className="grid-4">
              {recommendations.map(p => <ProductCard key={p.id} product={p} />)}
            </div>
          </section>
        )}
      </div>
    </div>
  )
}

const styles = {
  hero: { background: 'linear-gradient(135deg,#1f2328 0%,#3b4a5a 100%)', color: '#fff', padding: '80px 16px', textAlign: 'center' },
  heroContent: { maxWidth: 600, margin: '0 auto' },
  heroTitle: { fontSize: 42, fontWeight: 800, marginBottom: 16, lineHeight: 1.2 },
  heroSub: { fontSize: 18, color: '#c9d1d9', marginBottom: 32 },
  section: { marginBottom: 48 },
  sectionTitle: { fontSize: 24, fontWeight: 700, marginBottom: 20, paddingBottom: 10, borderBottom: '2px solid #e5e7eb' },
  catGrid: { display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(160px, 1fr))', gap: 16 },
  catCard: { borderRadius: 10, overflow: 'hidden', background: '#fff', border: '1px solid #e5e7eb', display: 'block' },
  catImg: { width: '100%', height: 100, objectFit: 'cover' },
  catName: { padding: '10px 12px', fontWeight: 600, fontSize: 14 },
}
