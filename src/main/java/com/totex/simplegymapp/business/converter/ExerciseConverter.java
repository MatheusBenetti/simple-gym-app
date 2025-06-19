package com.totex.simplegymapp.business.converter;

import com.totex.simplegymapp.business.dto.ExerciseDto;
import com.totex.simplegymapp.infrastructure.model.ExerciseModel;
import com.totex.simplegymapp.infrastructure.model.WorkoutModel;
import org.springframework.stereotype.Component;

@Component
public class ExerciseConverter {
    public ExerciseDto toExerciseDto(ExerciseModel model) {
        ExerciseDto dto = new ExerciseDto();
        dto.setExerciseId(model.getExerciseId());
        dto.setExerciseName(model.getExerciseName());
        dto.setWorkoutId(model.getWorkout().getWorkoutId());
        dto.setSeries(model.getSeries());
        dto.setRepetitions(model.getRepetitions());
        return dto;
    }

    public ExerciseModel toExerciseModel(ExerciseDto dto, WorkoutModel workout) {
        ExerciseModel model = new ExerciseModel();
        model.setExerciseId(dto.getExerciseId());
        model.setExerciseName(dto.getExerciseName());
        model.setRepetitions(dto.getRepetitions());
        model.setSeries(dto.getSeries());
        model.setWorkout(workout);
        return model;
    }
}
