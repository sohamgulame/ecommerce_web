import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import apiClient from '../api/axios';
import { useCart } from '../context/CartContext';
import { useAuth } from '../context/AuthContext';

export default function CheckoutPage() {
  const { cart, clearCartState } = useCart();
  const { user } = useAuth();
  const navigate = useNavigate();

  const [shippingInfo, setShippingInfo] = useState({
    fullName: user?.name || '',
    address: '123 Main Street',
    city: 'Springfield',
    phone: '9990001111',
  });

  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [confirmedOrder, setConfirmedOrder] = useState(null);

  const items = cart.items || [];
  const totalAmount = cart.totalAmount || items.reduce((sum, item) => sum + (item.subtotal || item.price * item.quantity), 0);

  const handlePlaceOrder = async (e) => {
    e.preventDefault();
    if (items.length === 0) {
      setError('Your cart is empty. Please add products before placing an order.');
      return;
    }

    try {
      setLoading(true);
      setError(null);

      // Call Spring Boot POST /api/v1/orders
      const res = await apiClient.post('/orders');
      const order = res.data;

      setConfirmedOrder(order);
      clearCartState();
    } catch (err) {
      setError(err.message || 'Failed to place order.');
    } finally {
      setLoading(false);
    }
  };

  if (confirmedOrder) {
    return (
      <div className="container-custom py-16">
        <div className="card p-8 max-w-lg mx-auto text-center border-green-200 bg-green-50/20">
          <div className="text-4xl mb-3">✅</div>
          <h1 className="text-xl font-bold text-slate-900 mb-2">Order Confirmed!</h1>
          <p className="text-xs text-slate-600 mb-6">
            Thank you for your purchase. Your order has been placed and stock has been decremented.
          </p>

          <div className="bg-white p-4 rounded-md border border-slate-200 text-left text-xs space-y-2 mb-6">
            <div className="flex justify-between">
              <span className="text-slate-500">Order ID:</span>
              <span className="font-bold text-slate-800">#{confirmedOrder.id}</span>
            </div>
            <div className="flex justify-between">
              <span className="text-slate-500">Status:</span>
              <span className="font-semibold text-blue-600">{confirmedOrder.status || 'PLACED'}</span>
            </div>
            <div className="flex justify-between">
              <span className="text-slate-500">Total Billed:</span>
              <span className="font-bold text-slate-900">${Number(confirmedOrder.totalAmount || totalAmount).toFixed(2)}</span>
            </div>
          </div>

          <div className="flex gap-3">
            <Link to="/orders" className="btn btn-secondary text-xs flex-1">
              View Order History
            </Link>
            <Link to="/" className="btn btn-primary text-xs flex-1">
              Continue Shopping
            </Link>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="container-custom py-8">
      
      <div className="mb-6">
        <h1 className="text-2xl font-bold text-slate-900">Checkout</h1>
        <p className="text-xs text-slate-500">
          Provide your delivery details and confirm your purchase.
        </p>
      </div>

      {error && (
        <div className="bg-red-50 border border-red-200 text-red-700 p-4 rounded-md text-xs mb-6">
          <span className="font-bold">Error:</span> {error}
        </div>
      )}

      {items.length === 0 ? (
        <div className="card p-12 text-center max-w-md mx-auto">
          <p className="text-sm font-semibold text-slate-800 mb-4">Your cart is currently empty.</p>
          <Link to="/" className="btn btn-primary text-xs">
            Back to Products
          </Link>
        </div>
      ) : (
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
          
          {/* Shipping Form */}
          <div className="lg:col-span-2 card p-6">
            <h2 className="text-base font-bold text-slate-900 mb-4 border-b border-slate-100 pb-3">
              1. Delivery &amp; Contact Details
            </h2>

            <form onSubmit={handlePlaceOrder} id="checkout-form" className="space-y-4 text-xs">
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                <div>
                  <label className="block font-semibold text-slate-700 mb-1">Full Name</label>
                  <input
                    type="text"
                    required
                    value={shippingInfo.fullName}
                    onChange={(e) => setShippingInfo({ ...shippingInfo, fullName: e.target.value })}
                    className="input-field text-xs"
                  />
                </div>
                <div>
                  <label className="block font-semibold text-slate-700 mb-1">Phone Number</label>
                  <input
                    type="text"
                    required
                    value={shippingInfo.phone}
                    onChange={(e) => setShippingInfo({ ...shippingInfo, phone: e.target.value })}
                    className="input-field text-xs"
                  />
                </div>
              </div>

              <div>
                <label className="block font-semibold text-slate-700 mb-1">Street Address</label>
                <input
                  type="text"
                  required
                  value={shippingInfo.address}
                  onChange={(e) => setShippingInfo({ ...shippingInfo, address: e.target.value })}
                  className="input-field text-xs"
                />
              </div>

              <div>
                <label className="block font-semibold text-slate-700 mb-1">City</label>
                <input
                  type="text"
                  required
                  value={shippingInfo.city}
                  onChange={(e) => setShippingInfo({ ...shippingInfo, city: e.target.value })}
                  className="input-field text-xs"
                />
              </div>

              <div className="pt-4 border-t border-slate-100">
                <h3 className="text-sm font-bold text-slate-900 mb-2">2. Payment Simulation</h3>
                <p className="text-slate-500 mb-3">
                  (Simulated standard checkout per Phase 5/8.5 specifications)
                </p>
                <div className="p-3 bg-slate-50 border border-slate-200 rounded text-slate-700">
                  💳 Cash on Delivery / Simulated Instant Payment
                </div>
              </div>
            </form>
          </div>

          {/* Order Review Side */}
          <div className="card p-6 h-fit">
            <h3 className="text-base font-bold text-slate-900 mb-4 border-b border-slate-100 pb-3">
              Order Review
            </h3>

            <div className="space-y-3 mb-6 max-h-56 overflow-y-auto">
              {items.map((i) => (
                <div key={i.id} className="flex justify-between text-xs">
                  <span className="text-slate-700 truncate max-w-[150px]">
                    {i.quantity}x {i.productName}
                  </span>
                  <span className="font-semibold text-slate-900">
                    ${Number(i.subtotal || i.price * i.quantity).toFixed(2)}
                  </span>
                </div>
              ))}
            </div>

            <div className="pt-3 border-t border-slate-100 space-y-2 text-xs mb-6">
              <div className="flex justify-between">
                <span className="text-slate-600">Subtotal:</span>
                <span className="font-semibold text-slate-900">${Number(totalAmount).toFixed(2)}</span>
              </div>
              <div className="flex justify-between">
                <span className="text-slate-600">Shipping:</span>
                <span className="text-green-600 font-semibold">Free</span>
              </div>
              <div className="pt-2 border-t border-slate-100 flex justify-between text-sm font-bold text-slate-900">
                <span>Grand Total:</span>
                <span className="text-blue-600">${Number(totalAmount).toFixed(2)}</span>
              </div>
            </div>

            <button
              type="submit"
              form="checkout-form"
              disabled={loading}
              className="btn btn-primary w-full text-xs py-2.5"
            >
              {loading ? 'Processing Order...' : `Place Order ($${Number(totalAmount).toFixed(2)})`}
            </button>
          </div>

        </div>
      )}

    </div>
  );
}
