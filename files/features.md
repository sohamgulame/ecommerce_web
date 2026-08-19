# features.md — E-Commerce Backend

Features grouped by module. Each is tagged with the roadmap phase it depends on, so the
agent never builds something the learner hasn't reached yet.

## Module: Authentication & Users
*(depends on: Spring Security / JWT — already completed)*
- Register (customer signup)
- Login (JWT access + refresh token)
- Role-based access: CUSTOMER, ADMIN
- Get current user profile
- Update profile (name, address, phone)

## Module: Category Management
*(depends on: REST + JPA — already completed)*
- Admin: create, update, delete category
- Public: list all categories
- Category can have subcategories (self-referencing, optional stretch)

## Module: Product Catalog
*(depends on: JPA relationships, pagination/search/filter — already completed)*
- Admin: create, update, delete product
- Product belongs to a Category (ManyToOne)
- Public: list products — paginated
- Public: search products by name/description
- Public: filter products by category, price range, availability
- Public: get single product detail (with average rating once reviews exist)

## Module: Cart
*(depends on: JPA relationships, JWT-scoped "own data" access — already completed)*
- Customer: add product to cart
- Customer: update quantity / remove item
- Customer: view current cart with computed total
- Cart is per-user, resolved from JWT — never from a path variable

## Module: Orders
*(depends on: JPA relationships, DTO/mapper, business logic — already completed)*
- Customer: place order from cart (cart → order conversion)
- Customer: view own order history — paginated
- Customer: view single order detail
- Admin: view all orders — filterable by status
- Admin: update order status (PLACED → CONFIRMED → SHIPPED → DELIVERED / CANCELLED)
- Order stores a snapshot of product price/name at time of purchase (don't rely on
  live product data changing later)

## Module: Reviews & Ratings
*(depends on: JPA relationships, validation — already completed)*
- Customer: add a review + rating (1–5) for a product they've ordered
- Public: view reviews for a product, paginated
- Validation: one review per customer per product; can only review products they've
  actually ordered

## Module: Product Images
*(depends on: File Handling — not yet learned; build when reached)*
- Admin: upload product image(s)
- Public: view product with image URLs
- Cloud storage integration with Cloudinary instead of local disk in production

## Module: Notifications
*(depends on: Production Features / Email — not yet learned; build when reached)*
- Order confirmation email
- Order status update email
- Email verification on signup
- Forgot/reset password flow

## Module: Scheduled Jobs
*(depends on: Background Processing — not yet learned; build when reached)*
- Auto-cancel orders left unpaid/unconfirmed after X hours
- Daily low-stock report (admin-facing, logged or emailed)

## Module: Performance
*(depends on: Redis/Caching — not yet learned; build when reached)*
- Cache product catalog listing and category list
- Evict cache on product/category update

## Module: Testing
*(depends on: Testing phase — not yet learned; build incrementally alongside features
once reached)*
- Unit tests for services (cart total calculation, order status transitions)
- Controller tests with MockMvc
- Integration tests for critical flows (checkout)

## Module: Deployment
*(depends on: Docker + Deployment — not yet learned; build when reached)*
- Dockerize the application
- Docker Compose with DB
- Deploy to Render/Railway

## Explicitly Deferred (not planned for this project)
- Payment gateway integration (Stripe/Razorpay) — simulate payment status only
- Multi-vendor support
- Real-time inventory sync
- Coupons/discount engine (possible future stretch, not in current scope)
