# =========================================================================
# Stage 1: Build React Application with Vite
# =========================================================================
FROM node:22-alpine AS builder
WORKDIR /app

# Copy package manifests first for optimal layer caching
COPY package.json package-lock.json ./
RUN npm ci

# Copy frontend source code
COPY . .

# Build argument for backend API URL (defaults to /api/v1 or dynamic hostname in axios.js)
ARG VITE_API_URL
ENV VITE_API_URL=$VITE_API_URL

# Build optimized production bundle
RUN npm run build

# =========================================================================
# Stage 2: Serve with Nginx Alpine
# =========================================================================
FROM nginx:alpine

# Copy custom Nginx configuration
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Copy production static assets from builder stage
COPY --from=builder /app/dist /usr/share/nginx/html

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
