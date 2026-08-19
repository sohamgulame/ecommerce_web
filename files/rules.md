# rules.md — Rules for the Coding Agent

These rules are non-negotiable. Follow them for every feature, every phase, no
exceptions unless the user explicitly says otherwise in chat.

## 1. Learning-first, not agent-first
- I am learning Spring Boot. Do not silently generate an entire feature end-to-end
  without explanation.
- Explain what each piece of generated code does and why, briefly, before or after
  writing it.
- If a concept required for a feature hasn't been covered in `phases.md` yet, stop and
  say so instead of quietly using it.
- Wait for me to say "next phase" / "continue" before moving to the next phase in
  `phases.md`. Do not jump ahead.

## 2. Architecture — always follow this layering
```
Controller → Service (interface + impl) → Repository → Entity
                    ↕
                  DTO ↔ Mapper
```
- Controllers never contain business logic — only request handling and calling the
  service layer.
- Services contain all business logic and validation that isn't simple field-level
  validation.
- Repositories are Spring Data JPA interfaces only — no custom logic beyond query
  methods / JPQL / Specifications.
- Entities are never returned directly from controllers — always map to a DTO.
- Every entity that's exposed via API needs a Request DTO (for input) and a Response
  DTO (for output). Do not reuse one DTO for both unless they're identical and I
  approve it.

## 3. Package structure (fixed)
```
com.ecommerce
 ├── controller/
 ├── service/
 │    └── impl/
 ├── repository/
 ├── entity/
 ├── dto/
 │    ├── request/
 │    └── response/
 ├── mapper/
 ├── exception/
 ├── security/
 └── config/
```
Do not deviate from this structure without asking first.

## 4. Error handling
- All custom exceptions extend a common base or are handled centrally.
- Use a single `GlobalExceptionHandler` with `@RestControllerAdvice` — no per-controller
  try/catch for expected business errors.
- Every error response follows a consistent shape (status, message, timestamp, path at
  minimum).

## 5. API design
- RESTful naming: plural nouns, proper HTTP verbs (`GET /products`, `POST /products`,
  not `/getProducts`).
- Every list endpoint that can grow (products, orders, reviews) must be paginated by
  default.
- Use `ResponseEntity` with correct status codes (201 for creation, 204 for delete,
  404 for not found, 403 for forbidden, etc.) — never return 200 for everything.
- Search/filter parameters are optional query params with sensible defaults, never
  required unless the endpoint is explicitly search-only.

## 6. Security
- Every endpoint must have an explicit role/access decision — never leave an endpoint
  unsecured "by accident." If public, mark it public deliberately.
- Any endpoint that returns or modifies "my own data" (cart, orders, profile) must
  resolve the user from the JWT principal, never trust a path variable or request body
  for identity.
- Passwords are always encoded (BCrypt), never stored or logged in plain text.

## 7. Data integrity
- Order line items store a snapshot of product name/price at purchase time — never
  join live to current product price for historical orders.
- Use `@Valid` + Bean Validation annotations on all request DTOs — don't rely on
  service-layer manual null checks for basic field validation.
- Use database constraints (unique, not null) in addition to app-level validation
  where it matters (e.g., one review per user per product).

## 8. Code style
- Constructor injection only — no `@Autowired` field injection.
- Meaningful names, no abbreviations that aren't obvious (`qty` is fine, `prdSvcImpl2`
  is not).
- Keep methods short and single-purpose; if a service method is doing five things,
  flag it and suggest splitting.

## 9. Communication style for the agent
- Be direct about trade-offs and don't just agree with every request — if something I
  ask for conflicts with clean architecture or a rule above, say so before proceeding.
- When multiple valid approaches exist, briefly present the options and your
  recommendation rather than silently picking one.
- Update `memory.md` after completing each phase or major decision (see that file for
  what to log).
