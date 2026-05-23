package com.tawseela.repository;

import com.tawseela.entity.Driver;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface DriverRepository extends JpaRepository<Driver, UUID>, JpaSpecificationExecutor<Driver> {}
