import React, { useState } from 'react';
import { useNavigate, useLocation, Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

export default function LoginPage() {
  const { login, register } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();

  const from = location.state?.from?.pathname && location.state?.from?.pathname !== '/login'
    ? location.state.from.pathname
    : '/';

  const [isLoginTab, setIsLoginTab] = useState(true);

  // Login Form State
  const [loginEmail, setLoginEmail] = useState(location.state?.email || '');
  const [loginPassword, setLoginPassword] = useState('');

  // Register Form State
  const [registerForm, setRegisterForm] = useState({
    name: '',
    email: '',
    password: '',
    phone: '',
    address: '',
  });

  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [successMsg, setSuccessMsg] = useState(location.state?.message || null);

  const handleLoginSubmit = async (e) => {
    e.preventDefault();
    try {
      setLoading(true);
      setError(null);
      await login(loginEmail, loginPassword);
      navigate(from, { replace: true });
    } catch (err) {
      setError(err.message || 'Invalid email or password.');
    } finally {
      setLoading(false);
    }
  };

  const handleRegisterSubmit = async (e) => {
    e.preventDefault();
    try {
      setLoading(true);
      setError(null);
      await register(registerForm);
      navigate('/verify-email', {
        state: {
          email: registerForm.email,
          message: 'Account created! Please enter the 6-digit OTP sent to your email to verify your account.'
        }
      });
    } catch (err) {
      setError(err.message || 'Registration failed. Check if email is already taken.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="container-custom py-12 flex justify-center">
      <div className="card p-8 max-w-md w-full shadow-md border-slate-200">
        
        {/* Header Title */}
        <div className="text-center mb-6">
          <div className="inline-block p-2.5 bg-blue-50 text-blue-600 rounded-full mb-3 text-2xl">
            🛒
          </div>
          <h1 className="text-xl font-bold text-slate-900">
            {isLoginTab ? 'Welcome to SpringShop' : 'Create an Account'}
          </h1>
          <p className="text-xs text-slate-500 mt-1">
            {isLoginTab
              ? 'Sign in to access your product catalog and shopping cart.'
              : 'Fill in your details to create a new account.'}
          </p>
        </div>

        {/* Feedback Alerts */}
        {error && (
          <div className="bg-red-50 border border-red-200 text-red-700 p-3 rounded-md text-xs mb-4">
            <span className="font-semibold">Error:</span> {error}
          </div>
        )}

        {successMsg && (
          <div className="bg-green-50 border border-green-200 text-green-700 p-3 rounded-md text-xs mb-4">
            ✓ {successMsg}
          </div>
        )}

        {isLoginTab ? (
          /* Sign In Form */
          <>
            <form onSubmit={handleLoginSubmit} className="space-y-4 text-xs">
              <div>
                <label className="block font-semibold text-slate-700 mb-1">Email Address</label>
                <input
                  type="email"
                  required
                  value={loginEmail}
                  onChange={(e) => setLoginEmail(e.target.value)}
                  placeholder="user@example.com"
                  className="input-field text-xs"
                />
              </div>

              <div>
                <div className="flex items-center justify-between mb-1">
                  <label className="font-semibold text-slate-700">Password</label>
                  <Link to="/forgot-password" className="text-blue-600 hover:underline">
                    Forgot password?
                  </Link>
                </div>
                <input
                  type="password"
                  required
                  value={loginPassword}
                  onChange={(e) => setLoginPassword(e.target.value)}
                  placeholder="••••••••"
                  className="input-field text-xs"
                />
              </div>

              <button
                type="submit"
                disabled={loading}
                className="btn btn-primary w-full text-xs py-2.5 mt-2 font-semibold"
              >
                {loading ? 'Authenticating with Backend...' : 'Sign In'}
              </button>
            </form>

            <div className="mt-6 pt-4 border-t border-slate-100 text-center text-xs text-slate-500">
              New user?{' '}
              <button
                type="button"
                onClick={() => {
                  setIsLoginTab(false);
                  setError(null);
                  setSuccessMsg(null);
                }}
                className="text-blue-600 font-semibold hover:underline"
              >
                Create an account
              </button>
            </div>
          </>
        ) : (
          /* Create Account Form */
          <>
            <form onSubmit={handleRegisterSubmit} className="space-y-3 text-xs">
              <div>
                <label className="block font-semibold text-slate-700 mb-1">Full Name</label>
                <input
                  type="text"
                  required
                  value={registerForm.name}
                  onChange={(e) => setRegisterForm({ ...registerForm, name: e.target.value })}
                  placeholder="Alex Mercer"
                  className="input-field text-xs"
                />
              </div>

              <div>
                <label className="block font-semibold text-slate-700 mb-1">Email Address</label>
                <input
                  type="email"
                  required
                  value={registerForm.email}
                  onChange={(e) => setRegisterForm({ ...registerForm, email: e.target.value })}
                  placeholder="alex@example.com"
                  className="input-field text-xs"
                />
              </div>

              <div>
                <label className="block font-semibold text-slate-700 mb-1">Password</label>
                <input
                  type="password"
                  required
                  value={registerForm.password}
                  onChange={(e) => setRegisterForm({ ...registerForm, password: e.target.value })}
                  placeholder="••••••••"
                  className="input-field text-xs"
                />
              </div>

              <div className="grid grid-cols-2 gap-2">
                <div>
                  <label className="block font-semibold text-slate-700 mb-1">Phone</label>
                  <input
                    type="text"
                    value={registerForm.phone}
                    onChange={(e) => setRegisterForm({ ...registerForm, phone: e.target.value })}
                    placeholder="9990001111"
                    className="input-field text-xs"
                  />
                </div>
                <div>
                  <label className="block font-semibold text-slate-700 mb-1">Address</label>
                  <input
                    type="text"
                    value={registerForm.address}
                    onChange={(e) => setRegisterForm({ ...registerForm, address: e.target.value })}
                    placeholder="123 Main St"
                    className="input-field text-xs"
                  />
                </div>
              </div>

              <button
                type="submit"
                disabled={loading}
                className="btn btn-primary w-full text-xs py-2.5 mt-2 font-semibold"
              >
                {loading ? 'Creating Account...' : 'Create Account'}
              </button>
            </form>

            <div className="mt-6 pt-4 border-t border-slate-100 text-center text-xs text-slate-500">
              Already have an account?{' '}
              <button
                type="button"
                onClick={() => {
                  setIsLoginTab(true);
                  setError(null);
                  setSuccessMsg(null);
                }}
                className="text-blue-600 font-semibold hover:underline"
              >
                Sign in here
              </button>
            </div>
          </>
        )}

      </div>
    </div>
  );
}
