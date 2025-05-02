import { FastifyPluginAsync } from 'fastify'
import {createBillSchema} from "../../schemas/schemas";
import Bill from "../../models/Bill";
import {ObjectId} from "mongodb";

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

  fastify.get("/:id", {
    preHandler: [fastify.authenticate, fastify.authorizeByRole(["ROLE_PATIENT","ROLE_ADMIN"])],
  }, async (request, reply) => {
    const inputId = (request.params as { id: string }).id;
    const billId = new ObjectId(inputId);
    const document = await fastify.getBill(billId);
    if(!document) {
      reply.status(404).send({
        title: 'Bill Not Found',
        message: 'The requested item does not exist',
        statusCode: 'NOT_FOUND'
      });
      fastify.log.warn(`User ${request.user.userId} requested a non-existent document: ${inputId}`);
      return;
    }
    const bill = Bill.fromDocument(document);
    const isAdmin = request.user.roles.includes("ROLE_ADMIN");
    if(!isAdmin && bill.patientId != request.user.userId) {
      reply.status(403).send({
        title: 'Authorization Failed',
        message: 'Cannot access this resource',
        statusCode: 'FORBIDDEN'
      });
      fastify.log.warn(`User ${request.user.userId} tried to access unowned bill: ${inputId}`);
      return;
    }
    fastify.log.info(`User ${request.user.userId} accessed bill ${billId}`);
    return bill;
  });
};

export default billingService;