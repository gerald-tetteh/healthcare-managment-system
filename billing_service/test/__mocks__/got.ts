import { jest } from "@jest/globals";
import { OptionsOfTextResponseBody } from "got";

type gotJsonType = () => Promise<{ consultationFee: Number }>

const mockGotJson = jest.fn<gotJsonType>().mockResolvedValue({ consultationFee: 30 });

const got = jest.fn((options: OptionsOfTextResponseBody) => {
    return {
        json: mockGotJson,
    };
});

export default got;
export { mockGotJson };