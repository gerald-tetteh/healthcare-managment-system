import fp from "fastify-plugin";
import {
    Document,
    Filter,
    InsertOneOptions,
    InsertOneResult,
    ObjectId,
    OptionalId,
    UpdateFilter,
    UpdateOptions,
    UpdateResult,
    WithId
} from "mongodb";
import {jest} from "@jest/globals";
import BillStatus from "../../../src/models/BillStatus";

type insertOneType = (doc: OptionalId<Document>, options?: InsertOneOptions) => Promise<InsertOneResult | undefined>;
type findOneType = (filter: Filter<Document>) => Promise<WithId<Document> | undefined | null>;
type updateOneType = (filter: Filter<Document>, update: Document[] | UpdateFilter<Document>, options?: UpdateOptions) => Promise<UpdateResult | undefined>;

const unknownId = new ObjectId();
const unAuthorizedId = new ObjectId();
const notPendingId = new ObjectId();
const mockInsertOne = jest.fn<insertOneType>().mockResolvedValue({
    insertedId: new ObjectId(),
    acknowledged: true,
});
const mockFindOne = jest.fn<findOneType>().mockImplementation((filter) => {
    const id = filter._id!.toString();
    if(id === unknownId.toString()) {
        return Promise.resolve(null);
    }
    if(id === unAuthorizedId.toString()) {
        return Promise.resolve({
            _id: new ObjectId(id),
            patientId: -1,
        });
    }
    if (id === notPendingId.toString()) {
        return Promise.resolve({
            _id: new ObjectId(id),
            status: BillStatus.PAID
        });
    }
    return Promise.resolve({
        _id: new ObjectId(id),
    });
});
const mockUpdateOne = jest.fn<updateOneType>().mockResolvedValue({
    acknowledged: true,
    matchedCount: 1,
    modifiedCount: 1,
    upsertedCount: 0,
    upsertedId: null,
})

export default fp(async (fastify) => {
    fastify.decorate("mongo", {
        db: {
            // @ts-ignore
            collection: (name: string) => {
                return {
                    insertOne: mockInsertOne,
                    findOne: mockFindOne,
                    updateOne: mockUpdateOne,
                }
            }
        }
    });
});

export {mockInsertOne, mockFindOne, mockUpdateOne, notPendingId, unknownId, unAuthorizedId};