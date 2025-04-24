FROM node:20.19.1-slim AS build

# Install system dependencies required for node-rdkafka and node-gyp
RUN apt-get update && apt-get install -y \
  python3 \
  python3-pip \
  python3-distutils \
  build-essential \
  curl \
  git \
  pkg-config \
  libssl-dev \
  libsasl2-dev \
  librdkafka-dev \
  && apt-get clean \
  && rm -rf /var/lib/apt/lists/*
# Tell node-gyp to use Python 3
ENV PYTHON=/usr/bin/python3

RUN mkdir src
WORKDIR /src
COPY . .
RUN npm ci
RUN npm run build:ts
RUN npm prune --production

FROM node:20.19.1-slim

RUN apt-get update && apt-get install -y \
  libsasl2-2 \
  libssl3 \
  librdkafka1 \
  && apt-get clean \
  && rm -rf /var/lib/apt/lists/*

ENV NODE_ENV="production"
COPY --from=build /src/dist /src/dist
COPY --from=build /src/node_modules /src/node_modules
COPY --from=build /src/package*.json /src/
WORKDIR /src

EXPOSE 8084
ENTRYPOINT [ "npm", "run", "start:docker" ]