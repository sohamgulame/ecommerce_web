# PRD.md — E-Commerce Backend

## 1. Overview
A full-stack e-commerce platform: a Spring Boot REST API backend (built and tested)
paired with a React single-page-application frontend, designed as a learning-driven
project that mirrors real production e-commerce systems: product catalog, cart, orders,
and role-based access for customers vs admins.

This project is built to consolidate and apply Spring Boot skills already learned
(REST APIs, JPA relationships, clean layered architecture, JWT security, pagination/
filtering, testing) and to extend into a React frontend that consumes the existing
JWT-secured API, plus skills still being learned on the backend (file handling,
Docker, caching, background jobs).

**Scope update (2026-08-16):** the project was originally API-only. A React frontend
has since been added to scope — see the updated Non-Goals below and `phases.md` for
the new frontend phase block.

## 2. Goals
- Build a production-shaped e-commerce backend, not a toy CRUD demo.
- Practice real relationship modeling (products, categories, cart, orders, reviews).
- Practice role-based authorization with two distinct actor types (Customer, Admin).
- Produce a portfolio-quality project that demonstrates layered architecture, clean
  DTOs, proper exception handling, and thoughtful API design.
- Grow the same codebase feature-by-feature as new topics are learned, rather than
  starting new projects per topic.
- Build a React frontend that consumes the existing REST API — practice separated
  frontend/backend architecture, JWT-based auth from the client side, and role-based
  UI (customer views vs admin panel).

## 3. Non-Goals (out of scope for now)
- Real payment gateway integration (a mock/simulated payment status is enough).
- Multi-vendor marketplace complexity (single-seller/admin model only).
- Real-time features (live chat, live stock updates via WebSocket) — not in current
  learning scope.

## 4. Target Users (of the system being built)
- **Customer** — browses products, manages cart, places orders, views order history,
  writes reviews.
- **Admin** — manages products, categories, inventory, views all orders, updates order
  status.

## 5. Success Criteria
- All core e-commerce flows work end-to-end: browse → cart → checkout → order → status
  update.
- Role-based access is correctly enforced (customers can't access admin endpoints and
  vice versa).
- API is paginated, searchable, and filterable where relevant (product listing).
- Codebase follows consistent clean architecture across every feature added.
- Project can be demoed and explained confidently in a technical interview.

## 6. Constraints
- Built solo, learning-paced — features are added in phases, not all at once.
- Must only use concepts already covered in the learning roadmap, phase by phase (see
  `phases.md`). No skipping ahead to unlearned concepts without flagging it.
- Backend tech stack fixed: Java, Spring Boot, Spring Data JPA, MySQL (local), Spring
  Security + JWT, Maven.
- Frontend tech stack fixed: React (via Vite), Axios for API calls, React Router for
  navigation, Tailwind CSS for styling. No Next.js/Redux — kept intentionally minimal
  for this project's scope.

## 6a. Frontend Scope
- Consumes the existing JWT-secured REST API — no server-side rendering, no direct DB
  access from the frontend.
- Customer-facing views: product catalog (browse/search/filter), product detail,
  cart, checkout, order history, order detail, product reviews.
- Admin-facing views: category management, product management, order management
  (view all + update status).
- Auth: login/register forms, JWT stored client-side, attached to requests via an
  Axios interceptor, route protection based on role (customer vs admin vs
  unauthenticated).
- Backend requirement introduced by this scope change: CORS must be configured in
  `SecurityConfig` for the frontend's dev origin (e.g. `http://localhost:5173`) — this
  did not exist before since there was no browser-based client.

## 7. Reference Docs
- `features.md` — full feature breakdown by module
- `rules.md` — architecture and coding rules the agent must follow
- `phases.md` — build order, phase by phase
- `design.md` — entities, API design, tech decisions
- `memory.md` — running project context/decisions log for the agent to maintain
