package com.medical.medicine.service.impl;

import com.medical.medicine.dto.request.MedicineRequest;
import com.medical.medicine.dto.response.MedicineResponse;
import com.medical.medicine.entity.Medicine;
import com.medical.medicine.exception.DuplicateResourceException;
import com.medical.medicine.exception.ResourceNotFoundException;
import com.medical.medicine.mapper.MedicineMapper;
import com.medical.medicine.repository.MedicineRepository;
import com.medical.medicine.service.MedicineService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class MedicineServiceImpl implements MedicineService {

    private final MedicineRepository medicineRepository;
    private final MedicineMapper medicineMapper;

    /**
     * Lay chi tiet thuoc theo ID voi co che Cache-aside pattern:
     * - Lan 1: Redis chua co cache -> Spring Cache chay ham nay -> Log SQL truy van database -> Luu vao Redis.
     * - Lan 2: Redis da co cache -> Spring Cache tra ve thang tu Redis -> Ham nay KHONG chay -> Khong co log SQL.
     */
    @Override
    @Cacheable(value = "medicines", key = "#id")
    public MedicineResponse getMedicineById(Long id) {
        log.info("--> [CACHE MISS / DB QUERY] Dang truy van thong tin thuoc tu PostgreSQL Database voi ID = {}", id);

        Medicine medicine = medicineRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay thuoc voi ID: " + id));

        return medicineMapper.toResponse(medicine);
    }

    @Override
    public List<MedicineResponse> getAllMedicines() {
        return medicineRepository.findAll().stream()
                .map(medicineMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public MedicineResponse createMedicine(MedicineRequest request) {
        if (medicineRepository.existsByCode(request.code())) {
            throw new DuplicateResourceException("Ma thuoc da ton tai: " + request.code());
        }

        Medicine medicine = medicineMapper.toEntity(request);
        Medicine savedMedicine = medicineRepository.save(medicine);
        log.info("Tao moi thuoc thanh cong voi ID = {}", savedMedicine.getId());
        return medicineMapper.toResponse(savedMedicine);
    }

    @Override
    @Transactional
    @CachePut(value = "medicines", key = "#id")
    public MedicineResponse updateMedicine(Long id, MedicineRequest request) {
        Medicine medicine = medicineRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay thuoc voi ID: " + id));

        // Kiem tra neu doi ma code thanh ma da ton tai cua thuoc khac
        if (!medicine.getCode().equals(request.code()) && medicineRepository.existsByCode(request.code())) {
            throw new DuplicateResourceException("Ma thuoc da ton tai: " + request.code());
        }

        medicineMapper.updateEntityFromRequest(medicine, request);
        Medicine updatedMedicine = medicineRepository.save(medicine);
        log.info("Cap nhat thuoc va dong bo cache Redis voi ID = {}", id);
        return medicineMapper.toResponse(updatedMedicine);
    }

    @Override
    @Transactional
    @CacheEvict(value = "medicines", key = "#id")
    public void deleteMedicine(Long id) {
        if (!medicineRepository.existsById(id)) {
            throw new ResourceNotFoundException("Khong tim thay thuoc voi ID: " + id);
        }
        medicineRepository.deleteById(id);
        log.info("Xoa thuoc va xoa cache Redis voi ID = {}", id);
    }
}
