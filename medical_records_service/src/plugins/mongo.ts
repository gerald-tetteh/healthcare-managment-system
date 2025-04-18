import fp from 'fastify-plugin';
import fastifyMongodb from '@fastify/mongodb';
import { FastifyInstance, FastifyReply } from 'fastify';
import {
  Document,
  GridFSBucket,
  InsertOneResult,
  ObjectId,
  OptionalId,
  UpdateResult,
  WithId
} from 'mongodb';
import ServerException from '../models/ServerException';
import Attachment from '../models/Attachment';
import MedicalRecord from '../models/MedicalRecord';
import { MultipartFile } from '@fastify/multipart';
import { pipeline } from 'node:stream/promises';

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
  if (process.env.NODE_ENV !== 'test') {
    fastify.register(fastifyMongodb, {
      forceClose: true,
      url: process.env.mongo_url
    });
  }

  let bucket: GridFSBucket;

  if (process.env.NODE_ENV !== 'test') {
    fastify.addHook('onReady', async () => {
      bucket = new GridFSBucket(fastify.mongo.db!);
      fastify.decorate('bucket', bucket);
    });
  }

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
        const encryptedAttachments = fastify.encrypt(attachments);
        return fastify.mongo.db?.collection<MedicalRecord>('medicalRecords').updateOne(
          { _id: record._id },
          {
            $push: { attachments: { $each: encryptedAttachments } }
          }
        );
      } catch (error) {
        fastify.log.error(error, 'Error updating attachments');
        throw new ServerException('Could not update attachments', 500);
      }
    }
  );
  fastify.decorate('uploadAttachment', async (part: MultipartFile, id: string, userId: Number) => {
    try {
      const fileStream = fastify.bucket.openUploadStream(part.filename, {
        metadata: {
          recordId: id,
          uploadedAt: new Date(),
          uploadedBy: userId,
          contentType: part.mimetype
        }
      });
      await pipeline(part.file, fileStream);
      return fileStream.id;
    } catch (error) {
      fastify.log.error(error, 'Error uploading attachment');
      throw new ServerException('Could not upload attachment', 500);
    }
  });
  fastify.decorate('getAttachment', async (id: string, reply: FastifyReply) => {
    try {
      const downloadStream = fastify.bucket.openDownloadStream(new ObjectId(id));
      await pipeline(downloadStream, reply.raw);
    } catch (error) {
      fastify.log.error(error, 'Error getting attachment');
      throw new ServerException('Could not get attachment', 500);
    }
  });
});
