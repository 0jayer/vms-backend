package com.stark.vms_backend.service;

import org.springframework.stereotype.Component;

@Component
public class DefaultVehicleNumberNormalizer implements VehicleNumberNormalizer {

    @Override
    public String normalize(String vehicleNumber) {
        return vehicleNumber != null ? vehicleNumber.trim().toUpperCase() : null;
    }
}

