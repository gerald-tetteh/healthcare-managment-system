interface Appointment {
    appointmentId: Number,
    doctorId: Number,
    patientId: Number,
    dateTime: Date,
    notes: string,
    status: string
}

export default Appointment;