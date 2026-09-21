import React, { createContext, useContext, useState, useEffect } from 'react'
import api from '../services/api'
import { useAuth } from './AuthContext'

const CartContext = createContext(null)

export function CartProvider({ children }) {
  const { user } = useAuth()
  const [cart, setCart] = useState({ items: [], totalItems: 0, subtotal: 0, total: 0 })

  const fetchCart = async () => {
    if (!user) return
    try {
      const { data } = await api.get('/cart')
      setCart(data)
    } catch {}
  }

  useEffect(() => { fetchCart() }, [user])

  const addItem = async (productId, quantity = 1) => {
    const { data } = await api.post('/cart/items', { productId, quantity })
    setCart(data)
  }

  const removeItem = async (itemId) => {
    const { data } = await api.delete(`/cart/items/${itemId}`)
    setCart(data)
  }

  const updateItem = async (itemId, quantity) => {
    const { data } = await api.put(`/cart/items/${itemId}`, { quantity })
    setCart(data)
  }

  const clearCart = () => setCart({ items: [], totalItems: 0, subtotal: 0, total: 0 })

  return (
    <CartContext.Provider value={{ cart, addItem, removeItem, updateItem, clearCart, fetchCart }}>
      {children}
    </CartContext.Provider>
  )
}

export const useCart = () => useContext(CartContext)
