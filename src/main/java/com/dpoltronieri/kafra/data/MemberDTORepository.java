package com.dpoltronieri.kafra.data;

import java.util.Optional;

import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MemberDTORepository extends JpaRepository<MemberDTO, Long> {

    @SuppressWarnings("null")
    Optional<MemberDTO> findById(@NotNull Long id);

    Optional<MemberDTO> findByMemberId(Long memberId);
}