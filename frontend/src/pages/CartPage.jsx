import React from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useCart } from '../context/CartContext';
import { useAuth } from '../context/AuthContext';

export default function CartPage() {
  const { cart, updateQuantity, removeItem, loading, message } = useCart();
  const { user } = useAuth();
  const navigate = useNavigate();

  const items = cart.items || [];
  const totalAmount = cart.totalAmount || items.reduce((sum, item) => sum + (item.subtotal || item.price * item.quantity), 0);

  if (!user) {
    return (
      <div className="container-custom py-16 text-center">
        <div className="card p-8 max-w-md mx-auto">
          <h2 className="text-lg font-bold text-slate-900 mb-2">Sign in to View Your Cart</h2>
          <p className="text-xs text-slate-500 mb-6">
            Your cart is securely tied to your user account via Spring Security &amp; JWT.
          </p>
          <Link to="/login" className="btn btn-primary text-xs w-full">
            Sign In Now
          </Link>
        </div>
      </div>
    );
  }

  return (
    <div className="container-custom py-8">
      
      <div className="mb-6">
        <h1 className="text-2xl font-bold text-slate-900">Your Shopping Cart</h1>
        <p className="text-xs text-slate-500">
          Review your items, change quantities, and proceed to checkout.
        </p>
      </div>

      {message && (
        <div className={`mb-6 p-4 rounded-md text-sm font-medium border ${
          message.type === 'error'
            ? 'bg-red-50 text-red-700 border-red-200'
            : 'bg-green-50 text-green-700 border-green-200'
        }`}>
          {message.text}
        </div>
      )}

      {loading ? (
        <div className="text-center py-16 text-slate-500 text-sm">
          Loading your cart...
        </div>
      ) : items.length === 0 ? (
        <div className="card p-12 text-center max-w-lg mx-auto">
          <p className="text-base font-bold text-slate-800 mb-2">Your cart is empty</p>
          <p className="text-xs text-slate-500 mb-6">
            Explore our product catalog and add items to your cart.
          </p>
          <Link to="/" className="btn btn-primary text-xs">
            Start Shopping
          </Link>
        </div>
      ) : (
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
          
          {/* Cart Table */}
          <div className="lg:col-span-2 card overflow-hidden">
            <table className="w-full text-left text-xs border-collapse">
              <thead className="bg-slate-50 text-slate-600 border-b border-slate-200 uppercase font-semibold">
                <tr>
                  <th className="p-3.5">Product</th>
                  <th className="p-3.5">Price</th>
                  <th className="p-3.5 text-center">Quantity</th>
                  <th className="p-3.5 text-right">Subtotal</th>
                  <th className="p-3.5 text-right">Action</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100">
                {items.map((item) => (
                  <tr key={item.id} className="hover:bg-slate-50/50">
                    <td className="p-3.5 font-medium text-slate-900">
                      <Link to={`/products/${item.productId}`} className="hover:text-blue-600">
                        {item.productName}
                      </Link>
                    </td>
                    <td className="p-3.5 text-slate-600">
                      ${Number(item.price).toFixed(2)}
                    </td>
                    <td className="p-3.5 text-center">
                      <div className="inline-flex items-center border border-slate-300 rounded">
                        <button
                          onClick={() => updateQuantity(item.id, item.quantity - 1)}
                          className="px-2 py-0.5 text-slate-600 hover:bg-slate-100"
                        >
                          -
                        </button>
                        <span className="px-3 py-0.5 font-semibold text-slate-800">
                          {item.quantity}
                        </span>
                        <button
                          onClick={() => updateQuantity(item.id, item.quantity + 1)}
                          className="px-2 py-0.5 text-slate-600 hover:bg-slate-100"
                        >
                          +
                        </button>
                      </div>
                    </td>
                    <td className="p-3.5 text-right font-bold text-slate-900">
                      ${Number(item.subtotal || item.price * item.quantity).toFixed(2)}
                    </td>
                    <td className="p-3.5 text-right">
                      <button
                        onClick={() => removeItem(item.id)}
                        className="text-red-600 hover:text-red-800 hover:underline"
                      >
                        Remove
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>

          {/* Order Summary */}
          <div className="card p-6 h-fit">
            <h3 className="text-base font-bold text-slate-900 mb-4 border-b border-slate-100 pb-3">
              Order Summary
            </h3>

            <div className="space-y-2.5 text-xs text-slate-600 mb-6">
              <div className="flex justify-between">
                <span>Items Subtotal:</span>
                <span className="font-semibold text-slate-900">${Number(totalAmount).toFixed(2)}</span>
              </div>
              <div className="flex justify-between">
                <span>Shipping:</span>
                <span className="text-green-600 font-semibold">Free</span>
              </div>
              <div className="pt-3 border-t border-slate-100 flex justify-between text-sm font-bold text-slate-900">
                <span>Total Amount:</span>
                <span className="text-blue-600">${Number(totalAmount).toFixed(2)}</span>
              </div>
            </div>

            <button
              onClick={() => navigate('/checkout')}
              className="btn btn-primary w-full text-xs py-2.5"
            >
              Proceed to Checkout &rarr;
            </button>
          </div>

        </div>
      )}

    </div>
  );
}
