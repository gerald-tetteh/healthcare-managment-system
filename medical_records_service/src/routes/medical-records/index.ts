import { FastifyPluginAsync } from 'fastify';
import { createMedicalRecordSchema, getMedicalRecordSchema } from '../../schemas/schemas';
import MedicalRecord from '../../models/MedicalRecord';

const medicalRecords: FastifyPluginAsync = async (fastify, opts): Promise<void> => {
  fastify.post('/', {
    schema: createMedicalRecordSchema,
    preHandler: [fastify.authenticate, fastify.authorizeByRole(["ROLE_DOCTOR", "ROLE_ADMIN"])] 
  }, async function (request, reply) {
    const record = MedicalRecord.fromJson(request.body);
    const encryptedRecord = fastify.encrypt(record);
    const result = await fastify.mongo.db?.collection('medicalRecords').insertOne(encryptedRecord);
    fastify.log.info(`Inserted medical record with id: ${result?.insertedId}`);
    return {
      status: 'success',
      message: 'Medical record created successfully',
      data: {
        insertedId: result?.insertedId,
      }
    };
  });

  fastify.get("/:id", {
    schema: getMedicalRecordSchema,
    preHandler: [fastify.authenticate, fastify.authorizeByRole(["ROLE_DOCTOR", "ROLE_ADMIN"])]
  }, async function (request, reply) {
    const id = (request.params as { id: string }).id;
    const record = await fastify.mongo.db?.collection('medicalRecords').findOne(
      { _id: new fastify.mongo.ObjectId(id) }
    );
    const decryptedRecord = fastify.decrypt(record);
    return decryptedRecord;
  });
};

export default medicalRecords;
