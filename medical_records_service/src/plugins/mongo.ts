import fp from "fastify-plugin";
import fastifyMongodb from "@fastify/mongodb";
import { FastifyInstance } from "fastify";
import { Document, InsertOneResult, OptionalId, WithId } from "mongodb";
import ServerException from "../models/ServerException";

declare module "fastify" {
  interface FastifyInstance {
    insertOne: (
      data: OptionalId<Document>,
      fastify: FastifyInstance
    ) => Promise<InsertOneResult<Document> | undefined>;
    findOne: (
      id: string,
      fastify: FastifyInstance
    ) => Promise<WithId<Document> | null | undefined>;
  }
}

export default fp(async (fastify, options) => {
  if (process.env.NODE_ENV !== "test") {
    fastify.register(fastifyMongodb, {
      forceClose: true,
      url: process.env.mongo_url,
    });
  }

  fastify.decorate(
    "insertOne",
    async (data: OptionalId<Document>, fastify: FastifyInstance) => {
      try {
        return fastify.mongo.db?.collection("medicalRecords").insertOne(data);
      } catch (error) {
        fastify.log.error("Error inserting document: ", error);
        throw new ServerException("Could not create medical record", 500);
      }
    }
  );
  fastify.decorate("findOne", async (id: string, fastify: FastifyInstance) => {
    try {
      return fastify.mongo.db
        ?.collection("medicalRecords")
        .findOne({ _id: new fastify.mongo.ObjectId(id) });
    } catch (error) {
      fastify.log.error("Error finding document: ", error);
      throw new ServerException("Could not find medical record", 500);
    }
  });
});
