import {afterEach, beforeEach, describe, expect, it, jest} from "@jest/globals";
import Fastify, {FastifyInstance} from "fastify";
import {options} from "../../src/app";
import kafkaPlugin from "../../src/plugins/kafka";
import {mockConnect, mockDisconnect, mockSend} from "../__mocks__/kafkajs";

describe("Kafka plugin tests", () => {
    let fastify: FastifyInstance;
    beforeEach(async () => {
        jest.mock("kafkajs");

        fastify = Fastify(options);
        await fastify.register(kafkaPlugin);
    });

    afterEach(() => {
        jest.clearAllMocks();
    })

    it("should load kafka plugin", async () => {
        await fastify.ready();

        expect(mockConnect).toHaveBeenCalledTimes(1);
        expect(fastify.publishKafka).toBeDefined();
        expect(fastify.topic).toBeDefined();

        await fastify.close();

        expect(mockDisconnect).toHaveBeenCalledTimes(1);
    });

    it("should publish kafka messages", async () => {
        await fastify.ready();

        await fastify.publishKafka({
            topic: "test",
            messages: [],
        });

        expect(mockSend).toHaveBeenCalledTimes(1);
    });
});