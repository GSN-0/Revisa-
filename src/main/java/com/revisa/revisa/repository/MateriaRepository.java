package com.revisa.revisa.repository;

import com.revisa.revisa.model.Materia;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MateriaRepository extends JpaRepository<Materia, Long> {

    List<Materia> findAllByUserEmail(String email);

    Page<Materia> findAllByUserEmail(String email, Pageable pageable);

    Long countByUserEmail(String email);

    Optional<Materia> findByIdAndUserEmail(Long id, String email);
}
