import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

export default function ForgotPasswordPage() {
  const { forgotPassword } = useAuth();
  const navigate = useNavigate();

  const [email, setEmail] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!email) return;

    try {
      setLoading(true);
      setError(null);
      await forgotPassword(email);
      navigate('/reset-password', {
        state: {
          email,
          message: 'If an account matches that email, a 6-digit password reset OTP has been dispatched.'
        }
      });
    } catch (err) {
      setError(err.response?.data?.message || err.message || 'Could not process request.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="container-custom py-12 flex justify-center">
      <div className="card p-8 max-w-md w-full shadow-md border-slate-200">
        
        <div className="text-center mb-6">
          <div className="inline-block p-3 bg-red-50 text-red-600 rounded-full mb-3 text-3xl">
            🔒
          </div>
          <h1 className="text-xl font-bold text-slate-900">Forgot Password</h1>
          <p className="text-xs text-slate-500 mt-1">
            Enter your account email and we'll send you a 6-digit OTP code to reset your password.
          </p>
        </div>

        {error && (
          <div className="bg-red-50 border border-red-200 text-red-700 p-3 rounded-md text-xs mb-4">
            <span className="font-semibold">Error:</span> {error}
          </div>
        )}

        <form onSubmit={handleSubmit} className="space-y-4 text-xs">
          <div>
            <label className="block font-semibold text-slate-700 mb-1">Registered Email Address</label>
            <input
              type="email"
              required
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              placeholder="user@example.com"
              className="input-field text-xs"
            />
          </div>

          <button
            type="submit"
            disabled={loading || !email}
            className="btn btn-primary w-full text-xs py-2.5 mt-2 font-semibold"
          >
            {loading ? 'Sending Reset Code...' : 'Send Reset Code'}
          </button>
        </form>

        <div className="mt-6 pt-4 border-t border-slate-100 text-center text-xs text-slate-500">
          Remembered your password?{' '}
          <Link to="/login" className="text-blue-600 font-semibold hover:underline">
            Sign in here
          </Link>
        </div>

      </div>
    </div>
  );
}
