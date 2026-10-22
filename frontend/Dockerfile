# Etapa 1: compilar la aplicacion
FROM node:22-slim AS build
WORKDIR /app
COPY package*.json ./
RUN npm ci
COPY . .
RUN npm run build

# Etapa 2: servir los estaticos con nginx
FROM nginx:1.27-alpine
# La plantilla se procesa con envsubst al arrancar: BACKEND_URL decide a donde va /api
COPY nginx/default.conf.template /etc/nginx/templates/default.conf.template
COPY nginx/security-headers.conf /etc/nginx/security-headers.conf
COPY --from=build /app/dist/taskin/browser /usr/share/nginx/html
ENV BACKEND_URL=http://host.docker.internal:8080
EXPOSE 80
