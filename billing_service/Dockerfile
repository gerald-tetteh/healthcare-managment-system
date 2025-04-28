FROM node:20.19.1-slim AS build

RUN mkdir src
WORKDIR /src
COPY . .
RUN npm ci
RUN npm run build:ts
RUN npm prune --production

FROM node:20.19.1-slim

ENV NODE_ENV="production"
COPY --from=build /src/dist /src/dist
COPY --from=build /src/node_modules /src/node_modules
COPY --from=build /src/package*.json /src/
WORKDIR /src

EXPOSE 8085
ENTRYPOINT [ "npm", "run", "start:docker" ]