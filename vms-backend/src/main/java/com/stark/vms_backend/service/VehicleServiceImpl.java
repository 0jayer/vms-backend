package com.stark.vms_backend.service;

import com.stark.vms_backend.dto.VehicleRequest;
import com.stark.vms_backend.dto.VehicleResponse;
import com.stark.vms_backend.entity.Vehicle;
import com.stark.vms_backend.exception.DuplicateVehicleNumberException;
import com.stark.vms_backend.exception.VehicleNotFoundException;
import com.stark.vms_backend.repository.VehicleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * SRP: sole responsibility is CRUD orchestration and business-rule enforcement
 *      (uniqueness checks and not-found guards). Normalization and DTO mapping
 *      are delegated to VehicleNumberNormalizer and VehicleMapper.
 * DIP/OCP: depends on abstractions rather than concrete implementations.
 */
@Service
@Transactional
public class VehicleServiceImpl implements VehicleService {

    private final VehicleRepository vehicleRepository;
    private final VehicleNumberNormalizer vehicleNumberNormalizer;
    private final VehicleMapper vehicleMapper;

    public VehicleServiceImpl(VehicleRepository vehicleRepository,
                              VehicleNumberNormalizer vehicleNumberNormalizer,
                              VehicleMapper vehicleMapper) {
        this.vehicleRepository = vehicleRepository;
        this.vehicleNumberNormalizer = vehicleNumberNormalizer;
        this.vehicleMapper = vehicleMapper;
    }

    @Override
    public VehicleResponse create(VehicleRequest request) {
        String normalizedNumber = vehicleNumberNormalizer.normalize(request.getVehicleNumber());
        if (vehicleRepository.existsByVehicleNumber(normalizedNumber)) {
            throw new DuplicateVehicleNumberException(normalizedNumber);
        }

        Vehicle vehicle = vehicleMapper.toEntity(request, normalizedNumber);
        Vehicle saved = vehicleRepository.save(vehicle);
        return vehicleMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<VehicleResponse> list() {
        return vehicleRepository.findAll().stream()
                .map(vehicleMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public VehicleResponse getById(Long id) {
        return vehicleRepository.findById(id)
                .map(vehicleMapper::toResponse)
                .orElseThrow(() -> new VehicleNotFoundException(id));
    }

    @Override
    public VehicleResponse update(Long id, VehicleRequest request) {
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new VehicleNotFoundException(id));

        String normalizedNumber = vehicleNumberNormalizer.normalize(request.getVehicleNumber());
        if (vehicleRepository.existsByVehicleNumberAndIdNot(normalizedNumber, id)) {
            throw new DuplicateVehicleNumberException(normalizedNumber);
        }

        vehicleMapper.updateEntity(vehicle, request, normalizedNumber);
        Vehicle saved = vehicleRepository.save(vehicle);
        return vehicleMapper.toResponse(saved);
    }

    @Override
    public void delete(Long id) {
        if (!vehicleRepository.existsById(id)) {
            throw new VehicleNotFoundException(id);
        }
        vehicleRepository.deleteById(id);
    }
}
