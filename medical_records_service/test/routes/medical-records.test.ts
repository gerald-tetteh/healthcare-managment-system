import { beforeEach, describe, it } from 'node:test';
import * as assert from 'node:assert/strict';
import VisitType from '../../src/models/VisitType';
import Diagnosis from '../../src/models/Diagnosis';
import Fastify, { FastifyInstance } from 'fastify';
import jwt from '../../src/plugins/jwt';
import mongoMock from '../__mocks__/mongo.mock';
import medicalRecords from '../../src/routes/medical-records';
import encryption from '../../src/plugins/encryption';
import { ObjectId } from '@fastify/mongodb';
import FormData from 'form-data';
import { Readable } from 'node:stream';
import multipart from '../../src/plugins/multipart';
import { options } from '../../src/app';

describe('Medical Records', () => {
  let fastify: FastifyInstance;

  beforeEach(async () => {
    fastify = Fastify({
      ...options,
      logger: false
    });
    await fastify.register(jwt);
    await fastify.register(encryption);
    await fastify.register(mongoMock);
    await fastify.register(multipart);
    await fastify.register(medicalRecords);
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

  it('should fail to create record if payload contains unexpected fields', async () => {
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
        notes: 'This is a test medical record',
        attachments: []
      },
      headers: {
        Authorization: `Bearer ${token}`
      }
    });
    const responseBody = JSON.parse(res.payload);
    assert.equal(res.statusCode, 400);
    assert.equal(responseBody.title, 'Bad Request');
    assert.equal(responseBody.statusCode, 'BAD_REQUEST');
    assert.equal(responseBody.message, 'body must NOT have additional properties');
  });

  it('should get medical record by id', async () => {
    const token = fastify.jwt.sign({ userId: 1, roles: ['ROLE_DOCTOR'] });

    const res = await fastify.inject({
      method: 'GET',
      url: `/${new ObjectId()}`,
      headers: {
        Authorization: `Bearer ${token}`
      }
    });

    assert.equal(res.statusCode, 200);
  });

  it('should return 404 if record not found', async () => {
    const token = fastify.jwt.sign({ userId: 1, roles: ['ROLE_DOCTOR'] });

    const res = await fastify.inject({
      method: 'GET',
      url: `/invalid`,
      headers: {
        Authorization: `Bearer ${token}`
      }
    });

    assert.equal(res.statusCode, 404);
    assert.equal(
      res.payload,
      '{"title":"Record Not Found","message":"The requested record does not exist","statusCode":"NOT_FOUND"}'
    );
  });

  it("should return 403 if user is a patient and tries to access another patient's record", async () => {
    const token = fastify.jwt.sign({ userId: 1, roles: ['ROLE_PATIENT'] });

    const res = await fastify.inject({
      method: 'GET',
      url: `/${new ObjectId()}`,
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

  it('should upload attachment', async () => {
    const token = fastify.jwt.sign({ userId: 1, roles: ['ROLE_DOCTOR'] });

    const formData = new FormData();
    formData.append('file', Readable.from(['Sample content']), {
      filename: 'test.txt',
      contentType: 'text/plain'
    });
    formData.append('file2', Readable.from(['Sample content 2.0']), {
      filename: 'test2.txt',
      contentType: 'text/plain'
    });

    const res = await fastify.inject({
      method: 'POST',
      url: `/${new ObjectId()}/attachments`,
      payload: formData,
      headers: {
        Authorization: `Bearer ${token}`,
        ...formData.getHeaders()
      }
    });

    assert.equal(res.statusCode, 200);
  });

  it('should return 404 if record not found when uploading attachment', async () => {
    const token = fastify.jwt.sign({ userId: 1, roles: ['ROLE_DOCTOR'] });

    const formData = new FormData();
    formData.append('file', Readable.from(['Sample content']), {
      filename: 'test.txt',
      contentType: 'text/plain'
    });

    const res = await fastify.inject({
      method: 'POST',
      url: `/invalid/attachments`,
      payload: formData,
      headers: {
        Authorization: `Bearer ${token}`,
        ...formData.getHeaders()
      }
    });

    assert.equal(res.statusCode, 404);
    assert.equal(
      res.payload,
      '{"title":"Record Not Found","message":"The requested record does not exist","statusCode":"NOT_FOUND"}'
    );
  });

  it('should return 403 if user is not authorized to upload attachment', async () => {
    const token = fastify.jwt.sign({ userId: 1, roles: ['ROLE_PATIENT'] });

    const formData = new FormData();
    formData.append('file', Readable.from(['Sample content']), {
      filename: 'test.txt',
      contentType: 'text/plain'
    });

    const res = await fastify.inject({
      method: 'POST',
      url: `/${new ObjectId()}/attachments`,
      payload: formData,
      headers: {
        Authorization: `Bearer ${token}`,
        ...formData.getHeaders()
      }
    });

    assert.equal(res.statusCode, 403);
    assert.equal(
      res.payload,
      '{"title":"Authorization Failed","message":"Cannot access this resource","statusCode":"FORBIDDEN"}'
    );
  });

  it('should return 400 if no files are uploaded', async () => {
    const token = fastify.jwt.sign({ userId: 1, roles: ['ROLE_DOCTOR'] });

    const res = await fastify.inject({
      method: 'POST',
      url: `/${new ObjectId()}/attachments`,
      headers: {
        Authorization: `Bearer ${token}`,
        'Content-Type': 'multipart/form-data; boundary=---011000010111000001110100'
      }
    });

    assert.equal(res.statusCode, 400);
    assert.equal(
      res.payload,
      '{"title":"Bad Request","message":"No files were uploaded","statusCode":"BAD_REQUEST"}'
    );
  });

  it('should get attachment', async () => {
    const token = fastify.jwt.sign({ userId: 1, roles: ['ROLE_DOCTOR'] });
    const testId = new ObjectId();

    const res = await fastify.inject({
      method: 'GET',
      url: `/${testId}/attachments/${testId}`,
      headers: {
        Authorization: `Bearer ${token}`
      }
    });

    assert.equal(res.statusCode, 200);
  });

  it('should return 404 if attachment not found', async () => {
    const token = fastify.jwt.sign({ userId: 1, roles: ['ROLE_DOCTOR'] });
    const testId = new ObjectId();

    const res = await fastify.inject({
      method: 'GET',
      url: `/${testId}/attachments/${new ObjectId()}`,
      headers: {
        Authorization: `Bearer ${token}`
      }
    });

    assert.equal(res.statusCode, 404);
    assert.equal(
      res.payload,
      '{"title":"Attachment Not Found","message":"The requested attachment does not exist","statusCode":"NOT_FOUND"}'
    );
  });

  it('should return 403 if user is not authorized to get attachment', async () => {
    const token = fastify.jwt.sign({ userId: 1, roles: ['ROLE_PATIENT'] });
    const testId = new ObjectId();

    const res = await fastify.inject({
      method: 'GET',
      url: `/${testId}/attachments/${testId}`,
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

  it('should return 404 if record not found when getting attachment', async () => {
    const token = fastify.jwt.sign({ userId: 1, roles: ['ROLE_DOCTOR'] });
    const testId = new ObjectId();

    const res = await fastify.inject({
      method: 'GET',
      url: `/invalid/attachments/${testId}`,
      headers: {
        Authorization: `Bearer ${token}`
      }
    });

    assert.equal(res.statusCode, 404);
    assert.equal(
      res.payload,
      '{"title":"Record Not Found","message":"The requested record does not exist","statusCode":"NOT_FOUND"}'
    );
  });

  it('should add lab test to medical record', async () => {
    const token = fastify.jwt.sign({ userId: 1, roles: ['ROLE_DOCTOR'] });
    const testId = new ObjectId();

    const res = await fastify.inject({
      method: 'POST',
      url: `/${testId}/lab-tests`,
      payload: {
        testName: 'Blood Test',
        result: 'Normal',
        date: new Date().toISOString()
      },
      headers: {
        Authorization: `Bearer ${token}`
      }
    });

    assert.equal(res.statusCode, 200);
  });

  it('should return 404 if record not found when adding lab test', async () => {
    const token = fastify.jwt.sign({ userId: 1, roles: ['ROLE_DOCTOR'] });

    const res = await fastify.inject({
      method: 'POST',
      url: `/invalid/lab-tests`,
      payload: {
        testName: 'Blood Test',
        result: 'Normal',
        date: new Date().toISOString()
      },
      headers: {
        Authorization: `Bearer ${token}`
      }
    });

    assert.equal(res.statusCode, 404);
    assert.equal(
      res.payload,
      '{"title":"Record Not Found","message":"The requested record does not exist","statusCode":"NOT_FOUND"}'
    );
  });

  it('should return 403 if user is not authorized to add lab test', async () => {
    const token = fastify.jwt.sign({ userId: 1, roles: ['ROLE_PATIENT'] });
    const testId = new ObjectId();

    const res = await fastify.inject({
      method: 'POST',
      url: `/${testId}/lab-tests`,
      payload: {
        testName: 'Blood Test',
        result: 'Normal',
        date: new Date().toISOString()
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

  it('should return 400 if payload is invalid when adding lab test', async () => {
    const token = fastify.jwt.sign({ userId: 1, roles: ['ROLE_DOCTOR'] });
    const testId = new ObjectId();

    const res = await fastify.inject({
      method: 'POST',
      url: `/${testId}/lab-tests`,
      payload: {
        testName: 'Blood Test',
        result: 'Normal'
      },
      headers: {
        Authorization: `Bearer ${token}`
      }
    });

    assert.equal(res.statusCode, 400);
    assert.equal(
      res.payload,
      '{"title":"Bad Request","message":"body must have required property \'date\'","statusCode":"BAD_REQUEST"}'
    );
  });

  it('should return 400 if payload contains unexpected fields when adding lab test', async () => {
    const token = fastify.jwt.sign({ userId: 1, roles: ['ROLE_DOCTOR'] });
    const testId = new ObjectId();

    const res = await fastify.inject({
      method: 'POST',
      url: `/${testId}/lab-tests`,
      payload: {
        testName: 'Blood Test',
        result: 'Normal',
        date: new Date().toISOString(),
        unexpectedField: 'unexpectedValue'
      },
      headers: {
        Authorization: `Bearer ${token}`
      }
    });

    assert.equal(res.statusCode, 400);
    assert.equal(
      res.payload,
      '{"title":"Bad Request","message":"body must NOT have additional properties","statusCode":"BAD_REQUEST"}'
    );
  });
});
