FROM node:23.11.0-slim AS build
RUN mkdir src
WORKDIR /src
COPY . .
RUN npm install
RUN npm run build:ts

FROM node:23.11.0-slim
ENV NODE_ENV="production"
COPY --from=build /src/dist /src/dist
COPY --from=build /src/node_modules /src/node_modules
COPY --from=build /src/package*.json /src/
WORKDIR /src

EXPOSE 8084
ENTRYPOINT [ "npm", "run", "start:docker" ]