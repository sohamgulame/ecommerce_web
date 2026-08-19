import React, { useState, useEffect, useRef } from 'react';
import apiClient from '../../api/axios';

export default function AdminProductsPage() {
  const [products, setProducts] = useState([]);
  const [categories, setCategories] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [successMsg, setSuccessMsg] = useState(null);

  // Modal / Form state
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [editingId, setEditingId] = useState(null);
  const [uploadingImage, setUploadingImage] = useState(false);
  const [imageError, setImageError] = useState(null);
  const fileInputRef = useRef(null);

  const [formData, setFormData] = useState({
    name: '',
    description: '',
    price: '',
    stockQuantity: '',
    categoryId: '',
    imageUrls: [],
  });

  useEffect(() => {
    loadData();
  }, []);

  const loadData = async () => {
    try {
      setLoading(true);
      setError(null);

      // 1. Fetch categories
      const catRes = await apiClient.get('/categories');
      const catList = Array.isArray(catRes.data) ? catRes.data : catRes.data.content || [];
      setCategories(catList);

      // 2. Fetch products
      const prodRes = await apiClient.get('/products', { params: { size: 100 } });
      const prodList = prodRes.data.content || prodRes.data || [];
      setProducts(prodList);
    } catch (err) {
      setError(err.message || 'Failed to load products.');
    } finally {
      setLoading(false);
    }
  };

  const showSuccess = (msg) => {
    setSuccessMsg(msg);
    setTimeout(() => setSuccessMsg(null), 3500);
  };

  const handleOpenCreateModal = () => {
    setEditingId(null);
    setImageError(null);
    setFormData({
      name: '',
      description: '',
      price: '',
      stockQuantity: '',
      categoryId: categories[0]?.id || '',
      imageUrls: [],
    });
    setIsModalOpen(true);
  };

  const handleOpenEditModal = (p) => {
    setEditingId(p.id);
    setImageError(null);
    const cat = categories.find((c) => c.name === p.categoryName);
    setFormData({
      name: p.name,
      description: p.description,
      price: p.price,
      stockQuantity: p.stockQuantity,
      categoryId: cat ? cat.id : categories[0]?.id || '',
      imageUrls: Array.isArray(p.imageUrls) ? [...p.imageUrls] : [],
    });
    setIsModalOpen(true);
  };

  const handleFileUpload = async (e) => {
    const file = e.target.files?.[0];
    if (!file) return;

    if (!file.type.startsWith('image/')) {
      setImageError('Please select a valid image file (JPG, PNG, WebP, GIF).');
      return;
    }

    if (file.size > 5 * 1024 * 1024) {
      setImageError('Image size exceeds maximum limit of 5MB.');
      return;
    }

    setImageError(null);
    setUploadingImage(true);

    try {
      const data = new FormData();
      data.append('file', file);
      data.append('folder', 'products');

      const res = await apiClient.post('/files/upload', data, {
        headers: { 'Content-Type': 'multipart/form-data' },
      });

      const uploadedUrl = res.data.imageUrl;
      if (uploadedUrl) {
        setFormData((prev) => ({
          ...prev,
          imageUrls: [...prev.imageUrls, uploadedUrl],
        }));
        showSuccess('Image uploaded to Cloudinary successfully.');
      }
    } catch (err) {
      setImageError(err.response?.data?.message || err.message || 'Failed to upload image to Cloudinary.');
    } finally {
      setUploadingImage(false);
      if (fileInputRef.current) {
        fileInputRef.current.value = '';
      }
    }
  };

  const handleRemoveImage = (indexToRemove) => {
    setFormData((prev) => ({
      ...prev,
      imageUrls: prev.imageUrls.filter((_, idx) => idx !== indexToRemove),
    }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      const payload = {
        name: formData.name.trim(),
        description: formData.description.trim(),
        price: parseFloat(formData.price),
        stockQuantity: parseInt(formData.stockQuantity, 10),
        categoryId: parseInt(formData.categoryId, 10),
        imageUrls: formData.imageUrls,
      };

      if (editingId) {
        await apiClient.put(`/products/${editingId}`, payload);
        showSuccess('Product updated successfully.');
      } else {
        await apiClient.post('/products', payload);
        showSuccess('Product created successfully.');
      }

      setIsModalOpen(false);
      loadData();
    } catch (err) {
      setError(err.response?.data?.message || err.message || 'Failed to save product.');
    }
  };

  const handleDelete = async (id, name) => {
    if (!window.confirm(`Are you sure you want to delete product "${name}"?`)) return;
    try {
      await apiClient.delete(`/products/${id}`);
      showSuccess(`Product "${name}" deleted.`);
      loadData();
    } catch (err) {
      setError(err.response?.data?.message || err.message || 'Failed to delete product.');
    }
  };

  return (
    <div className="container-custom py-8">
      
      <div className="flex flex-wrap items-center justify-between gap-4 mb-6">
        <div>
          <h1 className="text-2xl font-bold text-slate-900">Admin: Product Management</h1>
          <p className="text-xs text-slate-500">
            Create, update stock/price, manage Cloudinary product images, and delete catalog items.
          </p>
        </div>

        <button onClick={handleOpenCreateModal} className="btn btn-primary text-xs">
          + Add New Product
        </button>
      </div>

      {successMsg && (
        <div className="bg-green-50 border border-green-200 text-green-700 p-3 rounded-md text-xs mb-4">
          ✓ {successMsg}
        </div>
      )}

      {error && (
        <div className="bg-red-50 border border-red-200 text-red-700 p-3 rounded-md text-xs mb-4">
          Error: {error}
        </div>
      )}

      {/* Products Table */}
      {loading ? (
        <div className="text-center py-16 text-slate-500 text-sm">Loading product ledger...</div>
      ) : products.length === 0 ? (
        <div className="card p-12 text-center text-slate-500 text-xs">
          No products in the database yet. Click "Add New Product" to create one.
        </div>
      ) : (
        <div className="card overflow-x-auto">
          <table className="w-full text-left text-xs border-collapse">
            <thead className="bg-slate-50 text-slate-600 border-b border-slate-200 uppercase font-semibold">
              <tr>
                <th className="p-3.5">ID</th>
                <th className="p-3.5">Preview</th>
                <th className="p-3.5">Name</th>
                <th className="p-3.5">Category</th>
                <th className="p-3.5">Price</th>
                <th className="p-3.5">Stock</th>
                <th className="p-3.5 text-right">Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100">
              {products.map((p) => {
                const primaryImage = p.imageUrls && p.imageUrls.length > 0 ? p.imageUrls[0] : null;
                return (
                  <tr key={p.id} className="hover:bg-slate-50/50">
                    <td className="p-3.5 font-mono text-slate-500">{p.id}</td>
                    <td className="p-3.5">
                      {primaryImage ? (
                        <img
                          src={primaryImage}
                          alt={p.name}
                          className="w-10 h-10 object-cover rounded-md border border-slate-200"
                        />
                      ) : (
                        <div className="w-10 h-10 bg-slate-100 rounded-md border border-slate-200 flex items-center justify-center text-[10px] text-slate-400 font-mono">
                          N/A
                        </div>
                      )}
                    </td>
                    <td className="p-3.5 font-semibold text-slate-900">{p.name}</td>
                    <td className="p-3.5 text-slate-600">{p.categoryName || 'N/A'}</td>
                    <td className="p-3.5 font-medium text-slate-800">${Number(p.price).toFixed(2)}</td>
                    <td className="p-3.5">
                      <span className={`font-semibold ${p.stockQuantity <= 0 ? 'text-red-600' : 'text-green-600'}`}>
                        {p.stockQuantity}
                      </span>
                    </td>
                    <td className="p-3.5 text-right space-x-3">
                      <button
                        onClick={() => handleOpenEditModal(p)}
                        className="text-blue-600 hover:underline font-medium"
                      >
                        Edit
                      </button>
                      <button
                        onClick={() => handleDelete(p.id, p.name)}
                        className="text-red-600 hover:underline font-medium"
                      >
                        Delete
                      </button>
                    </td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        </div>
      )}

      {/* Create / Edit Modal */}
      {isModalOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-900/40 backdrop-blur-xs">
          <div className="card p-6 max-w-lg w-full max-h-[90vh] overflow-y-auto">
            <h2 className="text-base font-bold text-slate-900 mb-4 pb-2 border-b border-slate-100">
              {editingId ? 'Edit Product' : 'Add New Product'}
            </h2>

            <form onSubmit={handleSubmit} className="space-y-3.5 text-xs">
              <div>
                <label className="block font-semibold text-slate-700 mb-1">Product Title</label>
                <input
                  type="text"
                  required
                  value={formData.name}
                  onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                  className="input-field text-xs"
                />
              </div>

              <div>
                <label className="block font-semibold text-slate-700 mb-1">Description</label>
                <textarea
                  rows="3"
                  required
                  value={formData.description}
                  onChange={(e) => setFormData({ ...formData, description: e.target.value })}
                  className="input-field text-xs resize-none"
                />
              </div>

              <div className="grid grid-cols-3 gap-3">
                <div>
                  <label className="block font-semibold text-slate-700 mb-1">Price ($)</label>
                  <input
                    type="number"
                    step="0.01"
                    required
                    value={formData.price}
                    onChange={(e) => setFormData({ ...formData, price: e.target.value })}
                    className="input-field text-xs"
                  />
                </div>
                <div>
                  <label className="block font-semibold text-slate-700 mb-1">Stock Qty</label>
                  <input
                    type="number"
                    required
                    value={formData.stockQuantity}
                    onChange={(e) => setFormData({ ...formData, stockQuantity: e.target.value })}
                    className="input-field text-xs"
                  />
                </div>
                <div>
                  <label className="block font-semibold text-slate-700 mb-1">Category</label>
                  <select
                    value={formData.categoryId}
                    onChange={(e) => setFormData({ ...formData, categoryId: e.target.value })}
                    className="input-field text-xs"
                  >
                    {categories.map((c) => (
                      <option key={c.id} value={c.id}>{c.name}</option>
                    ))}
                  </select>
                </div>
              </div>

              {/* Cloudinary Image Upload Section */}
              <div className="pt-2 border-t border-slate-100">
                <label className="block font-semibold text-slate-700 mb-1">
                  Product Images (Cloudinary)
                </label>

                {/* Upload Trigger */}
                <div className="flex items-center gap-3 mb-2">
                  <input
                    type="file"
                    ref={fileInputRef}
                    accept="image/jpeg,image/png,image/webp,image/gif"
                    onChange={handleFileUpload}
                    className="hidden"
                    id="admin-product-file-input"
                  />
                  <label
                    htmlFor="admin-product-file-input"
                    className={`btn btn-secondary text-xs cursor-pointer inline-flex items-center gap-2 ${
                      uploadingImage ? 'opacity-50 pointer-events-none' : ''
                    }`}
                  >
                    {uploadingImage ? (
                      <>
                        <svg className="animate-spin h-3.5 w-3.5 text-slate-600" fill="none" viewBox="0 0 24 24">
                          <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
                          <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8v8H4z"></path>
                        </svg>
                        Uploading to Cloudinary...
                      </>
                    ) : (
                      <>📁 Upload Image to Cloudinary</>
                    )}
                  </label>
                  <span className="text-[11px] text-slate-400">Max 5MB (JPG, PNG, WebP)</span>
                </div>

                {imageError && (
                  <div className="text-[11px] text-red-600 mb-2 font-medium">
                    ⚠️ {imageError}
                  </div>
                )}

                {/* Thumbnail Previews */}
                {formData.imageUrls.length > 0 ? (
                  <div className="flex flex-wrap gap-2.5 p-2 bg-slate-50 rounded-md border border-slate-200">
                    {formData.imageUrls.map((url, idx) => (
                      <div key={idx} className="relative group w-16 h-16 rounded-md overflow-hidden border border-slate-200 bg-white">
                        <img src={url} alt={`Preview ${idx + 1}`} className="w-full h-full object-cover" />
                        <button
                          type="button"
                          onClick={() => handleRemoveImage(idx)}
                          className="absolute top-1 right-1 bg-red-600 text-white rounded-full w-4 h-4 flex items-center justify-center text-[10px] opacity-90 hover:opacity-100 shadow-sm"
                          title="Remove image"
                        >
                          ✕
                        </button>
                      </div>
                    ))}
                  </div>
                ) : (
                  <p className="text-[11px] text-slate-400 italic">
                    No images uploaded yet. Upload images above to attach them to this product.
                  </p>
                )}
              </div>

              <div className="pt-3 border-t border-slate-100 flex justify-end gap-2">
                <button
                  type="button"
                  onClick={() => setIsModalOpen(false)}
                  className="btn btn-secondary text-xs"
                >
                  Cancel
                </button>
                <button type="submit" disabled={uploadingImage} className="btn btn-primary text-xs">
                  {editingId ? 'Save Changes' : 'Create Product'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

    </div>
  );
}
