FROM node:20-alpine AS build

WORKDIR /app

# package.json и package-lock.json
COPY package*.json ./

# зависимости
RUN npm ci

# исходный код
COPY . .

# сборка
RUN npm run build

# этап с nginx
FROM nginx:alpine

# собранное приложение в nginx
COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /app/dist /usr/share/nginx/html

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]