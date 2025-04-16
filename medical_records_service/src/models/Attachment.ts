import { ObjectId } from '@fastify/mongodb';

class Attachment {
  private fileName: string;
  private fileId: ObjectId;
  private mimeType: string;

  constructor(fileName: string, fileId: ObjectId, mimeType: string) {
    this.fileName = fileName;
    this.fileId = fileId;
    this.mimeType = mimeType;
  }

  public getFileName() {
    return this.fileName;
  }
  public getFileId() {
    return this.fileId;
  }
  public getMimeType() {
    return this.mimeType;
  }
}

export default Attachment;
