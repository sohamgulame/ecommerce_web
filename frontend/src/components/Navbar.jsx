import React from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { useCart } from '../context/CartContext';

export default function Navbar() {
  const { user, isAdmin, logout } = useAuth();
  const { totalItemCount } = useCart();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  return (
    <header className="bg-white border-b border-slate-200 sticky top-0 z-30 shadow-xs">
      <div className="container-custom py-3.5 flex items-center justify-between gap-4">
        
        {/* Brand Logo */}
        <div className="flex items-center gap-6">
          <Link to="/" className="text-lg font-bold text-blue-600 hover:text-blue-700 flex items-center gap-2">
            <span>🛒 SpringShop</span>
          </Link>

          {/* Nav Links */}
          <nav className="hidden md:flex items-center gap-5 text-sm font-medium text-slate-600">
            <Link to="/" className="hover:text-blue-600 transition-colors">
              Products
            </Link>
            {user && (
              <Link to="/orders" className="hover:text-blue-600 transition-colors">
                My Orders
              </Link>
            )}
          </nav>
        </div>

        {/* Admin Navigation (if ADMIN) */}
        {isAdmin && (
          <div className="hidden lg:flex items-center gap-3 bg-purple-50 px-3 py-1.5 rounded-md border border-purple-200 text-xs font-semibold text-purple-800">
            <span className="uppercase text-[10px] tracking-wider text-purple-600 font-bold">Admin:</span>
            <Link to="/admin/products" className="hover:underline">Products</Link>
            <span>&bull;</span>
            <Link to="/admin/categories" className="hover:underline">Categories</Link>
            <span>&bull;</span>
            <Link to="/admin/orders" className="hover:underline">Orders</Link>
          </div>
        )}

        {/* Right Actions */}
        <div className="flex items-center gap-3">
          
          {/* Cart Icon & Badge */}
          <Link
            to="/cart"
            className="btn btn-secondary text-xs relative flex items-center gap-2"
          >
            <span>Cart</span>
            {totalItemCount > 0 && (
              <span className="bg-blue-600 text-white text-[11px] font-bold px-1.5 py-0.2 rounded-full">
                {totalItemCount}
              </span>
            )}
          </Link>

          {/* User Profile / Auth */}
          {user ? (
            <div className="flex items-center gap-3 pl-2 border-l border-slate-200 text-xs">
              <div className="hidden sm:block text-right">
                <span className="font-semibold text-slate-800 block truncate max-w-[120px]">
                  {user.name}
                </span>
                <span className="text-[10px] text-slate-500 font-mono">
                  {user.role === 'ROLE_ADMIN' ? 'ADMIN' : 'CUSTOMER'}
                </span>
              </div>
              <button
                onClick={handleLogout}
                className="btn btn-secondary text-xs py-1 px-2.5 text-slate-600 hover:text-red-600 hover:border-red-300"
              >
                Logout
              </button>
            </div>
          ) : (
            <div className="flex items-center gap-2 text-xs">
              <Link to="/login" className="btn btn-secondary text-xs">
                Login
              </Link>
              <Link to="/register" className="btn btn-primary text-xs">
                Register
              </Link>
            </div>
          )}

        </div>

      </div>

      {/* Mobile Admin Bar */}
      {isAdmin && (
        <div className="lg:hidden bg-purple-50 border-t border-purple-200 px-4 py-2 flex items-center justify-between text-xs font-semibold text-purple-800">
          <span className="text-[10px] uppercase text-purple-600 font-bold">Admin Portal:</span>
          <div className="flex gap-4">
            <Link to="/admin/products" className="hover:underline">Products</Link>
            <Link to="/admin/categories" className="hover:underline">Categories</Link>
            <Link to="/admin/orders" className="hover:underline">Orders</Link>
          </div>
        </div>
      )}
    </header>
  );
}
