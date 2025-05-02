import {afterEach, beforeEach, describe, expect, it, jest} from "@jest/globals";
import Fastify, {FastifyInstance} from "fastify";
import {options} from "../../src/app";
import billingService from "../../src/routes/billing-service";
import jwt from "../../src/plugins/jwt";
import kafkaPlugin from "../../src/plugins/kafka";
import mongoPlugin from "../../src/plugins/mongo";
import {mockJwtVerify} from "../__mocks__/@fastify/jwt";
import {mockInsertOne} from "../__mocks__/@fastify/mongodb";
import {mockSend} from "../__mocks__/kafkajs";

describe("Billing Service tests", () => {
    let fastify: FastifyInstance;
    beforeEach(async () => {
        jest.mock("kafkajs");
        jest.mock("@fastify/mongodb");
        jest.mock("@fastify/jwt");

        fastify = Fastify(options);
        await fastify.register(jwt);
        await fastify.register(kafkaPlugin);
        await fastify.register(mongoPlugin);
        await fastify.register(billingService);
    });

    afterEach(async () => {
        jest.clearAllMocks();
    });

    it("should create a bill", async () => {
        await fastify.ready();

        const result = await fastify.inject({
            method: "POST",
            url: "/",
            body: {
                patientId: 1,
                appointmentId: 2,
                items: [
                    {
                        type: "APPOINTMENT",
                        price: 100.45,
                        identifier: "consultation",
                    }
                ],
            },
            headers: {
                "Authorization": "Bearer test-token"
            }
        });

        expect(mockJwtVerify).toHaveBeenCalledTimes(1);
        expect(mockInsertOne).toHaveBeenCalledTimes(1);
        expect(mockSend).toHaveBeenCalledTimes(1);

        const responseBody = result.json();

        expect(result.statusCode).toEqual(200);
        expect(responseBody.status).toEqual("success");
        expect(responseBody.message).toEqual("Bill created successfully");
        expect(responseBody.data.insertedId).toBeDefined();
    });

    it("should fail to create a bill due missing property", async () => {
        await fastify.ready();

        const result = await fastify.inject({
            method: "POST",
            url: "/",
            body: {
                appointmentId: 2,
                items: [
                    {
                        type: "APPOINTMENT",
                        price: 100.45,
                        identifier: "consultation",
                    }
                ],
            },
            headers: {
                "Authorization": "Bearer test-token"
            }
        });

        expect(mockJwtVerify).toHaveBeenCalledTimes(1);
        expect(mockInsertOne).toHaveBeenCalledTimes(0);
        expect(mockSend).toHaveBeenCalledTimes(0);

        const responseBody = result.json();

        expect(result.statusCode).toEqual(400);
        expect(responseBody.title).toEqual("Bad Request");
        expect(responseBody.message).toBeDefined();
        expect(responseBody.statusCode).toEqual("BAD_REQUEST");
    });

    it("should fail to create a bill due to extra property", async () => {
        await fastify.ready();

        const result = await fastify.inject({
            method: "POST",
            url: "/",
            body: {
                patientId: 1,
                doctorId: 2,
                appointmentId: 2,
                items: [
                    {
                        type: "APPOINTMENT",
                        price: 100.45,
                        identifier: "consultation",
                    }
                ],
            },
            headers: {
                "Authorization": "Bearer test-token"
            }
        });

        expect(mockJwtVerify).toHaveBeenCalledTimes(1);
        expect(mockInsertOne).toHaveBeenCalledTimes(0);
        expect(mockSend).toHaveBeenCalledTimes(0);

        const responseBody = result.json();

        expect(result.statusCode).toEqual(400);
        expect(responseBody.title).toEqual("Bad Request");
        expect(responseBody.message).toBeDefined();
        expect(responseBody.statusCode).toEqual("BAD_REQUEST");
    });
});