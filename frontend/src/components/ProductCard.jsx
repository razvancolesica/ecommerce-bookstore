import React from 'react'
import { Link } from 'react-router-dom'
import { useCart } from '../context/CartContext'
import { useAuth } from '../context/AuthContext'
import { useNavigate } from 'react-router-dom'

export default function ProductCard({ product }) {
  const { addItem } = useCart()
  const { user } = useAuth()
  const navigate = useNavigate()

  const handleAdd = async (e) => {
    e.preventDefault()
    if (!user) { navigate('/login'); return }
    await addItem(product.id, 1)
  }

  return (
    <Link to={`/products/${product.id}`} style={styles.card}>
      <img
        src={product.coverImageUrl || `https://picsum.photos/seed/${product.id}/300/450`}
        alt={product.title}
        style={styles.img}
        onError={e => { e.target.src = `https://picsum.photos/seed/${product.id}/300/450` }}
      />
      <div style={styles.body}>
        <div style={styles.category}>{product.categoryName}</div>
        <div style={styles.title}>{product.title}</div>
        <div style={styles.author}>{product.author}</div>
        <div style={styles.footer}>
          <span style={styles.price}>${product.price?.toFixed(2)}</span>
          <button className="btn-primary" onClick={handleAdd} style={styles.addBtn}>
            Add to Cart
          </button>
        </div>
        <div style={styles.delivery}>🚚 Est. {product.estimatedDeliveryDays} days delivery</div>
      </div>
    </Link>
  )
}

const styles = {
  card: { display: 'block', background: '#fff', border: '1px solid #e5e7eb', borderRadius: 10, overflow: 'hidden', transition: 'box-shadow 0.2s', cursor: 'pointer' },
  img: { width: '100%', height: 200, objectFit: 'cover' },
  body: { padding: 14 },
  category: { fontSize: 11, color: '#3b82d4', fontWeight: 600, textTransform: 'uppercase', marginBottom: 4 },
  title: { fontWeight: 700, fontSize: 15, marginBottom: 4, lineHeight: 1.3 },
  author: { color: '#57606a', fontSize: 13, marginBottom: 10 },
  footer: { display: 'flex', justifyContent: 'space-between', alignItems: 'center' },
  price: { fontSize: 18, fontWeight: 700, color: '#1f2328' },
  addBtn: { padding: '6px 12px', fontSize: 12 },
  delivery: { fontSize: 11, color: '#57606a', marginTop: 8 },
}
