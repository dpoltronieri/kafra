package com.dpoltronieri.kafra.data;

import java.util.Optional;

import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface MemberDTORepository extends JpaRepository<MemberDTO, Long> {

    @SuppressWarnings("null")
    Optional<MemberDTO> findById(@NotNull Long id);

    Optional<MemberDTO> findByMemberId(Long memberId);

    @Query("SELECT m FROM MemberDTO m LEFT JOIN FETCH m.roles WHERE m.memberId = :memberId")
    Optional<MemberDTO> findByMemberIdWithRoles(@Param("memberId") Long memberId);
}