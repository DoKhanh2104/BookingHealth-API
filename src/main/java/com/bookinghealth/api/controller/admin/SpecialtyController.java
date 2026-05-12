package com.bookinghealth.api.controller.admin;

import com.bookinghealth.api.dto.request.admin.SpecialtyRequest;
import com.bookinghealth.api.dto.response.ApiResponse;
import com.bookinghealth.api.dto.response.admin.SpecialtyResponse;
import com.bookinghealth.api.service.SpecialtyService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/specialties")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SpecialtyController {

    SpecialtyService specialtyService;

    @PostMapping
    public ApiResponse<SpecialtyResponse> create(@RequestBody @Valid SpecialtyRequest request) {
        return ApiResponse.<SpecialtyResponse>builder()
                .result(specialtyService.createSpecialty(request))
                .build();
    }

    @GetMapping
    public ApiResponse<Page<SpecialtyResponse>> getAll(@PageableDefault(size = 10) Pageable pageable) {
        return ApiResponse.<Page<SpecialtyResponse>>builder()
                .result(specialtyService.getAllSpecialties(pageable))
                .build();
    }

    @PutMapping("/{id}")
    public ApiResponse<SpecialtyResponse> update(@PathVariable Long id, @RequestBody SpecialtyRequest request) {
        return ApiResponse.<SpecialtyResponse>builder()
                .result(specialtyService.updateSpecialty(id, request))
                .build();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<String> delete(@PathVariable Long id) {
        specialtyService.deleteSpecialty(id);
        return ApiResponse.<String>builder().result("Đã xóa chuyên khoa").build();
    }
}
