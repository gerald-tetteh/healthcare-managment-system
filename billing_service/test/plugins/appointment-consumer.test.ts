import {jest, describe, it, beforeEach} from "@jest/globals";
import { consumeMessage } from "../../src/plugins/appointment-consumer";
import Fastify from "fastify";
import mongo from "../../src/plugins/mongo";
import {options} from "../../src/app";
import {EachMessagePayload} from "kafkajs";
import Appointment from "../../src/models/Appointment";
import jwt from "../../src/plugins/jwt"
import kafka from "../../src/plugins/kafka";

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
        await fastify.ready();
    });

    it("should process completed appointment", async () => {
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
        const mockConsumer = {
            commitOffsets: jest.fn(() => Promise.resolve()),
        }
        // @ts-ignore
        await consumeMessage(payload,fastify,mockConsumer);
    });
});