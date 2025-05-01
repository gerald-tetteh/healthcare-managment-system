import fp from "fastify-plugin";
import { jest } from "@jest/globals";

type jwtSign = () => string;

const mockSign = jest.fn<jwtSign>().mockImplementation(() => "test token");

export default fp(async (fastify) => {
    // @ts-ignore
    fastify.decorate("jwt", {
        sign: mockSign,
    });
});

export { mockSign };