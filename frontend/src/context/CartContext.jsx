import React, { createContext, useContext, useState, useEffect } from 'react';
import apiClient from '../api/axios';
import { useAuth } from './AuthContext';

const CartContext = createContext(null);

export function CartProvider({ children }) {
  const { token } = useAuth();
  const [cart, setCart] = useState({ id: null, items: [], totalAmount: 0 });
  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState(null); // { text, type: 'success' | 'error' }

  const showMessage = (text, type = 'success') => {
    setMessage({ text, type });
    setTimeout(() => setMessage(null), 4000);
  };

  const fetchCart = async () => {
    if (!token) {
      setCart({ id: null, items: [], totalAmount: 0 });
      return;
    }
    try {
      setLoading(true);
      const res = await apiClient.get('/cart');
      setCart(res.data);
    } catch (err) {
      console.warn('Could not fetch cart from backend:', err.message);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchCart();
  }, [token]);

  const addToCart = async (productId, quantity = 1) => {
    if (!token) {
      showMessage('Please sign in to add items to your cart.', 'error');
      return false;
    }
    try {
      const res = await apiClient.post('/cart/items', { productId, quantity });
      setCart(res.data);
      showMessage('Item added to cart!', 'success');
      return true;
    } catch (err) {
      showMessage(err.message || 'Failed to add item to cart.', 'error');
      return false;
    }
  };

  const updateQuantity = async (itemId, newQuantity) => {
    if (newQuantity <= 0) {
      return removeItem(itemId);
    }
    try {
      const res = await apiClient.put(`/cart/items/${itemId}`, { quantity: newQuantity });
      setCart(res.data);
    } catch (err) {
      showMessage(err.message || 'Failed to update quantity.', 'error');
    }
  };

  const removeItem = async (itemId) => {
    try {
      const res = await apiClient.delete(`/cart/items/${itemId}`);
      setCart(res.data);
      showMessage('Item removed from cart.', 'info');
    } catch (err) {
      showMessage(err.message || 'Failed to remove item.', 'error');
    }
  };

  const clearCartState = () => {
    setCart({ id: null, items: [], totalAmount: 0 });
  };

  const totalItemCount = (cart.items || []).reduce((sum, i) => sum + i.quantity, 0);

  return (
    <CartContext.Provider
      value={{
        cart,
        totalItemCount,
        loading,
        message,
        addToCart,
        updateQuantity,
        removeItem,
        fetchCart,
        clearCartState,
        showMessage,
      }}
    >
      {children}
    </CartContext.Provider>
  );
}

export function useCart() {
  return useContext(CartContext);
}
