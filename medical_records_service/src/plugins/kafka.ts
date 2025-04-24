import fp from 'fastify-plugin';
import { Kafka, Producer } from 'kafkajs';

const KAFKA_TOPIC = "medical_records";

declare module "fastify" {
  interface FastifyInstance {
    topic: string;
    kafka: Producer;
  }
}

const kafka = new Kafka({
  brokers: [process.env.kafka_address!],
});
const producer = kafka.producer();

export default fp(async (fastify) => {
  fastify.addHook("onReady", async () => {
    await producer.connect();
    fastify.decorate("kafka", producer);
  });

  fastify.addHook("onClose", async () => {
    await producer.disconnect();
  });

  fastify.decorate("topic", KAFKA_TOPIC);
})