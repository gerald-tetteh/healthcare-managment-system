class LabTest {
  private testName: string;
  private result: string;
  private date: Date;

  constructor(
    testName: string,
    result: string,
    date: Date
  ) {
    this.testName = testName;
    this.result = result;
    this.date = date;
  }

  public getTestName() {
    return this.testName;
  }
  public getResult() {
    return this.result;
  }
  public getDate() {
    return this.date;
  }
}

export default LabTest;