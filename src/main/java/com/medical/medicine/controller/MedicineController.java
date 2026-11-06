package com.medical.medicine.controller;

import com.medical.medicine.dto.request.MedicineRequest;
import com.medical.medicine.dto.response.ApiResponse;
import com.medical.medicine.dto.response.MedicineResponse;
import com.medical.medicine.service.MedicineService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/medicines")
@RequiredArgsConstructor
@Tag(name = "Medicine Management", description = "APIs quan ly va truy van danh muc thuoc voi Redis Caching")
public class MedicineController {

    private final MedicineService medicineService;

    /**
     * Endpoint lay chi tiet thuoc theo ID
     * Yeu cau bai tap:
     * - Lan 1: Query database -> Log hien SQL -> Cache vao Redis.
     * - Lan 2: Lay truc tiep tu Redis -> Khong co SQL log -> Toc do phan hoi sieu nhanh (<10ms).
     */
    @GetMapping("/{id}")
    @Operation(summary = "Lay chi tiet thuoc theo ID (co Caching Redis)")
    public ResponseEntity<ApiResponse<MedicineResponse>> getMedicineById(@PathVariable Long id) {
        MedicineResponse medicine = medicineService.getMedicineById(id);
        return ResponseEntity.ok(ApiResponse.success(medicine, "Lay thong tin thuoc thanh cong"));
    }

    @GetMapping
    @Operation(summary = "Lay danh sach tat ca cac thuoc trong danh muc")
    public ResponseEntity<ApiResponse<List<MedicineResponse>>> getAllMedicines() {
        List<MedicineResponse> medicines = medicineService.getAllMedicines();
        return ResponseEntity.ok(ApiResponse.success(medicines, "Lay danh sach thuoc thanh cong"));
    }

    @PostMapping
    @Operation(summary = "Them moi mot loai thuoc vao danh muc")
    public ResponseEntity<ApiResponse<MedicineResponse>> createMedicine(
            @Valid @RequestBody MedicineRequest request) {
        MedicineResponse created = medicineService.createMedicine(request);
        return new ResponseEntity<>(
                ApiResponse.success(created, "Tao moi thuoc thanh cong"),
                HttpStatus.CREATED
        );
    }

    @PutMapping("/{id}")
    @Operation(summary = "Cap nhat thong tin thuoc (dong bo lai cache Redis)")
    public ResponseEntity<ApiResponse<MedicineResponse>> updateMedicine(
            @PathVariable Long id,
            @Valid @RequestBody MedicineRequest request) {
        MedicineResponse updated = medicineService.updateMedicine(id, request);
        return ResponseEntity.ok(ApiResponse.success(updated, "Cap nhat thuoc thanh cong"));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Xoa thuoc theo ID (xoa cache Redis tuong ung)")
    public ResponseEntity<ApiResponse<Void>> deleteMedicine(@PathVariable Long id) {
        medicineService.deleteMedicine(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Xoa thuoc thanh cong"));
    }
}
