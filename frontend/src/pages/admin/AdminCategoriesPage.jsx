import React, { useState, useEffect } from 'react';
import apiClient from '../../api/axios';

export default function AdminCategoriesPage() {
  const [categories, setCategories] = useState([]);
  const [name, setName] = useState('');
  const [description, setDescription] = useState('');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [success, setSuccess] = useState(null);

  useEffect(() => {
    loadCategories();
  }, []);

  const loadCategories = async () => {
    try {
      setLoading(true);
      setError(null);
      const res = await apiClient.get('/categories');
      setCategories(Array.isArray(res.data) ? res.data : res.data.content || []);
    } catch (err) {
      setError(err.message || 'Failed to load categories.');
    } finally {
      setLoading(false);
    }
  };

  const handleCreateCategory = async (e) => {
    e.preventDefault();
    if (!name.trim()) return;

    try {
      setError(null);
      await apiClient.post('/categories', {
        name: name.trim(),
        description: description.trim(),
      });
      setSuccess(`Category "${name}" created successfully.`);
      setName('');
      setDescription('');
      setTimeout(() => setSuccess(null), 3500);
      loadCategories();
    } catch (err) {
      setError(err.message || 'Failed to create category.');
    }
  };

  const handleDeleteCategory = async (id, catName) => {
    if (!window.confirm(`Delete category "${catName}"?`)) return;

    try {
      setError(null);
      await apiClient.delete(`/categories/${id}`);
      setSuccess(`Category "${catName}" deleted.`);
      setTimeout(() => setSuccess(null), 3500);
      loadCategories();
    } catch (err) {
      setError(err.message || 'Failed to delete category.');
    }
  };

  return (
    <div className="container-custom py-8">
      
      <div className="mb-6">
        <h1 className="text-2xl font-bold text-slate-900">Admin: Category Management</h1>
        <p className="text-xs text-slate-500">
          Create and manage product taxonomy categories.
        </p>
      </div>

      {success && (
        <div className="bg-green-50 border border-green-200 text-green-700 p-3 rounded-md text-xs mb-4">
          ✓ {success}
        </div>
      )}

      {error && (
        <div className="bg-red-50 border border-red-200 text-red-700 p-3 rounded-md text-xs mb-4">
          Error: {error}
        </div>
      )}

      <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
        
        {/* Create Category Form */}
        <div className="card p-6 h-fit">
          <h2 className="text-sm font-bold text-slate-900 mb-3 pb-2 border-b border-slate-100">
            Create New Category
          </h2>

          <form onSubmit={handleCreateCategory} className="space-y-3 text-xs">
            <div>
              <label className="block font-semibold text-slate-700 mb-1">Category Name</label>
              <input
                type="text"
                required
                placeholder="e.g. Smart Home"
                value={name}
                onChange={(e) => setName(e.target.value)}
                className="input-field text-xs"
              />
            </div>

            <div>
              <label className="block font-semibold text-slate-700 mb-1">Description</label>
              <textarea
                rows="3"
                placeholder="Brief category description..."
                value={description}
                onChange={(e) => setDescription(e.target.value)}
                className="input-field text-xs resize-none"
              />
            </div>

            <button type="submit" className="btn btn-primary w-full text-xs py-2">
              Save Category
            </button>
          </form>
        </div>

        {/* Existing Categories Table */}
        <div className="md:col-span-2 card overflow-hidden">
          <div className="p-4 bg-slate-50 border-b border-slate-200">
            <h2 className="text-sm font-bold text-slate-900">Existing Categories ({categories.length})</h2>
          </div>

          {loading ? (
            <div className="text-center py-12 text-slate-500 text-xs">Loading categories...</div>
          ) : categories.length === 0 ? (
            <div className="p-8 text-center text-slate-500 text-xs">No categories found.</div>
          ) : (
            <table className="w-full text-left text-xs border-collapse">
              <thead className="bg-slate-50 text-slate-600 border-b border-slate-200 uppercase font-semibold">
                <tr>
                  <th className="p-3">ID</th>
                  <th className="p-3">Name</th>
                  <th className="p-3">Description</th>
                  <th className="p-3 text-right">Action</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100">
                {categories.map((c) => (
                  <tr key={c.id} className="hover:bg-slate-50/50">
                    <td className="p-3 font-mono text-slate-500">{c.id}</td>
                    <td className="p-3 font-bold text-slate-900">{c.name}</td>
                    <td className="p-3 text-slate-600">{c.description || '—'}</td>
                    <td className="p-3 text-right">
                      <button
                        onClick={() => handleDeleteCategory(c.id, c.name)}
                        className="text-red-600 hover:underline font-medium"
                      >
                        Delete
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </div>

      </div>

    </div>
  );
}
