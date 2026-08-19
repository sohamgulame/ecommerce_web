import React, { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import apiClient from '../api/axios';

export default function OrderHistoryPage() {
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const navigate = useNavigate();
  const pageSize = 10;

  useEffect(() => {
    loadOrders();
  }, [page]);

  const loadOrders = async () => {
    try {
      setLoading(true);
      setError(null);
      const res = await apiClient.get('/orders', {
        params: { page, size: pageSize },
      });
      const data = res.data;
      if (data.content !== undefined) {
        setOrders(data.content);
        setTotalPages(data.totalPages);
        setTotalElements(data.totalElements || 0);
      } else if (Array.isArray(data)) {
        setOrders(data);
        setTotalPages(1);
        setTotalElements(data.length);
      }
    } catch (err) {
      setError(err.message || 'Failed to load order history.');
    } finally {
      setLoading(false);
    }
  };

  const getStatusBadge = (status) => {
    switch (status) {
      case 'DELIVERED':
        return <span className="bg-green-100 text-green-800 text-[10px] font-bold px-2 py-0.5 rounded">DELIVERED</span>;
      case 'SHIPPED':
        return <span className="bg-blue-100 text-blue-800 text-[10px] font-bold px-2 py-0.5 rounded">SHIPPED</span>;
      case 'CONFIRMED':
        return <span className="bg-purple-100 text-purple-800 text-[10px] font-bold px-2 py-0.5 rounded">CONFIRMED</span>;
      case 'CANCELLED':
        return <span className="bg-red-100 text-red-800 text-[10px] font-bold px-2 py-0.5 rounded">CANCELLED</span>;
      case 'PLACED':
      default:
        return <span className="bg-amber-100 text-amber-800 text-[10px] font-bold px-2 py-0.5 rounded">PLACED</span>;
    }
  };

  return (
    <div className="container-custom py-8">
      
      <div className="mb-6">
        <h1 className="text-2xl font-bold text-slate-900">My Order History</h1>
        <p className="text-xs text-slate-500">
          View all your previous purchases and current delivery status.
        </p>
      </div>

      {error && (
        <div className="bg-red-50 border border-red-200 text-red-700 p-4 rounded-md text-xs mb-6">
          {error}
        </div>
      )}

      {loading ? (
        <div className="text-center py-16 text-slate-500 text-sm">
          Loading orders...
        </div>
      ) : orders.length === 0 ? (
        <div className="card p-12 text-center max-w-md mx-auto">
          <p className="text-base font-bold text-slate-800 mb-2">No orders found</p>
          <p className="text-xs text-slate-500 mb-6">
            You haven't placed any orders yet.
          </p>
          <Link to="/" className="btn btn-primary text-xs">
            Start Shopping
          </Link>
        </div>
      ) : (
        <div className="space-y-4">
          {orders.map((order, index) => {
            // Chronological numbering: oldest = #1, newest = #N
            const userOrderNumber = page * pageSize + index + 1;
            return (
            <div key={order.id} className="card p-5 hover:border-slate-300 transition-colors">
              <div className="flex flex-wrap items-center justify-between gap-3 pb-3 border-b border-slate-100">
                <div className="flex items-center gap-3">
                  <span className="font-bold text-slate-900 text-sm">
                    Order #{userOrderNumber}
                  </span>
                  {getStatusBadge(order.status)}
                </div>

                <div className="text-xs text-slate-500">
                  {order.createdAt ? new Date(order.createdAt).toLocaleDateString() : 'Recent'}
                </div>
              </div>

              {/* Items preview */}
              <div className="py-3 space-y-1 text-xs text-slate-700">
                {(order.items || []).map((item, idx) => (
                  <div key={item.id || idx} className="flex justify-between">
                    <span>
                      {item.quantity}x {item.productName}
                    </span>
                    <span className="font-medium">
                      ${Number(item.price * item.quantity).toFixed(2)}
                    </span>
                  </div>
                ))}
              </div>

              {/* Total and Detail Link */}
              <div className="pt-3 border-t border-slate-100 flex items-center justify-between text-xs">
                <div>
                  <span className="text-slate-500">Total: </span>
                  <span className="font-bold text-slate-900">${Number(order.totalAmount).toFixed(2)}</span>
                </div>

                <Link
                  to={`/orders/${order.id}`}
                  state={{ userOrderNumber }}
                  className="text-blue-600 hover:underline font-semibold"
                >
                  View Details &rarr;
                </Link>
              </div>
            </div>
            );
          })}

          {/* Pagination */}
          {totalPages > 1 && (
            <div className="flex justify-center gap-2 pt-4">
              <button
                onClick={() => setPage((p) => Math.max(0, p - 1))}
                disabled={page === 0}
                className="btn btn-secondary text-xs"
              >
                &larr; Previous
              </button>
              <button
                onClick={() => setPage((p) => Math.min(totalPages - 1, p + 1))}
                disabled={page >= totalPages - 1}
                className="btn btn-secondary text-xs"
              >
                Next &rarr;
              </button>
            </div>
          )}
        </div>
      )}

    </div>
  );
}
