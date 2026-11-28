package com.stark.vms_backend.service;

import com.stark.vms_backend.dto.VehicleRequest;
import com.stark.vms_backend.dto.VehicleResponse;
import com.stark.vms_backend.entity.Vehicle;
import com.stark.vms_backend.entity.VehicleStatus;
import com.stark.vms_backend.exception.DuplicateVehicleNumberException;
import com.stark.vms_backend.exception.VehicleNotFoundException;
import com.stark.vms_backend.repository.VehicleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VehicleServiceImplTest {

    @Mock
    private VehicleRepository vehicleRepository;

    private VehicleServiceImpl vehicleService;

    @BeforeEach
    void setUp() {
        vehicleService = new VehicleServiceImpl(
                vehicleRepository,
                new DefaultVehicleNumberNormalizer(),
                new DefaultVehicleMapper()
        );
    }

    @Test
    void create_newVehicle_succeeds() {
        VehicleRequest request = new VehicleRequest("abc-123", "Acme", "Truck", "Ford", "F-150", VehicleStatus.ACTIVE);
        when(vehicleRepository.existsByVehicleNumber("ABC-123")).thenReturn(false);
        when(vehicleRepository.save(any(Vehicle.class))).thenAnswer(invocation -> {
            Vehicle v = invocation.getArgument(0);
            v.setId(1L);
            return v;
        });

        VehicleResponse response = vehicleService.create(request);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("ABC-123", response.getVehicleNumber());
        assertEquals("Acme", response.getCompany());
        assertEquals(VehicleStatus.ACTIVE, response.getStatus());

        ArgumentCaptor<Vehicle> captor = ArgumentCaptor.forClass(Vehicle.class);
        verify(vehicleRepository).save(captor.capture());
        assertEquals("ABC-123", captor.getValue().getVehicleNumber());
    }

    @Test
    void create_duplicateVehicleNumber_throwsDuplicateVehicleNumberExceptionAndDoesNotSave() {
        VehicleRequest request = new VehicleRequest("ABC-123", "Acme", "Truck", "Ford", "F-150", VehicleStatus.ACTIVE);
        when(vehicleRepository.existsByVehicleNumber("ABC-123")).thenReturn(true);

        assertThrows(DuplicateVehicleNumberException.class, () -> vehicleService.create(request));
        verify(vehicleRepository, never()).save(any(Vehicle.class));
    }

    @Test
    void create_duplicateCheckIgnoresCaseAndWhitespace() {
        VehicleRequest request = new VehicleRequest("   abc-123   ", "Acme", "Truck", "Ford", "F-150", VehicleStatus.ACTIVE);
        when(vehicleRepository.existsByVehicleNumber("ABC-123")).thenReturn(true);

        assertThrows(DuplicateVehicleNumberException.class, () -> vehicleService.create(request));
        verify(vehicleRepository).existsByVehicleNumber("ABC-123");
        verify(vehicleRepository, never()).save(any(Vehicle.class));
    }

    @Test
    void update_toAnotherVehiclesNumber_rejected() {
        Long vehicleId = 1L;
        Vehicle existingVehicle = new Vehicle(vehicleId, "ABC-123", "Acme", "Truck", "Ford", "F-150", VehicleStatus.ACTIVE);
        VehicleRequest updateRequest = new VehicleRequest("XYZ-999", "Acme", "Truck", "Ford", "F-150", VehicleStatus.MAINTENANCE);

        when(vehicleRepository.findById(vehicleId)).thenReturn(Optional.of(existingVehicle));
        when(vehicleRepository.existsByVehicleNumberAndIdNot("XYZ-999", vehicleId)).thenReturn(true);

        assertThrows(DuplicateVehicleNumberException.class, () -> vehicleService.update(vehicleId, updateRequest));
        verify(vehicleRepository, never()).save(any(Vehicle.class));
    }

    @Test
    void update_keepingOwnNumber_allowed() {
        Long vehicleId = 1L;
        Vehicle existingVehicle = new Vehicle(vehicleId, "ABC-123", "Acme", "Truck", "Ford", "F-150", VehicleStatus.ACTIVE);
        VehicleRequest updateRequest = new VehicleRequest("   abc-123   ", "Acme Updated", "Truck", "Ford", "F-250", VehicleStatus.MAINTENANCE);

        when(vehicleRepository.findById(vehicleId)).thenReturn(Optional.of(existingVehicle));
        when(vehicleRepository.existsByVehicleNumberAndIdNot("ABC-123", vehicleId)).thenReturn(false);
        when(vehicleRepository.save(any(Vehicle.class))).thenAnswer(invocation -> invocation.getArgument(0));

        VehicleResponse response = vehicleService.update(vehicleId, updateRequest);

        assertNotNull(response);
        assertEquals("ABC-123", response.getVehicleNumber());
        assertEquals("Acme Updated", response.getCompany());
        assertEquals("F-250", response.getModel());
        assertEquals(VehicleStatus.MAINTENANCE, response.getStatus());
        verify(vehicleRepository).save(existingVehicle);
    }

    @Test
    void getById_existingId_returnsVehicle() {
        Vehicle vehicle = new Vehicle(1L, "ABC-123", "Acme", "Truck", "Ford", "F-150", VehicleStatus.ACTIVE);
        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(vehicle));

        VehicleResponse response = vehicleService.getById(1L);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("ABC-123", response.getVehicleNumber());
    }

    @Test
    void getById_nonExistingId_throwsVehicleNotFoundException() {
        when(vehicleRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(VehicleNotFoundException.class, () -> vehicleService.getById(99L));
    }

    @Test
    void list_returnsAllVehicles() {
        List<Vehicle> vehicles = List.of(
                new Vehicle(1L, "ABC-123", "Acme", "Truck", "Ford", "F-150", VehicleStatus.ACTIVE),
                new Vehicle(2L, "XYZ-999", "Beta", "Van", "Mercedes", "Sprinter", VehicleStatus.INACTIVE)
        );
        when(vehicleRepository.findAll()).thenReturn(vehicles);

        List<VehicleResponse> responses = vehicleService.list();

        assertEquals(2, responses.size());
        assertEquals("ABC-123", responses.get(0).getVehicleNumber());
        assertEquals("XYZ-999", responses.get(1).getVehicleNumber());
    }

    @Test
    void delete_existingId_deletesVehicle() {
        when(vehicleRepository.existsById(1L)).thenReturn(true);

        vehicleService.delete(1L);

        verify(vehicleRepository).deleteById(1L);
    }

    @Test
    void delete_nonExistingId_throwsVehicleNotFoundException() {
        when(vehicleRepository.existsById(99L)).thenReturn(false);

        assertThrows(VehicleNotFoundException.class, () -> vehicleService.delete(99L));
        verify(vehicleRepository, never()).deleteById(anyLong());
    }
}
