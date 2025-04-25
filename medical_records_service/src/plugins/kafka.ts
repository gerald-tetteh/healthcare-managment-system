import fp from 'fastify-plugin';
import { Kafka, ProducerRecord } from 'kafkajs';

const KAFKA_TOPIC = "medical_records";

declare module "fastify" {
  interface FastifyInstance {
    topic: string;
    publishKafka(data: ProducerRecord): Promise<void>;
  }
}

const kafka = new Kafka({
  brokers: [process.env.kafka_address!],
});
const producer = kafka.producer();

export default fp(async (fastify) => {
  fastify.addHook("onReady", async () => {
    await producer.connect();
    fastify.decorate("publishKafka", async (data: ProducerRecord) => {
      await producer.send(data);
    });
  });

  fastify.addHook("onClose", async () => {
    await producer.disconnect();
  });

  fastify.decorate("topic", KAFKA_TOPIC);
})