import MongoDocument from "./MongoDocument";
import {ObjectId} from "@fastify/mongodb";
import BillStatus from "./BillStatus";
import BillItem from "./BillItem";

class Bill implements MongoDocument {
    _id: ObjectId | undefined;
    status: BillStatus;
    items: BillItem[];
    paidAt: Date;
    createdAt: Date;
    updatedAt: Date;

    constructor(
        _id: string,
        status: BillStatus,
        items: BillItem[],
        paidAt: Date,
        createdAt: Date = new Date(),
        updatedAt: Date = new Date(),
    ) {
        this._id = _id ? new ObjectId(_id) : undefined;
        this.items = items;
        this.status = status;
        this.items = items;
        this.paidAt = paidAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    static fromJson(json:any): Bill {
        return new Bill(
            json.id,
            BillStatus[json.visitType as keyof typeof BillStatus],
            json.items,
            json.paidAt,
            json.createdAt ? new Date(json.createdAt) : new Date(),
        );
    }
}

export default Bill;