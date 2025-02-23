package com.dpoltronieri.kafra.data;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MissionRepository extends JpaRepository<Mission, Long> {
    Optional<Mission> findByNameAndGuild(String name, GuildDTO guild);
    List<Mission> findByTypeAndGuild(String type, GuildDTO guild);
    List<Mission> findByGuild(GuildDTO guild); // Keep this method to find all missions in a guild
    Optional<Mission> findByIdAndGuild(Long id, GuildDTO guild);
}