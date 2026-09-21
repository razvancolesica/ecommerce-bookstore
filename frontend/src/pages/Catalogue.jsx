import React, { useEffect, useState } from 'react'
import { useSearchParams } from 'react-router-dom'
import api from '../services/api'
import ProductCard from '../components/ProductCard'

export default function Catalogue() {
  const [searchParams, setSearchParams] = useSearchParams()
  const [products, setProducts] = useState([])
  const [categories, setCategories] = useState([])
  const [brands, setBrands] = useState([])
  const [total, setTotal] = useState(0)
  const [totalPages, setTotalPages] = useState(0)
  const [loading, setLoading] = useState(true)

  const page = parseInt(searchParams.get('page') || '0')
  const categoryId = searchParams.get('categoryId') || ''
  const brandId = searchParams.get('brandId') || ''
  const search = searchParams.get('search') || ''
  const sort = searchParams.get('sort') || 'title_asc'

  useEffect(() => {
    Promise.all([api.get('/categories'), api.get('/brands')]).then(([c, b]) => {
      setCategories(c.data); setBrands(b.data)
    })
  }, [])

  useEffect(() => {
    setLoading(true)
    const params = new URLSearchParams({ page, size: 12, sort })
    if (categoryId) params.set('categoryId', categoryId)
    if (brandId) params.set('brandId', brandId)
    if (search) params.set('search', search)
    api.get(`/products?${params}`).then(({ data }) => {
      setProducts(data.content || [])
      setTotal(data.totalElements || 0)
      setTotalPages(data.totalPages || 0)
    }).finally(() => setLoading(false))
  }, [categoryId, brandId, search, page, sort])

  const set = (key, val) => {
    const p = new URLSearchParams(searchParams)
    if (val) p.set(key, val); else p.delete(key)
    p.delete('page')
    setSearchParams(p)
  }

  return (
    <div className="page">
      <h1 style={{ fontSize: 28, fontWeight: 700, marginBottom: 24 }}>Book Catalogue</h1>

      {/* Filters */}
      <div style={styles.filters}>
        <input placeholder="Search title or author…" value={search}
          onChange={e => set('search', e.target.value)} style={{ maxWidth: 280 }} />
        <select value={categoryId} onChange={e => set('categoryId', e.target.value)}>
          <option value="">All Categories</option>
          {categories.map(c => <option key={c.id} value={c.id}>{c.name}</option>)}
        </select>
        <select value={brandId} onChange={e => set('brandId', e.target.value)}>
          <option value="">All Publishers</option>
          {brands.map(b => <option key={b.id} value={b.id}>{b.name}</option>)}
        </select>
        <select value={sort} onChange={e => set('sort', e.target.value)}>
          <option value="title_asc">Title A–Z</option>
          <option value="price_asc">Price: Low to High</option>
          <option value="price_desc">Price: High to Low</option>
          <option value="newest">Newest First</option>
        </select>
      </div>

      <p style={{ color: '#57606a', marginBottom: 20 }}>{total} book{total !== 1 ? 's' : ''} found</p>

      {loading ? (
        <div className="spinner">Loading…</div>
      ) : products.length === 0 ? (
        <div style={{ textAlign: 'center', padding: 60, color: '#57606a' }}>No books found. Try adjusting your filters.</div>
      ) : (
        <div className="grid-4">
          {products.map(p => <ProductCard key={p.id} product={p} />)}
        </div>
      )}

      {/* Pagination */}
      {totalPages > 1 && (
        <div style={styles.pagination}>
          {Array.from({ length: totalPages }, (_, i) => (
            <button key={i}
              onClick={() => { const p = new URLSearchParams(searchParams); p.set('page', i); setSearchParams(p) }}
              style={{ ...styles.pageBtn, ...(i === page ? styles.pageBtnActive : {}) }}>
              {i + 1}
            </button>
          ))}
        </div>
      )}
    </div>
  )
}

const styles = {
  filters: { display: 'flex', flexWrap: 'wrap', gap: 12, marginBottom: 24 },
  pagination: { display: 'flex', gap: 8, marginTop: 32, justifyContent: 'center' },
  pageBtn: { padding: '8px 14px', border: '1px solid #e5e7eb', borderRadius: 6, background: '#fff', fontSize: 14, cursor: 'pointer' },
  pageBtnActive: { background: '#3b82d4', color: '#fff', borderColor: '#3b82d4' },
}
