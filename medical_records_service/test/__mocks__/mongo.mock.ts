import { FastifyInstance } from 'fastify';
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
      doctorId: 1
    };
  });
});
