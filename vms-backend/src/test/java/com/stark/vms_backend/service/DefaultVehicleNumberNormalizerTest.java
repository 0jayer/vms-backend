package com.stark.vms_backend.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class DefaultVehicleNumberNormalizerTest {

    private final DefaultVehicleNumberNormalizer normalizer = new DefaultVehicleNumberNormalizer();

    @Test
    void normalize_null_returnsNull() {
        assertNull(normalizer.normalize(null));
    }

    @Test
    void normalize_trimsWhitespaceAndUppercases() {
        assertEquals("ABC-123", normalizer.normalize("   abc-123   "));
    }

    @Test
    void normalize_alreadyNormalized_returnsSame() {
        assertEquals("XYZ-999", normalizer.normalize("XYZ-999"));
    }
}

