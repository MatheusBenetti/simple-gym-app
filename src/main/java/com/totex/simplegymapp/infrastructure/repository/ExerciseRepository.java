package com.totex.simplegymapp.infrastructure.repository;

import com.totex.simplegymapp.infrastructure.model.ExerciseModel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExerciseRepository extends JpaRepository<ExerciseModel, Long> {
}
