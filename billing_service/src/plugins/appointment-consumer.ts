import { kafka } from "./kafka";
import fp from "fastify-plugin";
import Bill from "../models/Bill";
import BillItemType from "../models/BillItemType";
import got from "got";
import Appointment from "../models/Appointment";
import {Consumer, EachMessagePayload} from "kafkajs";
import { FastifyInstance } from "fastify";
import ServerException from "../models/ServerException";

const consumer = kafka.consumer({
    groupId: "appointment-consumer",
});

interface DoctorProfile {
    consultationFee: Number;
}

const consumeMessage = async (payload: EachMessagePayload, fastify: FastifyInstance, consumer: Consumer) => {
    if(payload.message.key?.toString() === "complete") {
        try {
            const token = fastify.jwt.sign({
                "userId": -1,
                "roles": ["ROLE_ADMIN"]
            });
            const appointment = JSON.parse(payload.message.value?.toString()!) as Appointment;
            const doctorProfile: DoctorProfile = await got({
                url: `http://doctors-service:8082/doctors/${appointment.doctorId}`,
                headers: {
                    "Authorization": `Bearer ${token}`,
                }
            }).json();
            const bill = new Bill({
                patientId: appointment.patientId,
                appointmentId: appointment.appointmentId,
                items: [{
                    type: BillItemType.APPOINTMENT,
                    price: doctorProfile.consultationFee,
                    identifier: "Appointment"
                }],
            });
            const result = await fastify.createBill(bill);
            fastify.log.info(`Created bill: ${result?.insertedId} for appointment: ${appointment.appointmentId}`);
            await fastify.publishKafka({
                topic: "bill",
                messages: [{
                    key: "create",
                    value: JSON.stringify({
                        users: [appointment.doctorId, appointment.patientId],
                        data: bill
                    }),
                }],
            });
            fastify.log.info(`Published kafka message for bill: ${result?.insertedId}`);
            const commitOffset = Number(payload.message.offset) + 1;
            await consumer.commitOffsets([{
                topic: payload.topic,
                partition: payload.partition,
                offset: commitOffset.toString()
            }]);
            fastify.log.info(`Committed offset ${commitOffset} of topic ${payload.topic}`);
        } catch (error) {
            fastify.log.error(error, "Could not process appointment bill")
            throw new ServerException("Could not process appointment bill", 500);
        }
    } else {
        const commitOffset = Number(payload.message.offset) + 1;
        await consumer.commitOffsets([{
            topic: payload.topic,
            partition: payload.partition,
            offset: commitOffset.toString()
        }]);
        fastify.log.info(`Did not process message with key: ${payload.topic} Committed offset ${commitOffset} of topic ${payload.topic}`);
    }
}

export default fp(async (fastify) => {
    fastify.addHook("onReady", async () => {
        await consumer.connect();
        await consumer.subscribe({
            topic: "appointment",
        });

        consumer.run({
            autoCommit: false,
            eachMessage: async (payload) => {
                await consumeMessage(payload, fastify, consumer);
            },
        });
    });

    fastify.addHook("onClose", async () => {
        await consumer.disconnect();
    });
});

export { consumeMessage };