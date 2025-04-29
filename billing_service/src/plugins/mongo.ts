import mongodb from "@fastify/mongodb";
import { InsertOneResult } from "mongodb";
import fp from "fastify-plugin";
import Bill from "../models/Bill";
import ServerException from "../models/ServerException";

declare module "fastify" {
    interface FastifyInstance {
        createBill(bill: Bill): Promise<InsertOneResult | undefined>;
    }
}

export default fp(async (fastify) => {
    fastify.register(mongodb, {
        forceClose: false,
        url: process.env.mongo_url,
    });

    fastify.decorate("createBill", async (bill: Bill) => {
        try {
            return fastify.mongo.db?.collection("bills").insertOne(bill);
        } catch (error) {
            fastify.log.error(error, "Failed to insert bill");
            throw new ServerException("Could not create bill", 500);
        }
    });
});