import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import apiClient from '../api/axios';
import { useCart } from '../context/CartContext';

export default function ProductListPage() {
  const { addToCart, message } = useCart();

  const [products, setProducts] = useState([]);
  const [categories, setCategories] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  // Filters State
  const [search, setSearch] = useState('');
  const [categoryId, setCategoryId] = useState('');
  const [minPrice, setMinPrice] = useState('');
  const [maxPrice, setMaxPrice] = useState('');
  const [onlyAvailable, setOnlyAvailable] = useState(false);

  // Pagination State
  const [currentPage, setCurrentPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const pageSize = 12;

  // Load Categories on mount
  useEffect(() => {
    async function loadCategories() {
      try {
        const res = await apiClient.get('/categories');
        setCategories(Array.isArray(res.data) ? res.data : res.data.content || []);
      } catch (err) {
        console.warn('Could not load categories:', err.message);
      }
    }
    loadCategories();
  }, []);

  // Load Products whenever filters or page change
  useEffect(() => {
    loadProducts();
  }, [currentPage, categoryId, onlyAvailable]);

  const loadProducts = async () => {
    try {
      setLoading(true);
      setError(null);

      const params = {
        page: currentPage,
        size: pageSize,
      };

      if (search.trim()) params.search = search.trim();
      if (categoryId) params.categoryId = categoryId;
      if (minPrice) params.minPrice = minPrice;
      if (maxPrice) params.maxPrice = maxPrice;
      if (onlyAvailable) params.onlyAvailable = true;

      const res = await apiClient.get('/products', { params });
      const data = res.data;

      // Handle Spring Page<ProductResponseDTO>
      if (data.content !== undefined) {
        setProducts(data.content);
        setTotalPages(data.totalPages);
        setTotalElements(data.totalElements);
      } else if (Array.isArray(data)) {
        setProducts(data);
        setTotalPages(1);
        setTotalElements(data.length);
      }
    } catch (err) {
      setError(err.message || 'Failed to load products from server.');
    } finally {
      setLoading(false);
    }
  };

  const handleSearchSubmit = (e) => {
    e.preventDefault();
    setCurrentPage(0);
    loadProducts();
  };

  const handleResetFilters = () => {
    setSearch('');
    setCategoryId('');
    setMinPrice('');
    setMaxPrice('');
    setOnlyAvailable(false);
    setCurrentPage(0);
  };

  return (
    <div className="container-custom py-8">
      
      {/* Toast / Global Cart Message */}
      {message && (
        <div className={`mb-6 p-4 rounded-md text-sm font-medium border ${
          message.type === 'error'
            ? 'bg-red-50 text-red-700 border-red-200'
            : 'bg-green-50 text-green-700 border-green-200'
        }`}>
          {message.text}
        </div>
      )}

      {/* Hero Header */}
      <div className="bg-white p-6 rounded-lg border border-slate-200 shadow-xs mb-8">
        <h1 className="text-2xl font-bold text-slate-900 mb-2">Product Catalog</h1>
        <p className="text-sm text-slate-600">
          Browse our full collection of products, filtered by category and price range.
        </p>
      </div>

      {/* Filter Toolbar */}
      <div className="bg-white p-5 rounded-lg border border-slate-200 shadow-xs mb-8">
        <form onSubmit={handleSearchSubmit} className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-5 gap-3">
          
          {/* Search Input */}
          <div className="lg:col-span-2">
            <label className="block text-xs font-semibold text-slate-700 mb-1">Search Products</label>
            <input
              type="text"
              placeholder="Search by name or description..."
              value={search}
              onChange={(e) => setSearch(e.target.value)}
              className="input-field text-xs"
            />
          </div>

          {/* Category Dropdown */}
          <div>
            <label className="block text-xs font-semibold text-slate-700 mb-1">Category</label>
            <select
              value={categoryId}
              onChange={(e) => {
                setCategoryId(e.target.value);
                setCurrentPage(0);
              }}
              className="input-field text-xs"
            >
              <option value="">All Categories</option>
              {categories.map((c) => (
                <option key={c.id} value={c.id}>{c.name}</option>
              ))}
            </select>
          </div>

          {/* Price Range */}
          <div className="flex gap-2">
            <div className="flex-1">
              <label className="block text-xs font-semibold text-slate-700 mb-1">Min $</label>
              <input
                type="number"
                placeholder="0"
                value={minPrice}
                onChange={(e) => setMinPrice(e.target.value)}
                className="input-field text-xs"
              />
            </div>
            <div className="flex-1">
              <label className="block text-xs font-semibold text-slate-700 mb-1">Max $</label>
              <input
                type="number"
                placeholder="1000"
                value={maxPrice}
                onChange={(e) => setMaxPrice(e.target.value)}
                className="input-field text-xs"
              />
            </div>
          </div>

          {/* Filter Buttons */}
          <div className="flex items-end gap-2">
            <button type="submit" className="btn btn-primary text-xs flex-1">
              Filter
            </button>
            <button
              type="button"
              onClick={handleResetFilters}
              className="btn btn-secondary text-xs"
            >
              Reset
            </button>
          </div>

        </form>

        {/* In-Stock Checkbox */}
        <div className="mt-3 pt-3 border-t border-slate-100 flex items-center">
          <label className="flex items-center gap-2 text-xs font-medium text-slate-700 cursor-pointer">
            <input
              type="checkbox"
              checked={onlyAvailable}
              onChange={(e) => {
                setOnlyAvailable(e.target.checked);
                setCurrentPage(0);
              }}
              className="rounded text-blue-600"
            />
            <span>Show In-Stock Products Only</span>
          </label>
        </div>
      </div>

      {/* Error Banner */}
      {error && (
        <div className="bg-red-50 border border-red-200 text-red-700 p-4 rounded-md text-sm mb-6">
          <p className="font-semibold">Failed to connect to backend</p>
          <p className="text-xs mt-1">{error}</p>
        </div>
      )}

      {/* Loading Indicator */}
      {loading ? (
        <div className="text-center py-16 text-slate-500 text-sm">
          Loading products from API...
        </div>
      ) : products.length === 0 ? (
        <div className="bg-white p-12 rounded-lg border border-slate-200 text-center">
          <p className="text-base font-semibold text-slate-800 mb-1">No products found</p>
          <p className="text-xs text-slate-500">
            Try clearing or adjusting your search filters.
          </p>
        </div>
      ) : (
        /* Product Cards Grid */
        <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-6">
          {products.map((product) => {
            const isOutOfStock = product.stockQuantity <= 0;
            const primaryImage = product.imageUrls && product.imageUrls.length > 0 ? product.imageUrls[0] : null;

            return (
              <div key={product.id} className="card flex flex-col justify-between overflow-hidden hover:shadow-md transition-shadow">
                
                {/* Image & Category */}
                <div>
                  <Link to={`/products/${product.id}`} className="block relative aspect-[4/3] bg-slate-100 overflow-hidden group">
                    {primaryImage ? (
                      <img
                        src={primaryImage}
                        alt={product.name}
                        className="w-full h-full object-cover group-hover:scale-105 transition-transform duration-300"
                        onError={(e) => {
                          e.currentTarget.style.display = 'none';
                          const fallback = e.currentTarget.parentElement.querySelector('.image-fallback');
                          if (fallback) fallback.style.display = 'flex';
                        }}
                      />
                    ) : null}
                    
                    <div
                      className={`image-fallback w-full h-full bg-gradient-to-br from-slate-100 to-slate-200 flex flex-col items-center justify-center text-slate-400 p-4 ${
                        primaryImage ? 'hidden' : 'flex'
                      }`}
                    >
                      <svg className="w-10 h-10 mb-1 opacity-40" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="1.5" d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z" />
                      </svg>
                      <span className="text-[10px] font-medium tracking-wide text-slate-400">No Image Available</span>
                    </div>

                    {product.categoryName && (
                      <span className="absolute top-2 right-2 bg-slate-900/80 backdrop-blur-xs text-white text-[10px] font-semibold px-2 py-0.5 rounded shadow-xs">
                        {product.categoryName}
                      </span>
                    )}
                  </Link>

                  {/* Body Details */}
                  <div className="p-4">
                    <div className="flex items-center justify-between text-xs text-slate-500 mb-1">
                      <span>Rating: ⭐ {product.averageRating ? product.averageRating.toFixed(1) : 'N/A'}</span>
                      <span className={isOutOfStock ? 'text-red-600 font-semibold' : 'text-green-600 font-semibold'}>
                        {isOutOfStock ? 'Out of Stock' : `${product.stockQuantity} in stock`}
                      </span>
                    </div>

                    <Link to={`/products/${product.id}`} className="block">
                      <h3 className="text-sm font-bold text-slate-900 hover:text-blue-600 transition-colors line-clamp-1 mb-1">
                        {product.name}
                      </h3>
                    </Link>

                    <p className="text-xs text-slate-600 line-clamp-2 mb-3">
                      {product.description}
                    </p>
                  </div>
                </div>

                {/* Price & Add to Cart */}
                <div className="p-4 pt-0 border-t border-slate-100 mt-2 flex items-center justify-between">
                  <span className="text-base font-bold text-slate-900">
                    ${Number(product.price).toFixed(2)}
                  </span>

                  <button
                    onClick={() => addToCart(product.id, 1)}
                    disabled={isOutOfStock}
                    className="btn btn-primary text-xs py-1.5 px-3"
                  >
                    {isOutOfStock ? 'Sold Out' : 'Add to Cart'}
                  </button>
                </div>

              </div>
            );
          })}
        </div>
      )}

      {/* Pagination Controls */}
      {totalPages > 1 && (
        <div className="flex items-center justify-center gap-2 mt-10">
          <button
            onClick={() => setCurrentPage((p) => Math.max(0, p - 1))}
            disabled={currentPage === 0}
            className="btn btn-secondary text-xs"
          >
            &larr; Previous
          </button>

          <span className="text-xs text-slate-600 px-3">
            Page {currentPage + 1} of {totalPages} ({totalElements} total)
          </span>

          <button
            onClick={() => setCurrentPage((p) => Math.min(totalPages - 1, p + 1))}
            disabled={currentPage >= totalPages - 1}
            className="btn btn-secondary text-xs"
          >
            Next &rarr;
          </button>
        </div>
      )}

    </div>
  );
}
