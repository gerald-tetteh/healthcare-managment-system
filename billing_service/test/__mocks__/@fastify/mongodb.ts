import fp from "fastify-plugin";
import { OptionalId, Document, InsertOneOptions, InsertOneResult, ObjectId } from "mongodb";

export default fp(async (fastify) => {
    fastify.decorate("mongo", {
        db: {
            // @ts-ignore
            collection: (name: string) => {
                return {
                    insertOne: async (doc: OptionalId<Document>, options?: InsertOneOptions) => {
                        return Promise.resolve(<InsertOneResult>{
                            insertedId: new ObjectId(),
                            acknowledged: true,
                        });
                    }
                }
            }
        }
    });
});