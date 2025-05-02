import fp from "fastify-plugin";
import { jest } from "@jest/globals";
import {VerifyPayloadType} from "@fastify/jwt";

type jwtSign = () => string;
type jwtVerifyType = () => Promise<VerifyPayloadType>;

const mockSign = jest.fn<jwtSign>().mockImplementation(() => "test token");
const mockJwtVerify = jest.fn<jwtVerifyType>().mockResolvedValue(() => "");

export default fp(async (fastify) => {
    // @ts-ignore
    fastify.decorate("jwt", {
        sign: mockSign,
    });
    fastify.decorateRequest("jwtVerify", async function () {
        this.user = {
            userId: 1,
            email: "test@test.com",
            roles: ["ROLE_ADMIN"],
        };
        return mockJwtVerify();
    });
});

export { mockSign, mockJwtVerify };