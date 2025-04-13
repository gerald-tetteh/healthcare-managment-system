import { FastifyInstance, FastifyPluginAsync, FastifyReply, FastifyRequest } from 'fastify';
import { createMedicalRecordSchema, getMedicalRecordSchema } from '../../schemas/schemas';
import MedicalRecord from '../../models/MedicalRecord';

const medicalRecords: FastifyPluginAsync = async (fastify, opts): Promise<void> => {
  fastify.post('/', {
    schema: createMedicalRecordSchema,
    preHandler: [
      fastify.authenticate, 
      fastify.authorizeByRole(["ROLE_DOCTOR", "ROLE_ADMIN"])
    ] 
  }, async function (request, reply) {
    const record = MedicalRecord.fromJson(request.body);
    const encryptedRecord = fastify.encrypt(record);
    const result = await fastify.mongo.db?.collection('medicalRecords').insertOne(encryptedRecord);
    fastify.log.info(`User: ${request.user.userId} inserted medical record with id: ${result?.insertedId}`);
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
    preHandler: [
      fastify.authenticate, 
      fastify.authorizeByRole(["ROLE_DOCTOR", "ROLE_ADMIN"])
    ]
  }, async function (request, reply) {
    const record = await getMedicalRecord(fastify, request, reply);
    const decryptedRecord = fastify.decrypt(record);
    fastify.log.info(`User: ${request.user.userId} fetched medical record with id: ${record?._id}`);
    return decryptedRecord;
  });
};

const getMedicalRecord = async (fastify: FastifyInstance, request: FastifyRequest, reply: FastifyReply) => {
  const recordId = (request.params as { id: string }).id;
  const record = await fastify.mongo.db?.collection('medicalRecords').findOne({ _id: new fastify.mongo.ObjectId(recordId) });
  if (!record) {
    reply.status(404).send({ 
      title: "Record Not Found", 
      message: "The requested record does not exist", 
      statusCode: "NOT_FOUND" 
    });
    return;
  }
  const user = request.user;
  const isPatient = user && user.roles.some(role => role === "ROLE_PATIENT");
  if (isPatient && record.patientId !== user.userId) {
    reply.status(403).send({ 
      title: "Authorization Failed", 
      message: "Cannot access this resource", 
      statusCode: "FORBIDDEN" 
    });
    return;
  }
  return record;
};

export default medicalRecords;
