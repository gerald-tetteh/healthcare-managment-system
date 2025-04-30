import fp from "fastify-plugin";

export default fp(async (fastify) => {
    // @ts-ignore
    fastify.decorate("jwt", {
        sign: () => "test token",
    });
});