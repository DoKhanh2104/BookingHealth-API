package com.bookinghealth.api.service;

import com.bookinghealth.api.dto.response.admin.AppointmentResponse;
import com.bookinghealth.api.entity.AppointmentSlot;
import com.bookinghealth.api.entity.Specialty;
import com.bookinghealth.api.repository.AppointmentRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AppointmentService {

    AppointmentRepository appointmentRepository;

    public Page<AppointmentResponse> getAppointments(Pageable pageable) {
        return appointmentRepository.findAll(pageable).map(appointment -> {

            String patientName = appointment.getUser() != null ? appointment.getUser().getName() : "";
            String patientPhone = appointment.getUser() != null ? appointment.getUser().getPhone() : "";


            String doctorName = "";
            String specialty = "";
            if (appointment.getDoctor() != null) {
                if (appointment.getDoctor().getUser() != null) {
                    doctorName = appointment.getDoctor().getUser().getName();
                }
                if (appointment.getDoctor().getSpecialties() != null) {
                    specialty = appointment.getDoctor().getSpecialties().stream()
                            .map(Specialty::getSpecialtyName)
                            .collect(Collectors.joining(", "));
                }
            }

            String appDate = appointment.getExpectedExaminationDate() != null
                    ? appointment.getExpectedExaminationDate().toString()
                    : "";

            String timeSlot = "";
            if (appointment.getAppointmentSlot() != null) {
                AppointmentSlot slot = appointment.getAppointmentSlot();
                timeSlot = slot.getStartTime() + " - " + slot.getEndTime();
            }

            String statusStr = mapStatusToString(appointment.getStatus());
            return AppointmentResponse.builder()
                    .patientName(patientName)
                    .patientPhone(patientPhone)
                    .doctorName(doctorName)
                    .specialty(specialty)
                    .appointmentDate(appDate)
                    .appointmentFee(appointment.getTotalAmount())
                    .status(statusStr)
                    .timeSlot(timeSlot)
                    .build();
        });
    }
    private String mapStatusToString(Integer status) {
        if (status == null) return "PENDING";
        switch (status) {
            case 0: return "PENDING";
            case 1: return "CONFIRMED";
            case 2: return "COMPLETED";
            case 3: return "CANCELLED";
            default: return "PENDING";
        }
    }
}
