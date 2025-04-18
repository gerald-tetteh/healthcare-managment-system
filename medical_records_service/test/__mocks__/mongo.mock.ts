import { FastifyInstance, FastifyReply } from 'fastify';
import fp from 'fastify-plugin';
import {
  OptionalId,
  Document,
  InsertOneResult,
  WithId,
  ObjectId,
  UpdateResult,
  GridFSBucket
} from 'mongodb';
import Attachment from '../../src/models/Attachment';
import { MultipartFile } from '@fastify/multipart';
import { Readable } from 'node:stream';

declare module 'fastify' {
  interface FastifyInstance {
    insertOne: (
      data: OptionalId<Document>,
      fastify: FastifyInstance
    ) => Promise<InsertOneResult<Document> | undefined>;
    findOne: (id: string, fastify: FastifyInstance) => Promise<WithId<Document> | null | undefined>;
    addAttachments: (
      record: WithId<Document>,
      attachments: Attachment[]
    ) => Promise<UpdateResult | undefined>;
    uploadAttachment: (part: MultipartFile, id: string, userId: Number) => Promise<ObjectId>;
    getAttachment: (id: string, reply: FastifyReply) => Promise<void>;
    bucket: GridFSBucket;
  }
}

export default fp(async (fastify, options) => {
  fastify.decorate('insertOne', async (data: OptionalId<Document>, fastify: FastifyInstance) => {
    return {
      acknowledged: true,
      insertedId: new ObjectId()
    };
  });
  fastify.decorate('findOne', async (id: string, fastify: FastifyInstance) => {
    if (id === 'invalid') {
      return null;
    }
    return {
      _id: new ObjectId(id),
      patientId: 0,
      doctorId: 1,
      visitType: 'InPerson',
      symptoms: ['fever', 'cough'],
      diagnosis: {
        code: 'A01',
        description: 'Test Diagnosis'
      },
      notes: 'This is a test medical record',
      attachments: [
        new Attachment('test.pdf', new ObjectId(id), 'application/pdf'),
        new Attachment('test2.pdf', new ObjectId(), 'application/pdf')
      ]
    };
  });
  fastify.decorate(
    'addAttachments',
    async (record: WithId<Document>, attachments: Attachment[]) => {
      return {
        acknowledged: true,
        matchedCount: 1,
        modifiedCount: 1,
        upsertedCount: 0,
        upsertedId: null
      };
    }
  );
  fastify.decorate('uploadAttachment', async (part: MultipartFile, id: string, userId: Number) => {
    await new Promise<void>((resolve, reject) => {
      part.file.on('data', () => {});
      part.file.on('end', resolve);
      part.file.on('error', reject);
    });
    return new ObjectId();
  });
  fastify.decorate('getAttachment', async (id: string, reply: FastifyReply) => {
    const readable = new Readable();
    readable.push('test');
    readable.push(null);
    reply.type('application/pdf').send(readable);
  });
});
