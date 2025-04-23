import { FastifySchema } from 'fastify';
import VisitType from '../models/VisitType';

export const createMedicalRecordSchema: FastifySchema = {
  body: {
    type: 'object',
    additionalProperties: false,
    required: ['patientId', 'doctorId', 'visitType', 'symptoms', 'diagnosis', 'notes'],
    properties: {
      patientId: { type: 'number' },
      doctorId: { type: 'number' },
      visitType: {
        type: 'string',
        enum: Object.values(VisitType)
      },
      symptoms: {
        type: 'array',
        items: { type: 'string' }
      },
      diagnosis: {
        type: 'object',
        required: ['icd10Code', 'description'],
        properties: {
          icd10Code: { type: 'string' },
          description: { type: 'string' }
        }
      },
      prescriptions: {
        type: 'array',
        items: {
          type: 'object',
          required: ['medicationName', 'dosage', 'frequency', 'duration'],
          properties: {
            medicationName: { type: 'string' },
            dosage: { type: 'string' },
            frequency: { type: 'string' },
            duration: { type: 'number' }
          }
        }
      },
      labTests: {
        type: 'array',
        items: {
          type: 'object',
          required: ['testName', 'result', 'date'],
          properties: {
            testName: { type: 'string' },
            result: { type: 'string' },
            date: { type: 'string', format: 'date-time' }
          }
        }
      },
      notes: { type: 'string' }
    }
  }
};

export const createLabTestSchema: FastifySchema = {
  body: {
    type: 'object',
    additionalProperties: false,
    required: ['testName', 'result', 'date'],
    properties: {
      testName: { type: 'string' },
      result: { type: 'string' },
      date: { type: 'string', format: 'date-time' }
    }
  }
};
