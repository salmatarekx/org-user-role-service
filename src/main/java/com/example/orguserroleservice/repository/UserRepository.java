package com.example.orguserroleservice.repository;

import com.example.orguserroleservice.entity.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, Long> {

    @Query("""
        select u from UserEntity u
        left join fetch u.roles r
        left join fetch u.organization o
        where lower(u.email) = lower(:email)
    """)
    Optional<UserEntity> findByEmailWithAuth(@Param("email") String email);

    @Query("""
        select u from UserEntity u
        left join fetch u.roles
        left join fetch u.organization
        where u.id = :id
    """)
    Optional<UserEntity> findByIdWithAuth(@Param("id") Long id);

    // ✅ ONE pagination query, but fetch roles + org using EntityGraph
    @EntityGraph(attributePaths = {"roles", "organization"})
    @Query("""
        select u from UserEntity u
        where u.organization.id = :orgId
    """)
    Page<UserEntity> findAllByOrganizationId(@Param("orgId") Long orgId, Pageable pageable);

    boolean existsByEmailIgnoreCase(String email);

    @Query("""
        select count(u) > 0 from UserEntity u
        join u.roles r
        where r.id = :roleId
    """)
    boolean existsAnyUserWithRoleId(@Param("roleId") Long roleId);
}
