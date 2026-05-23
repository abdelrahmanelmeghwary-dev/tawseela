package com.tawseela.service;

import com.tawseela.dto.response.DriverResponse;
import com.tawseela.dto.request.UpdateDriverRequest;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface DriverService {

    Page<DriverResponse> list(Boolean online, Pageable pageable);

    DriverResponse getById(UUID id);

    DriverResponse create();

    DriverResponse update(UUID id, UpdateDriverRequest request);
}
