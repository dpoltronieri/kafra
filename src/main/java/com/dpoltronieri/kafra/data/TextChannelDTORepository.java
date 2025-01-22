package com.dpoltronieri.kafra.data;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TextChannelDTORepository extends JpaRepository<TextChannelDTO, Long> {
    Optional<TextChannelDTO> findByChannelId(Long channelId);
    Optional<TextChannelDTO> findByChannelIdAndGuild(Long channelId, GuildDTO guild);
}