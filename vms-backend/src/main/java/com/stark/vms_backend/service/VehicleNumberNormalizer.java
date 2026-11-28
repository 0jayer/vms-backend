package com.stark.vms_backend.service;

/**
 * SRP: sole responsibility is normalizing vehicle registration numbers.
 * OCP: new normalization strategies can be plugged in without modifying VehicleServiceImpl.
 */
public interface VehicleNumberNormalizer {

    String normalize(String vehicleNumber);
}

