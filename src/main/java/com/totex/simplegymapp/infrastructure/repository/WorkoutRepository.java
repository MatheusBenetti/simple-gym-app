package com.totex.simplegymapp.infrastructure.repository;

import com.totex.simplegymapp.infrastructure.model.WorkoutModel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkoutRepository extends JpaRepository<WorkoutModel, Long> {
}
