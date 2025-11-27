FROM node:18-alpine AS builder

WORKDIR /app

COPY package*.json ./
RUN npm ci --only=production

COPY . .

# Параметры окружения ДОЛЖНЫ быть заданы до сборки,
# чтобы create-react-app "зашил" их в бандл.
ARG API_URL=http://localhost:8080
ARG USE_MOCKS=false
ENV REACT_APP_API_URL=$API_URL
ENV REACT_APP_USE_MOCKS=$USE_MOCKS

RUN npm run build

# Финальный образ
FROM nginx:alpine

COPY --from=builder /app/build /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]