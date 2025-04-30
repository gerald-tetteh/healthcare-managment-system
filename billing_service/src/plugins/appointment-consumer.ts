import { kafka } from "./kafka";
import fp from "fastify-plugin";
import Bill from "../models/Bill";
import BillItemType from "../models/BillItemType";
import got from "got";
import Appointment from "../models/Appointment";

const consumer = kafka.consumer({
    groupId: "appointment-consumer",
});

interface DoctorProfile {
    consultationFee: Number;
}

export default fp(async (fastify) => {
    fastify.addHook("onReady", async () => {
        await consumer.connect();
        await consumer.subscribe({
            topic: "appointment",
        });

        consumer.run({
            autoCommit: false,
            eachMessage: async ({message, topic, partition}) => {
                const token = fastify.jwt.sign({
                    "userId": -1,
                    "roles": ["ROLE_ADMIN"]
                });
                if(message.key?.toString() === "complete") {
                    try {
                        const appointment = JSON.parse(message.value?.toString()!) as Appointment;
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
                        const commitOffset = Number(message.offset) + 1;
                        await consumer.commitOffsets([{
                            topic: topic,
                            partition: partition,
                            offset: commitOffset.toString()
                        }]);
                    } catch (error) {
                        fastify.log.error(error, "Could not process appointment bill")
                        console.log(error);
                    }
                } else {
                    const commitOffset = Number(message.offset) + 1;
                    await consumer.commitOffsets([{
                        topic: topic,
                        partition: partition,
                        offset: commitOffset.toString()
                    }]);
                }
            },
        });
    });

    fastify.addHook("onClose", async () => {
        await consumer.disconnect();
    });
});