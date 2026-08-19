import React, { useState, useEffect } from 'react';
import { useParams, Link, useNavigate } from 'react-router-dom';
import apiClient from '../api/axios';
import { useCart } from '../context/CartContext';
import { useAuth } from '../context/AuthContext';

export default function ProductDetailPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const { addToCart } = useCart();
  const { user } = useAuth();

  const [product, setProduct] = useState(null);
  const [reviews, setReviews] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  // Add to cart state
  const [quantity, setQuantity] = useState(1);
  const [cartSuccess, setCartSuccess] = useState(false);
  const [cartError, setCartError] = useState(null);

  // Review Form state
  const [rating, setRating] = useState(5);
  const [comment, setComment] = useState('');
  const [submittingReview, setSubmittingReview] = useState(false);
  const [reviewError, setReviewError] = useState(null);
  const [reviewSuccess, setReviewSuccess] = useState(null);

  useEffect(() => {
    loadProductAndReviews();
  }, [id]);

  const loadProductAndReviews = async () => {
    try {
      setLoading(true);
      setError(null);

      // 1. Fetch Product
      const productRes = await apiClient.get(`/products/${id}`);
      setProduct(productRes.data);

      // 2. Fetch Reviews
      try {
        const reviewsRes = await apiClient.get(`/products/${id}/reviews`);
        setReviews(reviewsRes.data.content || reviewsRes.data || []);
      } catch (revErr) {
        console.warn('Could not load reviews:', revErr.message);
      }
    } catch (err) {
      setError(err.message || 'Product not found.');
    } finally {
      setLoading(false);
    }
  };

  const handleAddToCart = async () => {
    setCartError(null);
    setCartSuccess(false);

    const success = await addToCart(product.id, quantity);
    if (success) {
      setCartSuccess(true);
      setTimeout(() => setCartSuccess(false), 3500);
    }
  };

  const handleReviewSubmit = async (e) => {
    e.preventDefault();
    if (!user) {
      navigate('/login');
      return;
    }
    if (!comment.trim()) {
      setReviewError('Please enter a review comment.');
      return;
    }

    try {
      setSubmittingReview(true);
      setReviewError(null);
      setReviewSuccess(null);

      await apiClient.post('/reviews', {
        productId: Number(id),
        rating: Number(rating),
        comment: comment.trim(),
      });

      setReviewSuccess('Your review has been submitted successfully!');
      setComment('');
      loadProductAndReviews();
    } catch (err) {
      setReviewError(err.message || 'Failed to submit review.');
    } finally {
      setSubmittingReview(false);
    }
  };

  if (loading) {
    return (
      <div className="container-custom py-16 text-center text-slate-500 text-sm">
        Loading product details...
      </div>
    );
  }

  if (error || !product) {
    return (
      <div className="container-custom py-16 text-center">
        <div className="bg-red-50 border border-red-200 text-red-700 p-6 rounded-lg max-w-md mx-auto">
          <h2 className="text-base font-bold mb-2">Product Error</h2>
          <p className="text-xs mb-4">{error || 'Product could not be found.'}</p>
          <Link to="/" className="btn btn-secondary text-xs">
            Back to Products
          </Link>
        </div>
      </div>
    );
  }

  const isOutOfStock = product.stockQuantity <= 0;
  const mainImage = product.imageUrls && product.imageUrls.length > 0 ? product.imageUrls[0] : null;

  return (
    <div className="container-custom py-8">
      
      {/* Breadcrumb Navigation */}
      <div className="text-xs text-slate-500 mb-6 flex items-center gap-2">
        <Link to="/" className="hover:underline">Catalog</Link>
        <span>&rsaquo;</span>
        {product.categoryName && (
          <>
            <span>{product.categoryName}</span>
            <span>&rsaquo;</span>
          </>
        )}
        <span className="text-slate-800 font-semibold truncate max-w-xs">{product.name}</span>
      </div>

      {/* Main Product Card */}
      <div className="card p-6 md:p-8 mb-10 grid grid-cols-1 md:grid-cols-2 gap-8 items-start">
        
        {/* Left: Product Image */}
        <div className="rounded-lg overflow-hidden bg-slate-100 border border-slate-200 aspect-square flex items-center justify-center relative">
          {mainImage ? (
            <img
              src={mainImage}
              alt={product.name}
              className="w-full h-full object-cover"
              onError={(e) => {
                e.currentTarget.style.display = 'none';
                const fallback = e.currentTarget.parentElement.querySelector('.image-detail-fallback');
                if (fallback) fallback.style.display = 'flex';
              }}
            />
          ) : null}

          <div
            className={`image-detail-fallback w-full h-full bg-gradient-to-br from-slate-100 to-slate-200 flex flex-col items-center justify-center text-slate-400 p-8 ${
              mainImage ? 'hidden' : 'flex'
            }`}
          >
            <svg className="w-16 h-16 mb-2 opacity-40" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="1.5" d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z" />
            </svg>
            <span className="text-xs font-medium tracking-wide text-slate-400">No Image Available</span>
          </div>
        </div>

        {/* Right: Product Info & Actions */}
        <div>
          <div className="flex items-center justify-between gap-2 mb-2">
            <span className="text-xs font-semibold text-blue-600 bg-blue-50 px-2.5 py-0.5 rounded-full">
              {product.categoryName || 'General Category'}
            </span>
            <span className="text-xs font-semibold text-slate-600">
              ⭐ {product.averageRating ? product.averageRating.toFixed(1) : 'No ratings yet'}
            </span>
          </div>

          <h1 className="text-2xl font-bold text-slate-900 mb-3">{product.name}</h1>

          <div className="text-2xl font-bold text-slate-900 mb-4">
            ${Number(product.price).toFixed(2)}
          </div>

          <div className="border-t border-b border-slate-100 py-4 mb-6">
            <h3 className="text-xs font-bold uppercase text-slate-500 mb-1">Description</h3>
            <p className="text-sm text-slate-700 leading-relaxed">{product.description}</p>
          </div>

          {/* Stock Status */}
          <div className="flex items-center gap-2 mb-6">
            <span className="text-xs text-slate-500">Availability:</span>
            <span className={`text-xs font-bold ${isOutOfStock ? 'text-red-600' : 'text-green-600'}`}>
              {isOutOfStock ? 'Out of Stock' : `${product.stockQuantity} units in stock`}
            </span>
          </div>

          {/* Cart Feedback Alerts */}
          {cartSuccess && (
            <div className="bg-green-50 border border-green-200 text-green-700 p-3 rounded-md text-xs mb-4">
              ✓ Successfully added to your cart!
            </div>
          )}
          {cartError && (
            <div className="bg-red-50 border border-red-200 text-red-700 p-3 rounded-md text-xs mb-4">
              {cartError}
            </div>
          )}

          {/* Quantity and Add to Cart */}
          <div className="flex items-center gap-4">
            <div className="w-24">
              <label className="block text-xs font-semibold text-slate-700 mb-1">Quantity</label>
              <input
                type="number"
                min="1"
                max={product.stockQuantity || 1}
                value={quantity}
                onChange={(e) => setQuantity(Math.max(1, parseInt(e.target.value) || 1))}
                disabled={isOutOfStock}
                className="input-field text-xs text-center"
              />
            </div>

            <button
              onClick={handleAddToCart}
              disabled={isOutOfStock}
              className="btn btn-primary flex-1 mt-5"
            >
              {isOutOfStock ? 'Sold Out' : 'Add to Cart'}
            </button>
          </div>

        </div>

      </div>

      {/* Reviews Section */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
        
        {/* Review List */}
        <div className="md:col-span-2 card p-6">
          <h2 className="text-lg font-bold text-slate-900 mb-4">
            Customer Reviews ({reviews.length})
          </h2>

          {reviews.length === 0 ? (
            <p className="text-xs text-slate-500 py-6 text-center">
              No customer reviews yet. Be the first to review this product!
            </p>
          ) : (
            <div className="space-y-4">
              {reviews.map((r, i) => (
                <div key={r.id || i} className="p-4 rounded-md bg-slate-50 border border-slate-100">
                  <div className="flex items-center justify-between mb-1">
                    <span className="text-xs font-bold text-slate-800">
                      {r.userName || r.userEmail || 'Customer'}
                    </span>
                    <span className="text-xs text-amber-500 font-semibold">
                      {'★'.repeat(r.rating)}{'☆'.repeat(5 - r.rating)} ({r.rating}/5)
                    </span>
                  </div>
                  <p className="text-xs text-slate-600 leading-normal">{r.comment}</p>
                </div>
              ))}
            </div>
          )}
        </div>

        {/* Submit a Review Form */}
        <div className="card p-6">
          <h3 className="text-sm font-bold text-slate-900 mb-3">Write a Product Review</h3>
          <p className="text-xs text-slate-500 mb-4">
            Note: As enforced by the backend, only customers who have placed an order containing this item can submit a review.
          </p>

          {reviewSuccess && (
            <div className="bg-green-50 border border-green-200 text-green-700 p-3 rounded-md text-xs mb-4">
              {reviewSuccess}
            </div>
          )}

          {reviewError && (
            <div className="bg-red-50 border border-red-200 text-red-700 p-3 rounded-md text-xs mb-4">
              {reviewError}
            </div>
          )}

          {user ? (
            <form onSubmit={handleReviewSubmit} className="space-y-3">
              <div>
                <label className="block text-xs font-semibold text-slate-700 mb-1">Rating</label>
                <select
                  value={rating}
                  onChange={(e) => setRating(e.target.value)}
                  className="input-field text-xs"
                >
                  <option value="5">⭐⭐⭐⭐⭐ (5 - Excellent)</option>
                  <option value="4">⭐⭐⭐⭐ (4 - Good)</option>
                  <option value="3">⭐⭐⭐ (3 - Average)</option>
                  <option value="2">⭐⭐ (2 - Below Average)</option>
                  <option value="1">⭐ (1 - Poor)</option>
                </select>
              </div>

              <div>
                <label className="block text-xs font-semibold text-slate-700 mb-1">Feedback Comment</label>
                <textarea
                  rows="3"
                  required
                  placeholder="Share your experience..."
                  value={comment}
                  onChange={(e) => setComment(e.target.value)}
                  className="input-field text-xs resize-none"
                />
              </div>

              <button
                type="submit"
                disabled={submittingReview}
                className="btn btn-primary w-full text-xs"
              >
                {submittingReview ? 'Submitting...' : 'Submit Review'}
              </button>
            </form>
          ) : (
            <div className="text-center py-4">
              <p className="text-xs text-slate-500 mb-3">You must be logged in to review.</p>
              <Link to="/login" className="btn btn-secondary text-xs w-full">
                Sign In to Review
              </Link>
            </div>
          )}
        </div>

      </div>

    </div>
  );
}
