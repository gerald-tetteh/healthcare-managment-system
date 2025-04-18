import { FastifyPluginAsync } from 'fastify';
import { createMedicalRecordSchema, getMedicalRecordSchema } from '../../schemas/schemas';
import MedicalRecord from '../../models/MedicalRecord';
import Attachment from '../../models/Attachment';

const medicalRecords: FastifyPluginAsync = async (fastify, opts): Promise<void> => {
  fastify.post(
    '/',
    {
      schema: createMedicalRecordSchema,
      attachValidation: true,
      preHandler: [fastify.authenticate, fastify.authorizeByRole(['ROLE_DOCTOR', 'ROLE_ADMIN'])]
    },
    async function (request, reply) {
      if (request.validationError) {
        fastify.log.error(
          request.validationError,
          'Schema validation failed for create medical record'
        );
        reply.status(400).send({
          title: 'Bad Request',
          message: request.validationError.message,
          statusCode: 'BAD_REQUEST'
        });
        return;
      }
      const record = MedicalRecord.fromJson(request.body);
      const encryptedRecord = fastify.encrypt(record);
      const result = await fastify.insertOne(encryptedRecord, fastify);
      fastify.log.info(
        `User: ${request.user.userId} inserted medical record with id: ${result?.insertedId}`
      );
      return {
        status: 'success',
        message: 'Medical record created successfully',
        data: {
          insertedId: result?.insertedId
        }
      };
    }
  );

  fastify.get(
    '/:id',
    {
      schema: getMedicalRecordSchema,
      attachValidation: true,
      preHandler: [fastify.authenticate, fastify.authorizeByRole(['ROLE_DOCTOR', 'ROLE_ADMIN'])]
    },
    async function (request, reply) {
      if (request.validationError) {
        fastify.log.error(
          request.validationError,
          'Schema validation failed for get medical record'
        );
        reply.status(400).send({
          title: 'Bad Request',
          message: request.validationError.message,
          statusCode: 'BAD_REQUEST'
        });
        return;
      }
      const recordId = (request.params as { id: string }).id;
      const record = await fastify.findOne(recordId, fastify);
      if (!record) {
        reply.status(404).send({
          title: 'Record Not Found',
          message: 'The requested record does not exist',
          statusCode: 'NOT_FOUND'
        });
        return;
      }
      const user = request.user;
      const isPatient = user && user.roles.some(role => role === 'ROLE_PATIENT');
      if (isPatient && record.patientId !== user.userId) {
        reply.status(403).send({
          title: 'Authorization Failed',
          message: 'Cannot access this resource',
          statusCode: 'FORBIDDEN'
        });
        return;
      }
      const decryptedRecord = fastify.decrypt(record);
      fastify.log.info(
        `User: ${request.user.userId} fetched medical record with id: ${record?._id}`
      );
      return decryptedRecord;
    }
  );

  fastify.post(
    '/:id/attachments',
    {
      preHandler: [fastify.authenticate, fastify.authorizeByRole(['ROLE_DOCTOR', 'ROLE_ADMIN'])]
    },
    async function (request, reply) {
      const recordId = (request.params as { id: string }).id;
      const record = await fastify.findOne(recordId, fastify);
      if (!record) {
        reply.status(404).send({
          title: 'Record Not Found',
          message: 'The requested record does not exist',
          statusCode: 'NOT_FOUND'
        });
        return;
      }
      const data = request.files();
      const uploadedFiles: Attachment[] = [];
      for await (const part of data) {
        const id = await fastify.uploadAttachment(part, recordId, request.user.userId);
        uploadedFiles.push(new Attachment(part.filename, id, part.mimetype));
        fastify.log.info(`User: ${request.user.userId} uploaded file with id: ${id}`);
      }
      if (uploadedFiles.length === 0) {
        reply.status(400).send({
          title: 'Bad Request',
          message: 'No files were uploaded',
          statusCode: 'BAD_REQUEST'
        });
        return;
      }
      await fastify.addAttachments(record, uploadedFiles);
      fastify.log.info(
        `User: ${request.user.userId} added attachments to medical record with id: ${record._id}`
      );
      return {
        status: 'success',
        message: 'File uploaded successfully',
        data: uploadedFiles
      };
    }
  );

  // get attachment
  // update record
};

export default medicalRecords;
