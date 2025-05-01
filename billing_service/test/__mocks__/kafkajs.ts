import {jest} from "@jest/globals";

const mockSend = jest.fn().mockResolvedValue(undefined);
const mockConnect = jest.fn().mockResolvedValue(undefined);
const mockDisconnect = jest.fn().mockResolvedValue(undefined);
const mockCommitOffsets = jest.fn<() => Promise<void>>().mockResolvedValue();
const mockSubscribe = jest.fn<() => Promise<void>>().mockResolvedValue();
const mockRun = jest.fn<() => Promise<void>>().mockResolvedValue();

const mockProducerInstance = {
    connect: mockConnect,
    disconnect: mockDisconnect,
    send: mockSend,
};

const mockConstructor = jest.fn().mockImplementation(() => ({
    producer: jest.fn(() => mockProducerInstance),
    consumer: jest.fn(() => ({
        connect: mockConnect,
        run: mockRun,
        subscribe: mockSubscribe,
        disconnect: mockDisconnect,
        commitOffsets: mockCommitOffsets,
    })),
}));

export { mockSend, mockConnect, mockDisconnect, mockCommitOffsets, mockSubscribe, mockRun };
export const Kafka = mockConstructor;