import MongoDocument from "./MongoDocument";
import {ObjectId} from "@fastify/mongodb";
import BillStatus from "./BillStatus";
import BillItem from "./BillItem";

interface IBill {
    _id?: string,
    patientId: Number,
    appointmentId: Number,
    status?: BillStatus,
    items: BillItem[],
    paidAt?: Date,
    createdAt?: Date,
    updatedAt?: Date,
}

class Bill implements MongoDocument {
    _id?: ObjectId;
    status: BillStatus;
    patientId: Number;
    appointmentId: Number;
    items: BillItem[];
    paidAt?: Date;
    createdAt: Date;
    updatedAt: Date;

    constructor(obj: IBill) {
        this._id = obj._id ? new ObjectId(obj._id) : undefined;
        this.items = obj.items ?? [];
        this.status = obj.status ?? BillStatus.PENDING;
        this.patientId = obj.patientId;
        this.appointmentId = obj.appointmentId;
        this.paidAt = obj.paidAt;
        this.createdAt = obj.createdAt ?? new Date();
        this.updatedAt = obj.updatedAt ?? new Date();
    }

    static fromJson(json:any): Bill {
        return new Bill({
            _id: json.id,
            status: BillStatus[json.visitType as keyof typeof BillStatus],
            patientId: json.patientId,
            appointmentId: json.appointmentId,
            items: json.items,
            paidAt: json.paidAt,
            createdAt: json.createdAt,
            updatedAt: json.updatedAt,
        });
    }
}

export default Bill;