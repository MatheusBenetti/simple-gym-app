package com.totex.simplegymapp.infrastructure.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "exercise")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ExerciseModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long exerciseId;

    @Column(name = "exercise_name")
    private String exerciseName;

    @Column(name = "series")
    private int series;

    @Column(name = "repetitions")
    private int repetitions;

    @ManyToOne()
    @JoinColumn(name = "workout_id", referencedColumnName = "workoutId")
    private WorkoutModel workout;
}
