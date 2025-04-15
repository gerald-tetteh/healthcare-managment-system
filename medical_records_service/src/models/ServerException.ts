class ServerException extends Error {
  constructor(message: string, statusCode: Number) {
    super(message);
  }
}

export default ServerException;
