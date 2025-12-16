FROM ghcr.io/cirruslabs/flutter:stable AS build

ARG BACKEND_BASE_URL
ARG SUPABASE_URL
ARG SUPABASE_ANON_KEY

WORKDIR /app

COPY pubspec.yaml pubspec.lock ./
RUN flutter pub get

COPY . .
RUN flutter build web \
    --dart-define=BACKEND_BASE_URL=${BACKEND_BASE_URL} \
    --dart-define=SUPABASE_URL=${SUPABASE_URL} \
    --dart-define=SUPABASE_ANON_KEY=${SUPABASE_ANON_KEY} \
    --release

FROM nginx:alpine
COPY --from=build /app/build/web /usr/share/nginx/html

RUN echo 'server { \
    listen 8000; \
    location / { \
        root /usr/share/nginx/html; \
        index index.html index.htm; \
        try_files $uri $uri/ /index.html; \
    } \
}' > /etc/nginx/conf.d/default.conf
EXPOSE 8000

CMD ["nginx", "-g", "daemon off;"]