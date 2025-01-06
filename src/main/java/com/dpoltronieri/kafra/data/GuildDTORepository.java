package com.dpoltronieri.kafra.data;

import java.util.Optional;

import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GuildDTORepository extends JpaRepository<GuildDTO, Integer> {
    @SuppressWarnings("null")
    Optional<GuildDTO> findById(@NotNull Integer id);

    @SuppressWarnings("null")
    Optional<GuildDTO> findByGuildId(@NotNull Long guildId);

}