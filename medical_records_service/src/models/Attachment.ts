class Attachment {
  private fileName: string;
  private fileUrl: string;
  private mimeType: string;

  constructor(
    fileName: string,
    fileUrl: string,
    mimeType: string
  ) {
    this.fileName = fileName;
    this.fileUrl = fileUrl;
    this.mimeType = mimeType;
  }

  public getFileName() {
    return this.fileName;
  }
  public getFileUrl() {
    return this.fileUrl;
  }
  public getMimeType() {
    return this.mimeType;
  }
}

export default Attachment;