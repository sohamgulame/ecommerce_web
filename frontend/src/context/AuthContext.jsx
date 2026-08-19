import React, { createContext, useContext, useState, useEffect } from 'react';
import apiClient from '../api/axios';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null);
  const [token, setToken] = useState(localStorage.getItem('token'));
  const [loading, setLoading] = useState(true);

  // Helper to parse JWT payload for email and role
  function parseJwt(tokenStr) {
    try {
      const base64Url = tokenStr.split('.')[1];
      const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
      const jsonPayload = decodeURIComponent(
        atob(base64)
          .split('')
          .map((c) => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2))
          .join('')
      );
      return JSON.parse(jsonPayload);
    } catch {
      return null;
    }
  }

  function extractUserFromToken(accessToken, fallbackEmail = null, fallbackName = null) {
    const payload = parseJwt(accessToken);
    if (!payload) return null;

    const role =
      payload.role ||
      (Array.isArray(payload.roles) ? payload.roles[0] : null) ||
      (Array.isArray(payload.authorities) ? payload.authorities[0] : null) ||
      'ROLE_CUSTOMER';

    const email = payload.sub || fallbackEmail || 'user';
    const name = payload.name || fallbackName || email.split('@')[0] || 'User';

    return { email, name, role };
  }

  useEffect(() => {
    const savedToken = localStorage.getItem('token');
    const savedUser = localStorage.getItem('user');

    if (savedToken && savedUser) {
      try {
        setToken(savedToken);
        setUser(JSON.parse(savedUser));
      } catch {
        localStorage.removeItem('token');
        localStorage.removeItem('refreshToken');
        localStorage.removeItem('user');
      }
    } else if (savedToken) {
      const parsedUser = extractUserFromToken(savedToken);
      if (parsedUser) {
        setUser(parsedUser);
        localStorage.setItem('user', JSON.stringify(parsedUser));
      }
    }
    setLoading(false);

    // Listen for silent token updates from Axios response interceptor
    const handleTokensUpdated = (e) => {
      const newToken = e.detail?.token;
      if (newToken) {
        setToken(newToken);
        const parsedUser = extractUserFromToken(newToken);
        if (parsedUser) {
          setUser(parsedUser);
          localStorage.setItem('user', JSON.stringify(parsedUser));
        }
      }
    };

    // Listen for forced logout event on refresh token failure
    const handleAuthLogout = () => {
      setUser(null);
      setToken(null);
    };

    window.addEventListener('auth-tokens-updated', handleTokensUpdated);
    window.addEventListener('auth-logout', handleAuthLogout);

    return () => {
      window.removeEventListener('auth-tokens-updated', handleTokensUpdated);
      window.removeEventListener('auth-logout', handleAuthLogout);
    };
  }, []);

  const login = async (email, password) => {
    const res = await apiClient.post('/auth/login', { email, password });
    const { accessToken, refreshToken } = res.data;

    setToken(accessToken);
    localStorage.setItem('token', accessToken);
    if (refreshToken) {
      localStorage.setItem('refreshToken', refreshToken);
    }

    const userData = extractUserFromToken(accessToken, email) || {
      email,
      name: email.split('@')[0],
      role: 'ROLE_CUSTOMER',
    };

    setUser(userData);
    localStorage.setItem('user', JSON.stringify(userData));
    return userData;
  };

  const register = async (registerData) => {
    const res = await apiClient.post('/auth/register', registerData);
    const { accessToken, refreshToken } = res.data;

    if (accessToken) {
      setToken(accessToken);
      localStorage.setItem('token', accessToken);
      if (refreshToken) {
        localStorage.setItem('refreshToken', refreshToken);
      }

      const userData = extractUserFromToken(accessToken, registerData.email, registerData.name) || {
        email: registerData.email,
        name: registerData.name,
        role: 'ROLE_CUSTOMER',
      };

      setUser(userData);
      localStorage.setItem('user', JSON.stringify(userData));
      return userData;
    }

    return res.data;
  };

  const logout = async () => {
    const storedRefreshToken = localStorage.getItem('refreshToken');
    if (storedRefreshToken) {
      try {
        await apiClient.post('/auth/logout', { refreshToken: storedRefreshToken });
      } catch {
        // Ignore network errors on logout
      }
    }
    setUser(null);
    setToken(null);
    localStorage.removeItem('token');
    localStorage.removeItem('refreshToken');
    localStorage.removeItem('user');
  };

  const verifyEmail = async (email, otp) => {
    const res = await apiClient.post('/auth/verify-email', { email, otp });
    const { accessToken, refreshToken } = res.data;
    if (accessToken) {
      setToken(accessToken);
      localStorage.setItem('token', accessToken);
      if (refreshToken) {
        localStorage.setItem('refreshToken', refreshToken);
      }
      const userData = extractUserFromToken(accessToken, email);
      if (userData) {
        setUser(userData);
        localStorage.setItem('user', JSON.stringify(userData));
      }
    }
    return res.data;
  };

  const resendOtp = async (email, type = 'EMAIL_VERIFICATION') => {
    const res = await apiClient.post('/auth/resend-otp', { email, type });
    return res.data;
  };

  const forgotPassword = async (email) => {
    const res = await apiClient.post('/auth/forgot-password', { email });
    return res.data;
  };

  const resetPassword = async (email, otp, newPassword) => {
    const res = await apiClient.post('/auth/reset-password', { email, otp, newPassword });
    return res.data;
  };

  const isAdmin = user?.role === 'ROLE_ADMIN';

  return (
    <AuthContext.Provider
      value={{
        user,
        token,
        isAdmin,
        loading,
        login,
        register,
        logout,
        verifyEmail,
        resendOtp,
        forgotPassword,
        resetPassword,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  return useContext(AuthContext);
}
