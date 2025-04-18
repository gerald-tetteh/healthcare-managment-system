class Diagnosis {
  private icd10Code: string;
  private description: string;

  constructor(icd10Code: string, description: string) {
    this.icd10Code = icd10Code;
    this.description = description;
  }

  static fromJson(json: any): Diagnosis {
    return new Diagnosis(json.icd10Code, json.description);
  }

  public getIcd10Code() {
    return this.icd10Code;
  }
  public getDescription() {
    return this.description;
  }
}

export default Diagnosis;
