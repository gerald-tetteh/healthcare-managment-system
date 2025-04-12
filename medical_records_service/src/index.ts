import Fastify from "fastify";
import app, { options } from "./app";

const fastify = Fastify(options);

fastify.register(app);

const init = async () => {
  await fastify.ready();
  await fastify.listen({
    host: "0.0.0.0",
    port: Number(process.env.port!),
  });
  fastify.log.info(`Started Medical Records Server`);
};

init().catch(err => {
  fastify.log.error(err);
});