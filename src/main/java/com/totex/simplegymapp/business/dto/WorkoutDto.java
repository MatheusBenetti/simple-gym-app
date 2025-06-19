package com.totex.simplegymapp.business.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class WorkoutDto {
    private Long workoutId;
    private String workoutName;
    private LocalDate startDate;
    private List<ExerciseDto> exercises;
    private String username;
    private Long userId;
}
