import { FastifyPluginAsync } from 'fastify'

const medicalRecords: FastifyPluginAsync = async (fastify, opts): Promise<void> => {
  fastify.post('/', { 
    preHandler: [fastify.authenticate, fastify.authorizeByRole(["ROLE_DOCTOR", "ROLE_ADMIN"])] 
  }, async function (request, reply) {
    return request.user;
  })
};

export default medicalRecords;
