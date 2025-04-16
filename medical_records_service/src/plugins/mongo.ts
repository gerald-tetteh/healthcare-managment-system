import fp from 'fastify-plugin';
import fastifyMongodb from '@fastify/mongodb';
import { FastifyInstance } from 'fastify';
import { Document, GridFSBucket, InsertOneResult, OptionalId, UpdateResult, WithId } from 'mongodb';
import ServerException from '../models/ServerException';
import Attachment from '../models/Attachment';
import MedicalRecord from '../models/MedicalRecord';

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
  if (process.env.NODE_ENV !== 'test') {
    fastify.register(fastifyMongodb, {
      forceClose: true,
      url: process.env.mongo_url
    });
  }

  let bucket: GridFSBucket;

  fastify.addHook('onReady', async () => {
    bucket = new GridFSBucket(fastify.mongo.db!);
    fastify.decorate('bucket', bucket);
  });

  fastify.decorate('insertOne', async (data: OptionalId<Document>, fastify: FastifyInstance) => {
    try {
      return fastify.mongo.db?.collection('medicalRecords').insertOne(data);
    } catch (error) {
      fastify.log.error(error, 'Error inserting document');
      throw new ServerException('Could not create medical record', 500);
    }
  });
  fastify.decorate('findOne', async (id: string, fastify: FastifyInstance) => {
    try {
      return fastify.mongo.db
        ?.collection('medicalRecords')
        .findOne({ _id: new fastify.mongo.ObjectId(id) });
    } catch (error) {
      fastify.log.error(error, 'Error finding document');
      throw new ServerException('Could not find medical record', 500);
    }
  });
  fastify.decorate(
    'addAttachments',
    async (record: WithId<Document>, attachments: Attachment[]) => {
      try {
        return fastify.mongo.db?.collection<MedicalRecord>('medicalRecords').updateOne(
          { _id: record._id },
          {
            $push: { attachments: { $each: attachments } }
          }
        );
      } catch (error) {
        fastify.log.error(error, 'Error updating attachments');
        throw new ServerException('Could not update attachments', 500);
      }
    }
  );
});
