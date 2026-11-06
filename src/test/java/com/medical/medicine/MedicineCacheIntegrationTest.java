package com.medical.medicine;

import com.medical.medicine.dto.response.MedicineResponse;
import com.medical.medicine.entity.Medicine;
import com.medical.medicine.repository.MedicineRepository;
import com.medical.medicine.service.MedicineService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.cache.CacheManager;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@SpringBootTest
class MedicineCacheIntegrationTest {

    @Autowired
    private MedicineService medicineService;

    @SpyBean
    private MedicineRepository medicineRepository;

    @Autowired
    private CacheManager cacheManager;

    @Test
    @DisplayName("Kiem chung Cache-aside: Lan 1 query DB, Lan 2 lay truc tiep tu Redis Cache")
    void testCacheAsidePattern() {
        // Clear cache "medicines" truoc khi test
        if (cacheManager.getCache("medicines") != null) {
            cacheManager.getCache("medicines").clear();
        }

        // Lan 1: Query lan dau -> Goi xuong Database (findById duoc goi 1 lan)
        MedicineResponse firstCall = medicineService.getMedicineById(1L);
        assertThat(firstCall).isNotNull();
        assertThat(firstCall.id()).isEqualTo(1L);
        verify(medicineRepository, times(1)).findById(1L);

        // Kiem tra gia tri da ton tai trong Cache Manager
        assertThat(cacheManager.getCache("medicines")).isNotNull();
        assertThat(cacheManager.getCache("medicines").get(1L)).isNotNull();

        // Lan 2: Query lan tiep theo voi cung ID -> Lay tu Redis Cache -> Repository findById khong bi goi them lan nao
        MedicineResponse secondCall = medicineService.getMedicineById(1L);
        assertThat(secondCall).isNotNull();
        assertThat(secondCall.name()).isEqualTo(firstCall.name());

        // Tong so lan goi findById van chi la 1 lan duy nhat!
        verify(medicineRepository, times(1)).findById(1L);
    }
}
