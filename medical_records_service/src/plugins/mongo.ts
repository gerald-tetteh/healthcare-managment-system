import fp from "fastify-plugin";
import fastifyMongodb from "@fastify/mongodb";

export default fp(async (fastify, options) => {
  fastify.register(fastifyMongodb, {
    forceClose: true,
    url: process.env.mongo_url,
  });
});