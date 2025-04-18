class Prescription {
  private medicationName: string;
  private dosage: string;
  private frequency: string;
  private duration: Number;

  constructor(medicationName: string, dosage: string, frequency: string, duration: Number) {
    this.medicationName = medicationName;
    this.dosage = dosage;
    this.frequency = frequency;
    this.duration = duration;
  }

  static fromJson(json: any): Prescription {
    return new Prescription(json.medicationName, json.dosage, json.frequency, json.duration);
  }

  public getMedicationName() {
    return this.medicationName;
  }
  public getDosage() {
    return this.dosage;
  }
  public getFrequency() {
    return this.frequency;
  }
  public getDuration() {
    return this.duration;
  }
}

export default Prescription;
