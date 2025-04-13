import Attachment from "./Attachment";
import Diagnosis from "./Diagnosis";
import LabTest from "./LabTest";
import Prescription from "./Prescription";
import VisitType from "./VisitType";

class MedicalRecord {
  private recordId: Number;
  private userId: Number;
  private doctorId: Number;
  private visitType: VisitType;
  private symptoms: string[];
  private diagnosis: Diagnosis;
  private prescriptions: Prescription[];
  private labTests: LabTest[];
  private notes: string;
  private attachments: Attachment[];
  private createdAt: Date;
  private updatedAt: Date;

  constructor(
    recordId: Number,
    userId: Number,
    doctorId: Number,
    visitType: VisitType,
    symptoms: string[],
    diagnosis: Diagnosis,
    prescriptions: Prescription[],
    labTests: LabTest[],
    notes: string,
    attachments: Attachment[],
    createdAt: Date = new Date(),
    updatedAt: Date = new Date()
  ) {
    this.recordId = recordId;
    this.userId = userId;
    this.doctorId = doctorId;
    this.visitType = visitType;
    this.symptoms = symptoms;
    this.diagnosis = diagnosis;
    this.prescriptions = prescriptions;
    this.labTests = labTests;
    this.notes = notes;
    this.attachments = attachments;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
  }

  public getRecordId(): Number {
    return this.recordId;
  }

  public getUserId(): Number {
    return this.userId;
  }

  public getDoctorId(): Number {
    return this.doctorId;
  }

  public getVisitType(): VisitType {
    return this.visitType;
  }

  public getSymptoms(): string[] {
    return this.symptoms;
  }

  public getDiagnosis(): Diagnosis {
    return this.diagnosis;
  }

  public getPrescriptions(): Prescription[] {
    return this.prescriptions;
  }

  public getLabTests(): LabTest[] {
    return this.labTests;
  }

  public getNotes(): string {
    return this.notes;
  }

  public getAttachments(): Attachment[] {
    return this.attachments;
  }

  public getCreatedAt(): Date {
    return this.createdAt;
  }

  public getUpdatedAt(): Date {
    return this.updatedAt;
  }
}

export default MedicalRecord;