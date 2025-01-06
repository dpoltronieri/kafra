package com.dpoltronieri.kafra.data;

import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;



public interface UserDTORepository extends JpaRepository<UserDTO, Long> {
    @SuppressWarnings("null")
    Optional<UserDTO> findById(@NotNull Long id);

    Optional<UserDTO> findByUserId(Long userId);
}
