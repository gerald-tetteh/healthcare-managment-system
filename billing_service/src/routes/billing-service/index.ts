import { FastifyPluginAsync } from 'fastify'
import {createBillSchema} from "../../schemas/schemas";
import Bill from "../../models/Bill";

const billingService: FastifyPluginAsync = async (fastify, opts): Promise<void> => {
  fastify.post('/', {
    attachValidation: true,
    schema: createBillSchema,
    preHandler: [fastify.authenticate, fastify.authorizeByRole(["ROLE_DOCTOR","ROLE_ADMIN"])]
  }, async function (request, reply) {
    if (request.validationError) {
      fastify.log.error(
          request.validationError,
          'Schema validation failed for create bill'
      );
      reply.status(400).send({
        title: 'Bad Request',
        message: request.validationError.message,
        statusCode: 'BAD_REQUEST'
      });
      return;
    }
    const bill = Bill.fromJson(request.body);
    const result = await fastify.createBill(bill);
    fastify.log.info(`User: ${request.user.userId} inserted a new bill with id ${result?.insertedId}`);
    fastify.publishKafka({
      topic: fastify.topic,
      messages: [{
        key: "create",
        value: JSON.stringify({
          users: [bill.patientId],
          data: bill
        }),
      }],
    }).then(() => fastify.log.info(`Published kafka message for bill: ${result?.insertedId}`));
    return {
      status: "success",
      message: "Bill created successfully",
      data: {
        insertedId: result?.insertedId,
      }
    }
  });
};

export default billingService;