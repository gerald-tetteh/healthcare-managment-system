import {FastifyPluginAsync, FastifyRequest} from 'fastify'
import {createBillSchema, updateBillSchema} from "../../schemas/schemas";
import Bill from "../../models/Bill";
import {ObjectId} from "mongodb";
import BillStatus from "../../models/BillStatus";

const billingService: FastifyPluginAsync = async (fastify, opts): Promise<void> => {
  fastify.post('/', {
    attachValidation: true,
    schema: createBillSchema,
    preHandler: [fastify.authenticate, fastify.authorizeByRole(["ROLE_ADMIN"])]
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
    const { document, id } = await getPatientBill(request);
    if(!document) {
      reply.status(404).send({
        title: 'Bill Not Found',
        message: 'The requested item does not exist',
        statusCode: 'NOT_FOUND'
      });
      fastify.log.warn(`User ${request.user.userId} requested a non-existent document: ${id}`);
      return;
    }
    const bill = Bill.fromJson(document);
    if(!canAccessBill(request, bill)) {
      reply.status(403).send({
        title: 'Authorization Failed',
        message: 'Cannot access this resource',
        statusCode: 'FORBIDDEN'
      });
      fastify.log.warn(`User ${request.user.userId} tried to access unowned bill: ${id}`);
      return;
    }
    fastify.log.info(`User ${request.user.userId} accessed bill ${id}`);
    return bill;
  });

  fastify.patch("/:id", {
    attachValidation: true,
    schema: updateBillSchema,
    preHandler: [fastify.authenticate, fastify.authorizeByRole(["ROLE_ADMIN"])],
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
    const { document, id } = await getPatientBill(request);
    if(!document) {
      reply.status(404).send({
        title: 'Bill Not Found',
        message: 'The requested item does not exist',
        statusCode: 'NOT_FOUND'
      });
      fastify.log.warn(`User ${request.user.userId} requested a non-existent document: ${id}`);
      return;
    }
    const bill = Bill.fromDocument(document);
    if(bill.status != BillStatus.PENDING) {
      reply.status(400).send({
        title: 'Bad Request',
        message: "Bill can not be modified",
        statusCode: "BAD_REQUEST",
      });
      return;
    }
    const inputBill = Bill.fromJson(request.body);
    inputBill._id = bill._id;
    inputBill.createdAt = bill.createdAt;
    inputBill.updatedAt = new Date();
    inputBill.paidAt = undefined;
    const result = await fastify.updateBill(inputBill);
    if(!result) {
      reply.status(500).send({
        title: 'Server Error',
        message: "Could not update bill",
        statusCode: "INTERNAL_SERVER_ERROR",
      });
      return;
    }
    fastify.log.info(`User ${request.user.userId} updated bill ${id}`);
    return {
      status: "success",
      message: "Bill updated successfully",
      data: {
        bill: inputBill,
      }
    }
  });

  const getPatientBill = async (request: FastifyRequest) => {
    const inputId = (request.params as { id: string }).id;
    const billId = new ObjectId(inputId);
    return { document: await fastify.getBill(billId), id: inputId };
  }
  const canAccessBill = (request: FastifyRequest, bill: Bill) => {
    const isAdmin = request.user.roles.includes("ROLE_ADMIN");
    return isAdmin || bill.patientId === request.user.userId
  }
};

export default billingService;