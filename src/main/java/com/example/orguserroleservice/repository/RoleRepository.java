package com.example.orguserroleservice.repository;

import com.example.orguserroleservice.entity.RoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RoleRepository extends JpaRepository<RoleEntity, Long> {
    Optional<RoleEntity> findByOrganization_IdAndName(Long organizationId, String name);
    boolean existsByOrganization_IdAndName(Long organizationId, String name);

    List<RoleEntity> findAllByOrganization_Id(Long organizationId);
}
