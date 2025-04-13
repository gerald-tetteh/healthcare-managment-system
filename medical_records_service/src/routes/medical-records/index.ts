import { FastifyPluginAsync } from 'fastify';
import { createMedicalRecordSchema } from '../../schemas/schemas';
import MedicalRecord from '../../models/MedicalRecord';

const medicalRecords: FastifyPluginAsync = async (fastify, opts): Promise<void> => {
  fastify.post('/', {
    schema: createMedicalRecordSchema,
    preHandler: [fastify.authenticate, fastify.authorizeByRole(["ROLE_DOCTOR", "ROLE_ADMIN"])] 
  }, async function (request, reply) {
    const record = MedicalRecord.fromJson(request.body);
    const result = await fastify.mongo.db?.collection<MedicalRecord>('medicalRecords').insertOne(record);
    fastify.log.info(`Inserted medical record with id: ${result?.insertedId}`);
    return {
      status: 'success',
      message: 'Medical record created successfully',
      data: {
        insertedId: result?.insertedId,
      }
    };
  });
};

export default medicalRecords;
