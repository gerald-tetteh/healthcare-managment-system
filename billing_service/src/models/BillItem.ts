import BillItemType from "./BillItemType";

interface BillItem {
    type: BillItemType;
    price: Number;
    identifier: string;
}

export default BillItem;