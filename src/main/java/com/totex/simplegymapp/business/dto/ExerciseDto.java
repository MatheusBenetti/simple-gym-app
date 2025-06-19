package com.totex.simplegymapp.business.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ExerciseDto {
    private Long exerciseId;
    private String exerciseName;
    private int series;
    private int repetitions;
    private Long workoutId;
}
