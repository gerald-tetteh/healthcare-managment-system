import { FastifyPluginAsync } from 'fastify'

const billingService: FastifyPluginAsync = async (fastify, opts): Promise<void> => {
  fastify.get('/', async function (request, reply) {
    return 'this is an billing_service'
  })
}

export default billingService;
