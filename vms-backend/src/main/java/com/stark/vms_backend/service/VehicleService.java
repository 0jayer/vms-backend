package com.stark.vms_backend.service;

import com.stark.vms_backend.dto.VehicleRequest;
import com.stark.vms_backend.dto.VehicleResponse;

import java.util.List;

/**
 * OCP/DIP: controllers and other callers depend on this abstraction.
 * New behaviours (caching, auditing, etc.) can be added via decoration or
 * alternative implementations without modifying existing code.
 */
public interface VehicleService {

    VehicleResponse create(VehicleRequest request);

    List<VehicleResponse> list();

    VehicleResponse getById(Long id);

    VehicleResponse update(Long id, VehicleRequest request);

    void delete(Long id);
}
