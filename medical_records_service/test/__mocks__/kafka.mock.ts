import { ProducerRecord } from 'kafkajs';
import fp from "fastify-plugin";

declare module "fastify" {
  interface FastifyInstance {
    topic: string;
    publishKafka(data: ProducerRecord): Promise<void>;
  }
}

export default fp(async (fastify) => {
  fastify.decorate("topic", "test");
  fastify.decorate("publishKafka", async (data: ProducerRecord) => {});
});