import { FastifyPluginAsync } from 'fastify'

const billingService: FastifyPluginAsync = async (fastify, opts): Promise<void> => {
  fastify.post('/', {
    attachValidation: true,
    preHandler: [fastify.authenticate, fastify.authorizeByRole(["ROLE_BILLING_ADMIN","ROLE_ADMIN"])]
  }, async function (request, reply) {
    return 'this is an billing_service'
  })
}

export default billingService;
