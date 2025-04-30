import {jest} from "@jest/globals";

const mockSend = jest.fn().mockResolvedValue(undefined);
const mockConnect = jest.fn().mockResolvedValue(undefined);
const mockDisconnect = jest.fn().mockResolvedValue(undefined);

const mockProducerInstance = {
    connect: mockConnect,
    disconnect: mockDisconnect,
    send: mockSend,
};

const mockConstructor = jest.fn().mockImplementation(() => ({
    producer: jest.fn(() => mockProducerInstance),
    consumer: jest.fn(() => ({
        connect: jest.fn(),
        run: jest.fn(),
        subscribe: jest.fn(),
        disconnect: jest.fn(),
    })),
}));

export { mockSend, mockConnect, mockDisconnect };
export const Kafka = mockConstructor;