import { ObjectId } from "@fastify/mongodb";

interface MongoDocument {
  _id?: ObjectId | undefined;
  createdAt: Date;
  updatedAt: Date;
};

export default MongoDocument;