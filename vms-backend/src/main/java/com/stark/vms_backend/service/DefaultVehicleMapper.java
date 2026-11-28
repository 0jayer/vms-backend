package com.stark.vms_backend.service;

import com.stark.vms_backend.dto.VehicleRequest;
import com.stark.vms_backend.dto.VehicleResponse;
import com.stark.vms_backend.entity.Vehicle;
import org.springframework.stereotype.Component;

@Component
public class DefaultVehicleMapper implements VehicleMapper {

    @Override
    public Vehicle toEntity(VehicleRequest request, String normalizedVehicleNumber) {
        return new Vehicle(
                normalizedVehicleNumber,
                request.getCompany(),
                request.getType(),
                request.getBrand(),
                request.getModel(),
                request.getStatus()
        );
    }

    @Override
    public void updateEntity(Vehicle vehicle, VehicleRequest request, String normalizedVehicleNumber) {
        vehicle.setVehicleNumber(normalizedVehicleNumber);
        vehicle.setCompany(request.getCompany());
        vehicle.setType(request.getType());
        vehicle.setBrand(request.getBrand());
        vehicle.setModel(request.getModel());
        vehicle.setStatus(request.getStatus());
    }

    @Override
    public VehicleResponse toResponse(Vehicle vehicle) {
        return new VehicleResponse(
                vehicle.getId(),
                vehicle.getVehicleNumber(),
                vehicle.getCompany(),
                vehicle.getType(),
                vehicle.getBrand(),
                vehicle.getModel(),
                vehicle.getStatus()
        );
    }
}

