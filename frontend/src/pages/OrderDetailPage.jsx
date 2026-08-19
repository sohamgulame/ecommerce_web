import React, { useState, useEffect } from 'react';
import { useParams, Link, useLocation } from 'react-router-dom';
import apiClient from '../api/axios';

export default function OrderDetailPage() {
  const { id } = useParams();
  const [order, setOrder] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const location = useLocation();
  const userOrderNumber = location.state?.userOrderNumber;

  useEffect(() => {
    loadOrder();
  }, [id]);

  const loadOrder = async () => {
    try {
      setLoading(true);
      setError(null);
      const res = await apiClient.get(`/orders/${id}`);
      setOrder(res.data);
    } catch (err) {
      setError(err.message || 'Order not found or unauthorized.');
    } finally {
      setLoading(false);
    }
  };

  if (loading) {
    return (
      <div className="container-custom py-16 text-center text-slate-500 text-sm">
        Loading order details...
      </div>
    );
  }

  if (error || !order) {
    return (
      <div className="container-custom py-16 text-center">
        <div className="card p-8 max-w-md mx-auto text-red-600 bg-red-50/50">
          <h2 className="font-bold text-sm mb-2">Error Loading Order</h2>
          <p className="text-xs mb-4">{error || 'Order could not be loaded.'}</p>
          <Link to="/orders" className="btn btn-secondary text-xs">
            Back to Orders
          </Link>
        </div>
      </div>
    );
  }

  return (
    <div className="container-custom py-8">
      
      <div className="text-xs text-slate-500 mb-4">
        <Link to="/orders" className="hover:underline">&larr; Back to Order History</Link>
      </div>

      <div className="card p-6 md:p-8 max-w-3xl mx-auto">
        
        {/* Top Header */}
        <div className="flex flex-wrap justify-between items-center gap-4 pb-4 border-b border-slate-100 mb-6">
          <div>
            <h1 className="text-xl font-bold text-slate-900">Order #{userOrderNumber || order.id}</h1>
            <p className="text-xs text-slate-500">
              Placed on: {order.createdAt ? new Date(order.createdAt).toLocaleString() : 'N/A'}
            </p>
          </div>

          <div className="text-right">
            <span className="text-xs font-semibold text-slate-500 block mb-1">Status</span>
            <span className="bg-blue-50 text-blue-700 border border-blue-200 text-xs font-bold px-3 py-1 rounded">
              {order.status}
            </span>
          </div>
        </div>

        {/* Snapshot Items Table */}
        <h3 className="text-sm font-bold text-slate-900 mb-3">Order Items (Purchased Snapshot)</h3>
        <div className="border border-slate-200 rounded-md overflow-hidden mb-6">
          <table className="w-full text-left text-xs border-collapse">
            <thead className="bg-slate-50 text-slate-600 border-b border-slate-200 uppercase font-semibold">
              <tr>
                <th className="p-3">Product Name</th>
                <th className="p-3 text-center">Qty</th>
                <th className="p-3 text-right">Unit Price</th>
                <th className="p-3 text-right">Subtotal</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100">
              {(order.items || []).map((item, idx) => (
                <tr key={item.id || idx}>
                  <td className="p-3 font-medium text-slate-800">{item.productName}</td>
                  <td className="p-3 text-center">{item.quantity}</td>
                  <td className="p-3 text-right">${Number(item.price).toFixed(2)}</td>
                  <td className="p-3 text-right font-bold text-slate-900">
                    ${Number(item.price * item.quantity).toFixed(2)}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>

        {/* Total summary */}
        <div className="bg-slate-50 p-4 rounded-md border border-slate-200 flex justify-between items-center text-sm font-bold text-slate-900">
          <span>Total Paid:</span>
          <span className="text-blue-600 text-base">${Number(order.totalAmount).toFixed(2)}</span>
        </div>

      </div>

    </div>
  );
}
