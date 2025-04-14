import { beforeEach, describe, it } from 'node:test'
import * as assert from 'node:assert/strict'

import Fastify, { FastifyInstance } from 'fastify'
import jwt from '../../src/plugins/jwt';

describe("JWT plugin", () => {
  let fastify: FastifyInstance;
  beforeEach(async () => {
    fastify = Fastify();
    await fastify.register(jwt, {});
  });

  it("should register the JWT plugin", async () => {
    await fastify.ready();
    assert.equal(typeof fastify.jwt, 'object');
  });

  it("should authenticate user", async () => {
    const token = fastify.jwt.sign({userId: 1})

    fastify.get("/test", {
      preHandler: fastify.authenticate,
    }, async (request, reply) => {
      assert.equal(request.user.userId, 1);
      reply.send({});
    });

    await fastify.inject({
      method: 'GET',
      url: '/test',
      headers: {
        "authorization": `Bearer ${token}`,
      },
    });
  });

  it("should return 401 if token is missing", async () => {
    fastify.get("/test", {
      preHandler: fastify.authenticate,
    }, async (request, reply) => {
      reply.send({});
    });

    const response = await fastify.inject({
      method: 'GET',
      url: '/test',
    });

    assert.equal(response.statusCode, 401);
    assert.equal(response.json().message, 'No Authorization was found in request.headers');
  });

  it("should authorize user by role", async () => {
    const token = fastify.jwt.sign({userId: 1, roles: ['admin']})

    fastify.get("/test", {
      preHandler: fastify.authorizeByRole(['admin']),
    }, async (request, reply) => {
      assert.equal(request.user.userId, 1);
      reply.send({});
    });

    await fastify.inject({
      method: 'GET',
      url: '/test',
      headers: {
        "authorization": `Bearer ${token}`,
      },
    });
  });

  it("should return 403 if user does not have required role", async () => {
    const token = fastify.jwt.sign({userId: 1, roles: ['user']})

    fastify.get("/test", {
      preHandler: fastify.authorizeByRole(['admin']),
    }, async (request, reply) => {
      reply.send({});
    });

    const response = await fastify.inject({
      method: 'GET',
      url: '/test',
      headers: {
        "authorization": `Bearer ${token}`,
      },
    });

    assert.equal(response.statusCode, 403);
    assert.equal(response.json().message, 'Cannot access this resource');
    assert.equal(response.json().title, 'Authorization Failed');
    assert.equal(response.json().statusCode, 'FORBIDDEN');
  });
});
