import { join } from 'node:path'
import AutoLoad, { AutoloadPluginOptions } from '@fastify/autoload'
import { FastifyPluginAsync, FastifyServerOptions } from 'fastify'
import ServerException from "./models/ServerException";

export interface AppOptions extends FastifyServerOptions, Partial<AutoloadPluginOptions> {

}
// Pass --options via CLI arguments in command to enable these options.
const options: AppOptions = {
  logger: true,
  ajv: {
    customOptions: {
      strict: true,
      removeAdditional: false
    }
  }
}

const app: FastifyPluginAsync<AppOptions> = async (
  fastify,
  opts
): Promise<void> => {
  // Place here your custom code!
  fastify.setErrorHandler((error, request, reply) => {
    if (error instanceof ServerException) {
      reply.status(500).send({
        title: 'Internal Server Error',
        message: error.message,
        statusCode: 500
      });
      return;
    }
    fastify.log.error(error, 'Server exception occurred');
    reply.status(500).send({
      title: 'Internal Server Error',
      message: 'An unexpected error occurred',
      statusCode: 'INTERNAL_SERVER_ERROR'
    });
  });

  // Do not touch the following lines

  // This loads all plugins defined in plugins
  // those should be support plugins that are reused
  // through your application
  // eslint-disable-next-line no-void
  void fastify.register(AutoLoad, {
    dir: join(__dirname, 'plugins'),
    options: opts
  })

  // This loads all plugins defined in routes
  // define your routes in one of these
  // eslint-disable-next-line no-void
  void fastify.register(AutoLoad, {
    dir: join(__dirname, 'routes'),
    options: opts
  })
}

export default app
export { app, options }
