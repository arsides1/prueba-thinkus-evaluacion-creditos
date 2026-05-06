# =============================================================================
# Dockerfile multi-stage para el frontend Angular
# =============================================================================
# Stage 1 (builder): Node 22 LTS para compilar la app a archivos estaticos
# Stage 2 (runtime): Nginx alpine para servirlos
#
# Imagen final: ~50 MB. Sin Node, sin npm, sin source maps.
# =============================================================================

# ---------- Stage 1: build ----------
FROM node:22-alpine AS builder

WORKDIR /app

# Copiamos solo los manifests primero para aprovechar el cache de Docker:
# si package.json no cambia, npm ci se reutiliza y el rebuild es rapido.
COPY package.json package-lock.json ./
RUN npm ci --no-audit --no-fund

# Despues el codigo fuente
COPY . .

# Build con configuracion 'docker' (aplica fileReplacements de environment.docker.ts)
RUN npm run build -- --configuration=docker


# ---------- Stage 2: runtime ----------
FROM nginx:1.27-alpine AS runtime

# Eliminamos la pagina default de Nginx
RUN rm -rf /usr/share/nginx/html/*

# Copiamos los archivos compilados de Angular (Angular 20 los pone en dist/<proj>/browser)
COPY --from=builder /app/dist/frontend/browser /usr/share/nginx/html

# Config custom de Nginx (HTML5 history mode + gzip + cache de assets)
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80

# Healthcheck: Nginx debe servir el index en menos de 3 segundos
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
    CMD wget -q --spider http://localhost/ || exit 1

CMD ["nginx", "-g", "daemon off;"]
