import fp from 'fastify-plugin';
import fastifyMultiPart from '@fastify/multipart';

export default fp(async (fastify, options) => {
  fastify.register(fastifyMultiPart, {
    limits: {
      fileSize: 5 * 1024 * 1024 // 5 MB
    }
  });
});
