import React, { useState, useEffect } from 'react';
import apiClient from '../../api/axios';

export default function AdminOrdersPage() {
  const [orders, setOrders] = useState([]);
  const [statusFilter, setStatusFilter] = useState('');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [success, setSuccess] = useState(null);

  useEffect(() => {
    loadOrders();
  }, [statusFilter]);

  const loadOrders = async () => {
    try {
      setLoading(true);
      setError(null);
      const params = { size: 50 };
      if (statusFilter) params.status = statusFilter;

      const res = await apiClient.get('/admin/orders', { params });
      const data = res.data;
      setOrders(data.content || data || []);
    } catch (err) {
      setError(err.message || 'Failed to load admin orders.');
    } finally {
      setLoading(false);
    }
  };

  // State machine helper for valid transitions
  const getNextStatus = (current) => {
    switch (current) {
      case 'PLACED': return 'CONFIRMED';
      case 'CONFIRMED': return 'SHIPPED';
      case 'SHIPPED': return 'DELIVERED';
      default: return null;
    }
  };

  const handleUpdateStatus = async (orderId, newStatus) => {
    try {
      setError(null);
      // PUT /api/v1/admin/orders/{orderId}/status?newStatus=...
      await apiClient.put(`/admin/orders/${orderId}/status`, null, {
        params: { newStatus },
      });
      setSuccess(`Order #${orderId} status changed to ${newStatus}.`);
      setTimeout(() => setSuccess(null), 3500);
      loadOrders();
    } catch (err) {
      setError(err.message || 'Invalid status transition.');
    }
  };

  return (
    <div className="container-custom py-8">
      
      <div className="flex flex-wrap justify-between items-center gap-4 mb-6">
        <div>
          <h1 className="text-2xl font-bold text-slate-900">Admin: Orders Management</h1>
          <p className="text-xs text-slate-500">
            View orders across all customers and update status following the state machine.
          </p>
        </div>

        {/* Filter by Status */}
        <div className="flex items-center gap-2 text-xs">
          <label className="font-semibold text-slate-700">Filter Status:</label>
          <select
            value={statusFilter}
            onChange={(e) => setStatusFilter(e.target.value)}
            className="input-field text-xs py-1"
          >
            <option value="">ALL STATUSES</option>
            <option value="PLACED">PLACED</option>
            <option value="CONFIRMED">CONFIRMED</option>
            <option value="SHIPPED">SHIPPED</option>
            <option value="DELIVERED">DELIVERED</option>
            <option value="CANCELLED">CANCELLED</option>
          </select>
        </div>
      </div>

      {success && (
        <div className="bg-green-50 border border-green-200 text-green-700 p-3 rounded-md text-xs mb-4">
          ✓ {success}
        </div>
      )}

      {error && (
        <div className="bg-red-50 border border-red-200 text-red-700 p-3 rounded-md text-xs mb-4">
          Status Error: {error}
        </div>
      )}

      {loading ? (
        <div className="text-center py-16 text-slate-500 text-sm">Loading order registry...</div>
      ) : orders.length === 0 ? (
        <div className="card p-12 text-center text-slate-500 text-xs">
          No orders found matching the selected status.
        </div>
      ) : (
        <div className="space-y-4">
          {orders.map((order) => {
            const next = getNextStatus(order.status);

            return (
              <div key={order.id} className="card p-5">
                <div className="flex flex-wrap items-center justify-between gap-4 pb-3 border-b border-slate-100 mb-3">
                  <div className="flex items-center gap-3">
                    <span className="font-bold text-slate-900 text-sm">Order #{order.id}</span>
                    <span className="bg-slate-100 text-slate-800 text-[10px] font-bold px-2 py-0.5 rounded">
                      {order.status}
                    </span>
                    <span className="text-xs text-slate-500">
                      Total: <strong className="text-slate-900">${Number(order.totalAmount).toFixed(2)}</strong>
                    </span>
                  </div>

                  {/* Transition Workflow Actions */}
                  <div className="flex items-center gap-2">
                    {next ? (
                      <button
                        onClick={() => handleUpdateStatus(order.id, next)}
                        className="btn btn-primary text-xs py-1 px-3"
                      >
                        Advance to {next} &rarr;
                      </button>
                    ) : (
                      <span className="text-xs text-green-700 bg-green-50 px-2 py-1 rounded font-semibold">
                        ✓ Order Completed
                      </span>
                    )}

                    {order.status === 'PLACED' && (
                      <button
                        onClick={() => handleUpdateStatus(order.id, 'CANCELLED')}
                        className="btn btn-danger text-xs py-1 px-2.5"
                      >
                        Cancel
                      </button>
                    )}
                  </div>
                </div>

                {/* Items preview in order */}
                <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 gap-2 text-xs text-slate-600">
                  {(order.items || []).map((item, idx) => (
                    <div key={item.id || idx} className="p-2 bg-slate-50 rounded border border-slate-100">
                      <span className="font-semibold text-slate-800">{item.quantity}x {item.productName}</span>
                      <span className="block text-[11px] text-slate-500">${Number(item.price).toFixed(2)} each</span>
                    </div>
                  ))}
                </div>
              </div>
            );
          })}
        </div>
      )}

    </div>
  );
}
