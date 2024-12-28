package com.dpoltronieri.kafra.data;

import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;


public interface UserDTORepository extends JpaRepository<UserDTO, Integer> {
    @SuppressWarnings("null")
    Optional<UserDTO> findById(@NotNull Integer id);
}
