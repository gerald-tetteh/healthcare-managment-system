import { kafka } from "./kafka";
import fp from "fastify-plugin";

const consumer = kafka.consumer({
    groupId: "appointment-consumer",
});

export default fp(async (fastify) => {
    fastify.addHook("onReady", async () => {
        await consumer.connect();
        await consumer.subscribe({
            topic: "appointment",
        });

        await consumer.run({
            autoCommit: false,
            eachMessage: async ({message, topic, partition}) => {
                if(message.key?.toString() == "complete") {
                    fastify.log.info({
                        key: message.key?.toString(),
                        topic: topic.toString(),
                        partition: partition.toString(),
                        value: message.value?.toString(),
                    });
                }
                const commitOffset = Number(message.offset) + 1;
                await consumer.commitOffsets([{
                    topic: topic,
                    partition: partition,
                    offset: commitOffset.toString()
                }]);
            },
        });
    });

    fastify.addHook("onClose", async () => {
        await consumer.disconnect();
    });
});