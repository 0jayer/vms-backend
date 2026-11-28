package com.stark.vms_backend.service;

import com.stark.vms_backend.dto.VehicleRequest;
import com.stark.vms_backend.dto.VehicleResponse;
import com.stark.vms_backend.entity.Vehicle;

/**
 * SRP: sole responsibility is mapping between Vehicle entities and DTOs.
 * OCP: mapping rules can be replaced or extended without modifying VehicleServiceImpl.
 */
public interface VehicleMapper {

    Vehicle toEntity(VehicleRequest request, String normalizedVehicleNumber);

    void updateEntity(Vehicle vehicle, VehicleRequest request, String normalizedVehicleNumber);

    VehicleResponse toResponse(Vehicle vehicle);
}

