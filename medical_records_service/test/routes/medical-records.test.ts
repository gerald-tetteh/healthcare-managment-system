import { beforeEach, describe, it } from 'node:test';
import * as assert from 'node:assert/strict';
import VisitType from '../../src/models/VisitType';
import Diagnosis from '../../src/models/Diagnosis';
import Fastify, { FastifyInstance } from 'fastify';
import jwt from '../../src/plugins/jwt';
import mongoMock from '../__mocks__/mongo.mock';
import medicalRecords from '../../src/routes/medical-records';
import encryption from '../../src/plugins/encryption';

describe('Medical Records', () => {
  let fastify: FastifyInstance;

  beforeEach(async () => {
    fastify = Fastify();
    await fastify.register(jwt);
    await fastify.register(mongoMock);
    await fastify.register(medicalRecords);
    await fastify.register(encryption);
    await fastify.ready();
  });

  it('should create medical record', async () => {
    const token = fastify.jwt.sign({ userId: 1, roles: ['ROLE_DOCTOR'] });

    const res = await fastify.inject({
      method: 'POST',
      url: '/',
      payload: {
        patientId: 0,
        doctorId: 1,
        visitType: VisitType.InPerson,
        symptoms: ['fever', 'cough'],
        diagnosis: new Diagnosis('A01', 'Test Diagnosis'),
        notes: 'This is a test medical record'
      },
      headers: {
        Authorization: `Bearer ${token}`
      }
    });

    const responseBody = JSON.parse(res.payload);
    assert.equal(res.statusCode, 200);
    assert.equal(responseBody.status, 'success');
    assert.equal(responseBody.message, 'Medical record created successfully');
  });

  it('should return 403 if user is not a doctor', async () => {
    const token = fastify.jwt.sign({ userId: 1, roles: ['ROLE_PATIENT'] });

    const res = await fastify.inject({
      method: 'POST',
      url: '/',
      payload: {
        patientId: 1,
        doctorId: 2,
        visitType: VisitType.InPerson,
        symptoms: ['fever', 'cough'],
        diagnosis: new Diagnosis('A01', 'Test Diagnosis'),
        notes: 'This is a test medical record'
      },
      headers: {
        Authorization: `Bearer ${token}`
      }
    });

    assert.equal(res.statusCode, 403);
    assert.equal(
      res.payload,
      '{"title":"Authorization Failed","message":"Cannot access this resource","statusCode":"FORBIDDEN"}'
    );
  });

  it('should return 401 if token is invalid or missing', async () => {
    const res = await fastify.inject({
      method: 'POST',
      url: '/',
      payload: {
        patientId: 1,
        doctorId: 2,
        visitType: VisitType.InPerson,
        symptoms: ['fever', 'cough'],
        diagnosis: new Diagnosis('A01', 'Test Diagnosis'),
        notes: 'This is a test medical record'
      },
      headers: {
        Authorization: 'Bearer fake-token'
      }
    });

    assert.equal(res.statusCode, 401);
    assert.equal(
      res.payload,
      '{"title":"Authentication Failed","message":"Invalid or missing token","statusCode":"UNAUTHORIZED"}'
    );
  });

  it('should fail to create record if schema is invalid', async () => {
    const token = fastify.jwt.sign({ userId: 1, roles: ['ROLE_DOCTOR'] });

    const res = await fastify.inject({
      method: 'POST',
      url: '/',
      payload: {
        patientId: 0,
        doctorId: 1,
        visitType: VisitType.InPerson,
        symptoms: ['fever', 'cough'],
        diagnosis: new Diagnosis('A01', 'Test Diagnosis')
      },
      headers: {
        Authorization: `Bearer ${token}`
      }
    });

    const responseBody = JSON.parse(res.payload);

    assert.equal(res.statusCode, 400);
    assert.equal(responseBody.title, 'Bad Request');
    assert.equal(responseBody.statusCode, 'BAD_REQUEST');
  });
});
