package com.medical.medicine.service;

import com.medical.medicine.dto.request.MedicineRequest;
import com.medical.medicine.dto.response.MedicineResponse;

import java.util.List;

public interface MedicineService {

    /**
     * Lay chi tiet thuoc theo ID - Duoc ap dung Redis Caching theo Cache-aside pattern
     *
     * @param id ID cua thuoc can tim
     * @return Thong tin chi tiet thuoc (MedicineResponse)
     */
    MedicineResponse getMedicineById(Long id);

    /**
     * Lay danh sach tat ca cac loai thuoc trong danh muc
     *
     * @return Danh sach MedicineResponse
     */
    List<MedicineResponse> getAllMedicines();

    /**
     * Tao moi thuoc vao he thong
     *
     * @param request Du lieu tao thuoc
     * @return Thong tin thuoc da duoc tao
     */
    MedicineResponse createMedicine(MedicineRequest request);

    /**
     * Cap nhat thong tin thuoc va dong bo/xoa cache cu
     *
     * @param id ID thuoc can cap nhat
     * @param request Du lieu cap nhat
     * @return Thong tin thuoc sau cap nhat
     */
    MedicineResponse updateMedicine(Long id, MedicineRequest request);

    /**
     * Xoa thuoc theo ID va dong thoi xoa cache trong Redis
     *
     * @param id ID thuoc can xoa
     */
    void deleteMedicine(Long id);
}
