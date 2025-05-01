import {FastifySchema} from "fastify";
import BillItemType from "../models/BillItemType";

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