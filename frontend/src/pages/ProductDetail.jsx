import React, { useEffect, useState } from 'react'
import { useParams, Link, useNavigate } from 'react-router-dom'
import api from '../services/api'
import { useCart } from '../context/CartContext'
import { useAuth } from '../context/AuthContext'
import ProductCard from '../components/ProductCard'

export default function ProductDetail() {
  const { id } = useParams()
  const [product, setProduct] = useState(null)
  const [related, setRelated] = useState([])
  const [qty, setQty] = useState(1)
  const [added, setAdded] = useState(false)
  const { addItem } = useCart()
  const { user } = useAuth()
  const navigate = useNavigate()

  useEffect(() => {
    setAdded(false)
    Promise.all([
      api.get(`/products/${id}`),
      api.get(`/products/${id}/related?limit=4`)
    ]).then(([p, r]) => {
      setProduct(p.data)
      setRelated(r.data || [])
    })
  }, [id])

  const handleAdd = async () => {
    if (!user) { navigate('/login'); return }
    await addItem(product.id, qty)
    setAdded(true)
  }

  if (!product) return <div className="spinner">Loading…</div>

  return (
    <div className="page">
      {/* Breadcrumb */}
      <div style={styles.breadcrumb}>
        <Link to="/">Home</Link> / <Link to="/catalogue">Catalogue</Link> / {product.title}
      </div>

      <div style={styles.layout}>
        {/* Image */}
        <div style={styles.imgWrap}>
          <img
            src={product.coverImageUrl || `https://picsum.photos/seed/${id}/300/450`}
            alt={product.title}
            style={styles.img}
            onError={e => { e.target.src = `https://picsum.photos/seed/${id}/300/450` }}
          />
        </div>

        {/* Info */}
        <div style={styles.info}>
          <span className="badge">{product.categoryName}</span>
          <h1 style={styles.title}>{product.title}</h1>
          <p style={styles.author}>by <strong>{product.author}</strong></p>
          {product.brand && <p style={styles.meta}>Publisher: {product.brand.name}</p>}
          {product.isbn && <p style={styles.meta}>ISBN: {product.isbn}</p>}
          {product.pages && <p style={styles.meta}>{product.pages} pages</p>}
          {product.publishedDate && <p style={styles.meta}>Published: {product.publishedDate}</p>}

          <p style={styles.desc}>{product.description}</p>

          <div style={styles.priceRow}>
            <span style={styles.price}>${product.price?.toFixed(2)}</span>
            <span style={styles.delivery}>🚚 Est. {product.estimatedDeliveryDays} business days</span>
          </div>

          <div style={styles.stockInfo}>
            {product.stockQuantity > 0
              ? <span style={{ color: '#2d7d46' }}>✓ In Stock ({product.stockQuantity} available)</span>
              : <span style={{ color: '#e53e3e' }}>✗ Out of Stock</span>}
          </div>

          <div style={styles.addRow}>
            <div style={styles.qtyWrap}>
              <button onClick={() => setQty(q => Math.max(1, q - 1))} style={styles.qtyBtn}>−</button>
              <span style={styles.qtyVal}>{qty}</span>
              <button onClick={() => setQty(q => q + 1)} style={styles.qtyBtn}>+</button>
            </div>
            <button
              className={added ? 'btn-outline' : 'btn-primary'}
              onClick={handleAdd}
              disabled={product.stockQuantity === 0}
              style={{ flex: 1, fontSize: 16 }}>
              {added ? '✓ Added to Cart' : 'Add to Cart'}
            </button>
          </div>

          {added && (
            <div style={styles.cartActions}>
              <Link to="/cart"><button className="btn-outline">View Cart</button></Link>
              <Link to="/checkout"><button className="btn-primary">Checkout Now</button></Link>
            </div>
          )}
        </div>
      </div>

      {/* Related */}
      {related.length > 0 && (
        <section style={{ marginTop: 48 }}>
          <h2 style={{ fontSize: 22, fontWeight: 700, marginBottom: 20 }}>Related Books</h2>
          <div className="grid-4">
            {related.map(p => <ProductCard key={p.id} product={p} />)}
          </div>
        </section>
      )}
    </div>
  )
}

const styles = {
  breadcrumb: { color: '#57606a', fontSize: 13, marginBottom: 24 },
  layout: { display: 'grid', gridTemplateColumns: '280px 1fr', gap: 40, alignItems: 'start' },
  imgWrap: { borderRadius: 10, overflow: 'hidden', border: '1px solid #e5e7eb' },
  img: { width: '100%', display: 'block' },
  info: { display: 'flex', flexDirection: 'column', gap: 10 },
  title: { fontSize: 32, fontWeight: 800, lineHeight: 1.2 },
  author: { fontSize: 16, color: '#57606a' },
  meta: { fontSize: 13, color: '#57606a' },
  desc: { fontSize: 15, lineHeight: 1.7, color: '#444', padding: '16px 0', borderTop: '1px solid #e5e7eb', borderBottom: '1px solid #e5e7eb', margin: '8px 0' },
  priceRow: { display: 'flex', alignItems: 'center', gap: 20 },
  price: { fontSize: 32, fontWeight: 800, color: '#1f2328' },
  delivery: { fontSize: 13, color: '#57606a' },
  stockInfo: { fontSize: 14 },
  addRow: { display: 'flex', gap: 12, alignItems: 'center', marginTop: 8 },
  qtyWrap: { display: 'flex', alignItems: 'center', border: '1px solid #e5e7eb', borderRadius: 6, overflow: 'hidden' },
  qtyBtn: { width: 36, height: 40, background: '#f7f8fa', border: 'none', fontSize: 18, cursor: 'pointer', borderRadius: 0 },
  qtyVal: { width: 40, textAlign: 'center', fontWeight: 700 },
  cartActions: { display: 'flex', gap: 12, marginTop: 8 },
}
