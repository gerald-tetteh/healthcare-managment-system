import jwt from "@fastify/jwt";
import { FastifyReply, FastifyRequest } from "fastify";
import fp from "fastify-plugin";

declare module "@fastify/jwt" {
    interface FastifyJWT {
        user: {
            userId: Number;
            email: string;
            roles: string[];
        };
    }
}
declare module "fastify" {
    export interface FastifyInstance {
        authenticate(request: FastifyRequest, reply: FastifyReply): Promise<void>;
        authorizeByRole(
            roles: string[]
        ): (request: FastifyRequest, reply: FastifyReply) => Promise<void>;
    }
}

export default fp(async (fastify, options) => {
    fastify.register(jwt, {
        secret: Buffer.from(process.env.secret_key! || "", "base64"),
        sign: {
            algorithm: "HS384",
            expiresIn: "4h",
        }
    });

    fastify.decorate(
        "authenticate",
        async (request: FastifyRequest, reply: FastifyReply) => {
            try {
                await request.jwtVerify();
            } catch (err) {
                reply.status(401).send({
                    title: "Authentication Failed",
                    message: "Invalid or missing token",
                    statusCode: "UNAUTHORIZED",
                });
            }
        }
    );

    fastify.decorate("authorizeByRole", (roles: string[]) => {
        return async (request: FastifyRequest, reply: FastifyReply) => {
            const user = request.user;
            const hasRole = user && user.roles.some((role) => roles.includes(role));
            if (!hasRole) {
                reply.status(403).send({
                    title: "Authorization Failed",
                    message: "Cannot access this resource",
                    statusCode: "FORBIDDEN",
                });
            }
        };
    });
});
