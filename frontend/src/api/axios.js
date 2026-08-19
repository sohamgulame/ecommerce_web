import axios from 'axios';

const getBaseUrl = () => {
  if (import.meta.env.VITE_API_URL) return import.meta.env.VITE_API_URL;
  const host = typeof window !== 'undefined' ? window.location.hostname : 'localhost';
  return `http://${host}:8080/api/v1`;
};

const apiClient = axios.create({
  baseURL: getBaseUrl(),
  headers: {
    'Content-Type': 'application/json',
  },
});

// Request Interceptor: Attach JWT Token
apiClient.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// Response Interceptor: Handle 401s via silent refresh-and-retry, extract backend error message cleanly
apiClient.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config;
    const status = error.response?.status;

    // Check if error is 401 and not already retried, and not an auth endpoint (to avoid infinite loops)
    const isAuthUrl = originalRequest?.url?.includes('/auth/login') ||
                      originalRequest?.url?.includes('/auth/refresh') ||
                      originalRequest?.url?.includes('/auth/register');

    if (status === 401 && originalRequest && !originalRequest._retry && !isAuthUrl) {
      originalRequest._retry = true;
      const storedRefreshToken = localStorage.getItem('refreshToken');

      if (storedRefreshToken) {
        try {
          // Use vanilla axios to avoid triggering this interceptor recursively
          const refreshRes = await axios.post(`${getBaseUrl()}/auth/refresh`, {
            refreshToken: storedRefreshToken,
          });

          const { accessToken, refreshToken: newRefreshToken } = refreshRes.data;

          if (accessToken) {
            localStorage.setItem('token', accessToken);
            if (newRefreshToken) {
              localStorage.setItem('refreshToken', newRefreshToken);
            }

            // Notify React context of token change
            window.dispatchEvent(
              new CustomEvent('auth-tokens-updated', {
                detail: { token: accessToken, refreshToken: newRefreshToken },
              })
            );

            // Retry original request with new access token
            originalRequest.headers.Authorization = `Bearer ${accessToken}`;
            return apiClient(originalRequest);
          }
        } catch (refreshErr) {
          // Refresh failed (token revoked or expired) -> clear session
          localStorage.removeItem('token');
          localStorage.removeItem('refreshToken');
          localStorage.removeItem('user');
          window.dispatchEvent(new Event('auth-logout'));
        }
      }
    }

    // Extract backend error message cleanly
    const backendMessage = error.response?.data?.message || error.response?.data?.error;
    const customError = new Error(backendMessage || error.message || 'An error occurred');
    customError.status = status;
    customError.data = error.response?.data;
    return Promise.reject(customError);
  }
);

export default apiClient;
