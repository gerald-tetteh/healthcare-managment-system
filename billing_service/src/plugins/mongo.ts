import mongodb, {ObjectId} from "@fastify/mongodb";
import {Document, InsertOneResult, WithId, UpdateResult} from "mongodb";
import fp from "fastify-plugin";
import Bill from "../models/Bill";
import ServerException from "../models/ServerException";

declare module "fastify" {
    interface FastifyInstance {
        createBill(bill: Bill): Promise<InsertOneResult | undefined>;
        getBill(id: ObjectId): Promise<WithId<Document> | null | undefined>;
        updateBill(bill: Bill): Promise<UpdateResult<Bill> | undefined>;
    }
}

export default fp(async (fastify) => {
    fastify.register(mongodb, {
        forceClose: false,
        url: process.env.mongo_url,
    });

    fastify.decorate("createBill", async (bill: Bill) => {
        try {
            return fastify.mongo.db?.collection<Bill>("bills").insertOne(bill);
        } catch (error) {
            fastify.log.error(error, "Failed to insert bill");
            throw new ServerException("Could not create bill", 500);
        }
    });

    fastify.decorate("getBill", async (id: ObjectId) => {
        try {
            return fastify.mongo.db?.collection<Bill>("bills").findOne({ _id: id });
        } catch (error) {
            fastify.log.error(error, "Failed to get bill");
            throw new ServerException("Could not find bill", 500);
        }
    });

    fastify.decorate("updateBill", async (bill: Bill) => {
        try {
            return fastify.mongo.db?.collection<Bill>("bills").updateOne(
                { _id: bill._id },
                { $set: bill }
            );
        } catch (error) {
            fastify.log.error(error, "Failed to update bill");
            throw new ServerException("Could not update bill", 500);
        }
    });
});