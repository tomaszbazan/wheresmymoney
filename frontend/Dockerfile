FROM debian:stable-slim AS build

# Build arguments for configuration
ARG BACKEND_BASE_URL
ARG SUPABASE_URL
ARG SUPABASE_ANON_KEY

RUN apt-get update && apt-get install -y curl git unzip xz-utils zip libglu1-mesa
RUN git clone https://github.com/flutter/flutter.git /usr/local/flutter
ENV PATH="/usr/local/flutter/bin:/usr/local/flutter/bin/cache/dart-sdk/bin:${PATH}"
RUN flutter doctor

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

# Create nginx config for port 9081
RUN echo 'server { \
    listen 9081; \
    location / { \
        root /usr/share/nginx/html; \
        index index.html index.htm; \
        try_files $uri $uri/ /index.html; \
    } \
}' > /etc/nginx/conf.d/default.conf

EXPOSE 9081
CMD ["nginx", "-g", "daemon off;"]