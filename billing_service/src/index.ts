import Fastify from "fastify";
import app, { options } from "./app";

const fastify = Fastify(options);

fastify.register(app);

const init = async () => {
    await fastify.ready();
    await fastify.listen({
        host: "0.0.0.0",
        port: Number(process.env.port || "3000"),
    });
    fastify.log.info(`Started Billing and Insurance Service`);
};

init().catch(err => {
    fastify.log.error(err);
});