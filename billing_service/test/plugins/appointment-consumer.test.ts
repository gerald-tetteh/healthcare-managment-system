import {jest, describe, it, beforeEach, afterEach, expect} from "@jest/globals";
import appointmentConsumer, { consumeMessage } from "../../src/plugins/appointment-consumer";
import Fastify from "fastify";
import mongo from "../../src/plugins/mongo";
import {options} from "../../src/app";
import {EachMessagePayload, Kafka} from "kafkajs";
import Appointment from "../../src/models/Appointment";
import jwt from "../../src/plugins/jwt"
import kafka from "../../src/plugins/kafka";
import {mockSign} from "../__mocks__/@fastify/jwt";
import {mockGotJson} from "../__mocks__/got";
import {mockInsertOne} from "../__mocks__/@fastify/mongodb";
import {mockCommitOffsets, mockConnect, mockDisconnect, mockRun, mockSend, mockSubscribe} from "../__mocks__/kafkajs";

describe("Appointment consumer tests", () => {
    let fastify = Fastify(options);

    beforeEach(async () => {
        jest.mock("@fastify/jwt");
        jest.mock("@fastify/mongodb");
        jest.mock("kafkajs");
        jest.mock("got");

        fastify = Fastify(options);
        await fastify.register(jwt);
        await fastify.register(kafka);
        await fastify.register(mongo);
    });

    afterEach(() => {
        jest.clearAllMocks();
    });

    it("should register plugin", async () => {
        await fastify.register(appointmentConsumer);
        await fastify.ready();

        // kafka module also calls connect for the producer
        expect(mockConnect).toHaveBeenCalledTimes(2);
        expect(mockSubscribe).toHaveBeenCalledTimes(1);
        expect(mockRun).toHaveBeenCalledTimes(1);

        await fastify.close();

        // kafka module also calls disconnect for producer
        expect(mockDisconnect).toHaveBeenCalledTimes(2);
    });

    it("should process completed appointment", async () => {
        await fastify.ready();
        const appointment: Appointment = {
            appointmentId: 0,
            patientId: 1,
            doctorId: 2,
            status: "COMPLETED",
            notes: "test appointment",
            dateTime: new Date(),
        };
        const appointmentJson = JSON.stringify(appointment);
        const payload: EachMessagePayload = {
            topic: 'test',
            partition: 0,
            message: {
                key: Buffer.from("complete", "utf-8"),
                value: Buffer.from(appointmentJson, "utf-8"),
                timestamp: new Date().toISOString(),
                attributes: 3,
                offset: "0",
                size: 111,
            },
            heartbeat: async (): Promise<void> => {
            },
            pause: () => {
                return () => {
                };
            }
        }
        const mockKafka = new Kafka({brokers: []});

        await consumeMessage(payload,fastify,mockKafka.consumer({ groupId: "test"}));

        expect(mockSign).toHaveBeenCalledTimes(1);
        expect(mockGotJson).toHaveBeenCalledTimes(1);
        expect(mockInsertOne).toHaveBeenCalledTimes(1);
        expect(mockSend).toHaveBeenCalledTimes(1);
        expect(mockCommitOffsets).toHaveBeenCalledTimes(1);
    });

    it("should skip kafka messages without the key 'complete'", async () => {
        await fastify.ready();
        const appointment: Appointment = {
            appointmentId: 0,
            patientId: 1,
            doctorId: 2,
            status: "PENDING",
            notes: "test appointment",
            dateTime: new Date(),
        };
        const appointmentJson = JSON.stringify(appointment);
        const payload: EachMessagePayload = {
            topic: 'test',
            partition: 0,
            message: {
                key: Buffer.from("create", "utf-8"),
                value: Buffer.from(appointmentJson, "utf-8"),
                timestamp: new Date().toISOString(),
                attributes: 3,
                offset: "0",
                size: 111,
            },
            heartbeat: async (): Promise<void> => {
            },
            pause: () => {
                return () => {
                };
            }
        }
        const mockKafka = new Kafka({brokers: []});

        await consumeMessage(payload,fastify,mockKafka.consumer({ groupId: "test" }));

        expect(mockSign).toHaveBeenCalledTimes(0);
        expect(mockGotJson).toHaveBeenCalledTimes(0);
        expect(mockInsertOne).toHaveBeenCalledTimes(0);
        expect(mockSend).toHaveBeenCalledTimes(0);
        expect(mockCommitOffsets).toHaveBeenCalledTimes(1);
    });
});