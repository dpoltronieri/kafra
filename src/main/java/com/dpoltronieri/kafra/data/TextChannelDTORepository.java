package com.dpoltronieri.kafra.data;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TextChannelDTORepository extends JpaRepository<TextChannelDTO, Long> {

    TextChannelDTO findByChannelId(Long channelId); 
}