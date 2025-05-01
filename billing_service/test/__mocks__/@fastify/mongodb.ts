import fp from "fastify-plugin";
import { OptionalId, Document, InsertOneOptions, InsertOneResult, ObjectId } from "mongodb";
import { jest } from "@jest/globals";

type insertOneType = (doc: OptionalId<Document>, options?: InsertOneOptions) => Promise<InsertOneResult>;

const mockInsertOne = jest.fn<insertOneType>().mockResolvedValue({
    insertedId: new ObjectId(),
    acknowledged: true,
});

export default fp(async (fastify) => {
    fastify.decorate("mongo", {
        db: {
            // @ts-ignore
            collection: (name: string) => {
                return {
                    insertOne: mockInsertOne,
                }
            }
        }
    });
});

export { mockInsertOne };