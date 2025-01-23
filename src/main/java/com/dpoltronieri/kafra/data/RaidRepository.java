package com.dpoltronieri.kafra.data;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RaidRepository extends JpaRepository<Raid, Long> {
    Raid findByEventId(Long eventId);
}