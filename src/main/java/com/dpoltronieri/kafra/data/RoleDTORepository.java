package com.dpoltronieri.kafra.data;

import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleDTORepository extends JpaRepository<RoleDTO, Long> {

    @SuppressWarnings("null")
    Optional<RoleDTO> findById(@NotNull Long id);

    Optional<RoleDTO> findByRoleId(Long roleId);

    Optional<RoleDTO> findByRoleIdAndGuild(Long roleId, GuildDTO guild);
}