import { ObjectId } from '@fastify/mongodb';
import Attachment from './Attachment';
import Diagnosis from './Diagnosis';
import LabTest from './LabTest';
import Prescription from './Prescription';
import VisitType from './VisitType';
import MongoDocument from './MongoDocument';

class MedicalRecord implements MongoDocument {
  _id?: ObjectId;
  patientId: Number;
  doctorId: Number;
  visitType: VisitType;
  symptoms: string[];
  diagnosis: Diagnosis;
  prescriptions: Prescription[];
  labTests: LabTest[];
  notes: string;
  attachments: Attachment[];
  createdAt: Date;
  updatedAt: Date;

  constructor(
    _id: string,
    patientId: Number,
    doctorId: Number,
    visitType: VisitType,
    symptoms: string[] = [],
    diagnosis: Diagnosis,
    prescriptions: Prescription[],
    labTests: LabTest[] = [],
    notes: string,
    attachments: Attachment[] = [],
    createdAt: Date = new Date(),
    updatedAt: Date = new Date()
  ) {
    this._id = _id ? new ObjectId(_id) : undefined;
    this.patientId = patientId;
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

  static fromJson(json: any): MedicalRecord {
    return new MedicalRecord(
      json._id,
      json.patientId,
      json.doctorId,
      VisitType[json.visitType as keyof typeof VisitType],
      json.symptoms,
      Diagnosis.fromJson(json.diagnosis),
      (json.prescriptions as []).map((prescription: any) => Prescription.fromJson(prescription)),
      (json.labTests as []).map((labTest: any) => LabTest.fromJson(labTest)),
      json.notes,
      (json.attachments as []).map((attachment: any) => Attachment.fromJson(attachment)),
      json.createdAt ? new Date(json.createdAt) : new Date(),
      json.updatedAt ? new Date(json.updatedAt) : new Date()
    );
  }
}

export default MedicalRecord;
