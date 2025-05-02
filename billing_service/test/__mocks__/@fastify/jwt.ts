import fp from "fastify-plugin";
import { jest } from "@jest/globals";
import {UserType, VerifyPayloadType} from "@fastify/jwt";

type jwtSign = () => string;
type jwtVerifyType = () => Promise<VerifyPayloadType>;
type mockUserType = () => UserType;

const mockSign = jest.fn<jwtSign>().mockImplementation(() => "test token");
const mockJwtVerify = jest.fn<jwtVerifyType>().mockResolvedValue(() => "");
const mockRequestUser = jest.fn<mockUserType>().mockReturnValue({
    userId: 1,
    email: "test@test.com",
    roles: ["ROLE_ADMIN"],
});
const setInvalidUser = () => {
    mockRequestUser.mockReturnValueOnce({
        userId: 1,
        email: "test@test.com",
        roles: ["ROLE_PATIENT"],
    });
}

export default fp(async (fastify) => {
    // @ts-ignore
    fastify.decorate("jwt", {
        sign: mockSign,
    });
    fastify.decorateRequest("jwtVerify", async function () {
        this.user = mockRequestUser();
        return mockJwtVerify();
    });
});

export { mockSign, mockJwtVerify, setInvalidUser };