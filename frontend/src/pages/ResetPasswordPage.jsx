import React, { useState, useEffect } from 'react';
import { useNavigate, useLocation, Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

export default function ResetPasswordPage() {
  const { resetPassword, resendOtp } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();

  const [email, setEmail] = useState(location.state?.email || '');
  const [otp, setOtp] = useState('');
  const [newPassword, setNewPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [loading, setLoading] = useState(false);
  const [resending, setResending] = useState(false);
  const [error, setError] = useState(null);
  const [message, setMessage] = useState(location.state?.message || null);
  const [cooldown, setCooldown] = useState(60);

  useEffect(() => {
    let timer;
    if (cooldown > 0) {
      timer = setInterval(() => setCooldown((c) => c - 1), 1000);
    }
    return () => clearInterval(timer);
  }, [cooldown]);

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!email || !otp || !newPassword) {
      setError('Please fill in all required fields.');
      return;
    }

    if (newPassword.length < 6) {
      setError('Password must be at least 6 characters.');
      return;
    }

    if (newPassword !== confirmPassword) {
      setError('Passwords do not match.');
      return;
    }

    try {
      setLoading(true);
      setError(null);
      await resetPassword(email, otp, newPassword);
      navigate('/login', {
        state: {
          email,
          message: 'Password reset successfully! You may now sign in with your new password.'
        }
      });
    } catch (err) {
      setError(err.response?.data?.message || err.message || 'Password reset failed. Please verify your OTP code.');
    } finally {
      setLoading(false);
    }
  };

  const handleResend = async () => {
    if (!email) {
      setError('Please provide your email to resend OTP.');
      return;
    }
    try {
      setResending(true);
      setError(null);
      await resendOtp(email, 'PASSWORD_RESET');
      setMessage('A fresh password reset code has been sent to your email.');
      setCooldown(60);
    } catch (err) {
      setError(err.response?.data?.message || err.message || 'Could not resend OTP.');
    } finally {
      setResending(false);
    }
  };

  return (
    <div className="container-custom py-12 flex justify-center">
      <div className="card p-8 max-w-md w-full shadow-md border-slate-200">
        
        <div className="text-center mb-6">
          <div className="inline-block p-3 bg-indigo-50 text-indigo-600 rounded-full mb-3 text-3xl">
            🔑
          </div>
          <h1 className="text-xl font-bold text-slate-900">Set New Password</h1>
          <p className="text-xs text-slate-500 mt-1">
            Enter the 6-digit code received via email and your new password.
          </p>
        </div>

        {error && (
          <div className="bg-red-50 border border-red-200 text-red-700 p-3 rounded-md text-xs mb-4">
            <span className="font-semibold">Error:</span> {error}
          </div>
        )}

        {message && (
          <div className="bg-blue-50 border border-blue-200 text-blue-700 p-3 rounded-md text-xs mb-4">
            ✓ {message}
          </div>
        )}

        <form onSubmit={handleSubmit} className="space-y-3.5 text-xs">
          <div>
            <label className="block font-semibold text-slate-700 mb-1">Email Address</label>
            <input
              type="email"
              required
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              placeholder="user@example.com"
              className="input-field text-xs"
            />
          </div>

          <div>
            <label className="block font-semibold text-slate-700 mb-1">6-Digit Reset Code (OTP)</label>
            <input
              type="text"
              required
              maxLength={6}
              value={otp}
              onChange={(e) => setOtp(e.target.value.replace(/\D/g, ''))}
              placeholder="123456"
              className="input-field text-center text-lg tracking-widest font-mono font-bold"
            />
          </div>

          <div>
            <label className="block font-semibold text-slate-700 mb-1">New Password</label>
            <input
              type="password"
              required
              value={newPassword}
              onChange={(e) => setNewPassword(e.target.value)}
              placeholder="•••••••• (min 6 characters)"
              className="input-field text-xs"
            />
          </div>

          <div>
            <label className="block font-semibold text-slate-700 mb-1">Confirm New Password</label>
            <input
              type="password"
              required
              value={confirmPassword}
              onChange={(e) => setConfirmPassword(e.target.value)}
              placeholder="••••••••"
              className="input-field text-xs"
            />
          </div>

          <button
            type="submit"
            disabled={loading || otp.length !== 6 || !newPassword}
            className="btn btn-primary w-full text-xs py-2.5 mt-2 font-semibold"
          >
            {loading ? 'Resetting Password...' : 'Update Password & Sign In'}
          </button>
        </form>

        <div className="mt-6 pt-4 border-t border-slate-100 flex items-center justify-between text-xs text-slate-500">
          <span>Didn't get the code?</span>
          <button
            type="button"
            disabled={cooldown > 0 || resending}
            onClick={handleResend}
            className="text-blue-600 font-semibold hover:underline disabled:text-slate-400 disabled:no-underline"
          >
            {cooldown > 0 ? `Resend in ${cooldown}s` : resending ? 'Sending...' : 'Resend Code'}
          </button>
        </div>

        <div className="mt-4 text-center text-xs">
          <Link to="/login" className="text-slate-500 hover:text-slate-700 hover:underline">
            Back to Sign In
          </Link>
        </div>

      </div>
    </div>
  );
}
