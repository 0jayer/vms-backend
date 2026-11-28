package com.stark.vms_backend.service;

import com.stark.vms_backend.dto.VehicleRequest;
import com.stark.vms_backend.dto.VehicleResponse;
import com.stark.vms_backend.entity.Vehicle;
import com.stark.vms_backend.entity.VehicleStatus;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class DefaultVehicleMapperTest {

    private final DefaultVehicleMapper mapper = new DefaultVehicleMapper();

    @Test
    void toEntity_mapsRequestAndNormalizedNumber() {
        VehicleRequest request = new VehicleRequest("abc-123", "Acme", "Truck", "Ford", "F-150", VehicleStatus.ACTIVE);

        Vehicle entity = mapper.toEntity(request, "ABC-123");

        assertNull(entity.getId());
        assertEquals("ABC-123", entity.getVehicleNumber());
        assertEquals("Acme", entity.getCompany());
        assertEquals("Truck", entity.getType());
        assertEquals("Ford", entity.getBrand());
        assertEquals("F-150", entity.getModel());
        assertEquals(VehicleStatus.ACTIVE, entity.getStatus());
    }

    @Test
    void updateEntity_updatesAllMutableFields() {
        Vehicle existing = new Vehicle(1L, "ABC-123", "Acme", "Truck", "Ford", "F-150", VehicleStatus.ACTIVE);
        VehicleRequest request = new VehicleRequest("xyz-999", "Stark", "Van", "Mercedes", "Sprinter", VehicleStatus.MAINTENANCE);

        mapper.updateEntity(existing, request, "XYZ-999");

        assertEquals(1L, existing.getId());
        assertEquals("XYZ-999", existing.getVehicleNumber());
        assertEquals("Stark", existing.getCompany());
        assertEquals("Van", existing.getType());
        assertEquals("Mercedes", existing.getBrand());
        assertEquals("Sprinter", existing.getModel());
        assertEquals(VehicleStatus.MAINTENANCE, existing.getStatus());
    }

    @Test
    void toResponse_mapsEntityFields() {
        Vehicle entity = new Vehicle(42L, "ABC-123", "Acme", "Truck", "Ford", "F-150", VehicleStatus.INACTIVE);

        VehicleResponse response = mapper.toResponse(entity);

        assertEquals(42L, response.getId());
        assertEquals("ABC-123", response.getVehicleNumber());
        assertEquals("Acme", response.getCompany());
        assertEquals("Truck", response.getType());
        assertEquals("Ford", response.getBrand());
        assertEquals("F-150", response.getModel());
        assertEquals(VehicleStatus.INACTIVE, response.getStatus());
    }
}

