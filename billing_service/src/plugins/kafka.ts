import {Kafka, ProducerRecord} from "kafkajs";
import fp from "fastify-plugin";

declare module "fastify" {
    interface FastifyInstance {
        publishKafka(message: ProducerRecord): Promise<void>;
    }
}

const KAFKA_TOPIC = "billing_service";
const kafka = new Kafka({
    brokers: [process.env.kafka_address!],
});
const producer = kafka.producer();

export default fp(async (fastify) => {
    fastify.addHook("onReady", async () => {
        await producer.connect();
        fastify.decorate("publishKafka", async (message: ProducerRecord) => {
            await producer.send(message);
        });
    });

    fastify.addHook("onClose", async () => {
        await producer.disconnect();
    });

    fastify.decorate("topic", KAFKA_TOPIC);
});

export { kafka };