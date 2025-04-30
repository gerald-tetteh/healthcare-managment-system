import { jest } from "@jest/globals";
import { OptionsOfTextResponseBody } from "got";

const got = jest.fn((options: OptionsOfTextResponseBody) => {
    return {
        json: () => Promise.resolve({
            consultationFee: Number,
        }),
    };
});

export default got;