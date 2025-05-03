import {afterEach, beforeEach, describe, expect, it, jest} from "@jest/globals";
import Fastify, {FastifyInstance} from "fastify";
import {options} from "../../src/app";
import billingService from "../../src/routes/billing-service";
import jwt from "../../src/plugins/jwt";
import kafkaPlugin from "../../src/plugins/kafka";
import mongoPlugin from "../../src/plugins/mongo";
import {mockJwtVerify, setInvalidUser} from "../__mocks__/@fastify/jwt";
import {mockFindOne, mockInsertOne, unAuthorizedId, unknownId} from "../__mocks__/@fastify/mongodb";
import {mockSend} from "../__mocks__/kafkajs";
import {ObjectId} from "mongodb";

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

    describe("Create bill tests", () => {
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

        it("should fail return 500 error if mongo fails to insert bill", async () => {
            mockInsertOne.mockResolvedValueOnce(undefined)

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
            expect(mockSend).toHaveBeenCalledTimes(0);
            const responseBody = result.json();
            expect(result.statusCode).toEqual(500);
            expect(responseBody.title).toEqual("Server Error");
            expect(responseBody.message).toEqual("Could not create bill");
            expect(responseBody.statusCode).toEqual("INTERNAL_SERVER_ERROR");

        });
    });

    describe("Get bill tests", () => {
        it("should get bill", async () => {
            await fastify.ready();
            const id = new ObjectId();
            const result = await fastify.inject({
                method: "GET",
                url: `/${id}`,
                headers: {
                    "Authorization": "Bearer test-token",
                }
            });

            expect(mockJwtVerify).toHaveBeenCalledTimes(1);
            expect(mockFindOne).toHaveBeenCalledTimes(1);
            expect(result.statusCode).toEqual(200);
        });

        it("should return a 404 error if the bill doesn't exist", async () => {
            await fastify.ready();
            const result = await fastify.inject({
                method: "GET",
                url: `/${unknownId}`,
                headers: {
                    "Authorization": "Bearer test-token",
                }
            });

            expect(mockJwtVerify).toHaveBeenCalledTimes(1);
            expect(mockFindOne).toHaveBeenCalledTimes(1);
            const responseBody = result.json();
            expect(result.statusCode).toEqual(404);
            expect(responseBody.title).toEqual("Bill Not Found");
            expect(responseBody.message).toEqual("The requested item does not exist");
            expect(responseBody.statusCode).toEqual("NOT_FOUND");
        });

        it("should return a 403 error if user doesn't own bill", async () => {
            setInvalidUser();

            await fastify.ready();
            const result = await fastify.inject({
                method: "GET",
                url: `/${unAuthorizedId}`,
                headers: {
                    "Authorization": "Bearer test-token",
                }
            });

            expect(mockJwtVerify).toHaveBeenCalledTimes(1);
            expect(mockFindOne).toHaveBeenCalledTimes(1);
            const responseBody = result.json();
            expect(result.statusCode).toEqual(403);
            expect(responseBody.title).toEqual("Authorization Failed");
            expect(responseBody.message).toEqual("Cannot access this resource");
            expect(responseBody.statusCode).toEqual("FORBIDDEN");
        });
    });

    describe("Update bill tests", () => {
        it("should update bill", async () => {
        });
    });
});