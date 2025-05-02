import fp from "fastify-plugin";
import { OptionalId, Document, InsertOneOptions, InsertOneResult, ObjectId, Filter, WithId } from "mongodb";
import { jest } from "@jest/globals";

type insertOneType = (doc: OptionalId<Document>, options?: InsertOneOptions) => Promise<InsertOneResult>;
type findOneType = (filter: Filter<Document>) => Promise<WithId<Document> | undefined | null>;

const mockInsertOne = jest.fn<insertOneType>().mockResolvedValue({
    insertedId: new ObjectId(),
    acknowledged: true,
});
const mockFindOne = jest.fn<findOneType>().mockImplementation((filter) => {
    const id = filter._id!.toString();
    if(id === "unknown") {
        return Promise.resolve(null);
    }
    return Promise.resolve({
        _id: new ObjectId(filter._id!.toString()),
    });
});

export default fp(async (fastify) => {
    fastify.decorate("mongo", {
        db: {
            // @ts-ignore
            collection: (name: string) => {
                return {
                    insertOne: mockInsertOne,
                    findOne: mockFindOne,
                }
            }
        }
    });
});

export { mockInsertOne, mockFindOne };