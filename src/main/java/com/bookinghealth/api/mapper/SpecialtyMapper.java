package com.bookinghealth.api.mapper;

import com.bookinghealth.api.dto.request.admin.SpecialtyRequest;
import com.bookinghealth.api.dto.response.admin.SpecialtyResponse;
import com.bookinghealth.api.entity.Specialty;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface SpecialtyMapper {
    Specialty toSpecialty(SpecialtyRequest request);
    SpecialtyResponse toSpecialtyResponse(Specialty specialty);
    void updateSpecialtyFromRequest(@MappingTarget Specialty specialty, SpecialtyRequest request);
}
