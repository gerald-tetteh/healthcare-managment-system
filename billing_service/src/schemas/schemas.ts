import {FastifySchema} from "fastify";
import BillItemType from "../models/BillItemType";
import BillStatus from "../models/BillStatus";

export const createBillSchema: FastifySchema = {
    body: {
        type: "object",
        additionalProperties: false,
        required: ["patientId", "appointmentId", "items"],
        properties: {
            patientId: { type: "number" },
            appointmentId: { type: "number" },
            items: {
                type: "array",
                items: {
                    type: "object",
                    properties: {
                        type: {
                            type: "string",
                            enum: Object.values(BillItemType),
                        },
                        price: { type: "number" },
                        identifier: { type: "string" },
                    },
                }
            },
        }
    }
};

export const updateBillSchema: FastifySchema = {
    body: {
        type: "object",
        additionalProperties: false,
        required: ["patientId", "appointmentId", "items"],
        properties: {
            patientId: { type: "number" },
            appointmentId: { type: "number" },
            items: {
                type: "array",
                items: {
                    type: "object",
                    properties: {
                        type: {
                            type: "string",
                            enum: Object.values(BillItemType),
                        },
                        price: { type: "number" },
                        identifier: { type: "string" },
                    },
                }
            },
            status: { type: "string", enum: Object.values(BillStatus) },
            createdAt: { type: "string", format: "date-time" },
            updatedAt: { type: "string", format: "date-time" },
            paidAt: { type: "string", format: "date-time" },
        }
    }
}