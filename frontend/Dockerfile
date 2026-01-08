# Stage 1: Build
FROM node:18 AS build
WORKDIR /app
COPY package.json package-lock.json ./
RUN npm install
COPY . .
# Build the application for production
RUN npm run build -- --configuration production

# Stage 2: Run
FROM nginx:alpine
# Copy custom nginx configuration
COPY nginx.conf /etc/nginx/conf.d/default.conf
# Copy the build output to replace the default nginx contents.
# Confirmed outputPath is dist/shopping-frontend in angular.json
COPY --from=build /app/dist/shopping-frontend /usr/share/nginx/html
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
