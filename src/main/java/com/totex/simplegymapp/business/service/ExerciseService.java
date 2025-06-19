package com.totex.simplegymapp.business.service;

import com.totex.simplegymapp.business.converter.ExerciseConverter;
import com.totex.simplegymapp.business.dto.ExerciseDto;
import com.totex.simplegymapp.infrastructure.exception.ResourceNotFoundException;
import com.totex.simplegymapp.infrastructure.model.ExerciseModel;
import com.totex.simplegymapp.infrastructure.model.WorkoutModel;
import com.totex.simplegymapp.infrastructure.repository.ExerciseRepository;
import com.totex.simplegymapp.infrastructure.repository.WorkoutRepository;
import com.totex.simplegymapp.infrastructure.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExerciseService {

    private final ExerciseRepository exerciseRepository;
    private final WorkoutRepository workoutRepository;
    private final ExerciseConverter exerciseConverter;
    private final JwtUtil jwtUtil;

    public ExerciseDto createExercise(ExerciseDto exerciseDto, String token) {
        String email = jwtUtil.extractEmailToken(token.substring(7));

        WorkoutModel workout = workoutRepository.findById(exerciseDto.getWorkoutId())
                .orElseThrow(() -> new ResourceNotFoundException("Workout not found."));

        if (!workout.getUser().getEmail().equals(email)) {
            throw new ResourceNotFoundException("Workout not found.");
        }

        ExerciseModel exercise = exerciseConverter.toExerciseModel(exerciseDto, workout);
        exercise = exerciseRepository.save(exercise);

        return exerciseConverter.toExerciseDto(exercise);
    }

    public List<ExerciseDto> getExercisesByWorkout(Long workoutId, String token) {
        String email = jwtUtil.extractEmailToken(token.substring(7));

        WorkoutModel workout = workoutRepository.findById(workoutId)
                .orElseThrow(() -> new ResourceNotFoundException("Workout not found."));

        if (!workout.getUser().getEmail().equals(email)) {
            throw new ResourceNotFoundException("Workout not found.");
        }

        return workout.getExercises().stream()
                .map(exerciseConverter::toExerciseDto)
                .collect(Collectors.toList());
    }

    public ExerciseDto getExerciseById(Long exerciseId, String token) {
        String email = jwtUtil.extractEmailToken(token.substring(7));

        ExerciseModel exercise = exerciseRepository.findById(exerciseId)
                .orElseThrow(() -> new ResourceNotFoundException("Exercise not found."));

        if (!exercise.getWorkout().getUser().getEmail().equals(email)) {
            throw new ResourceNotFoundException("Exercise not found.");
        }

        return exerciseConverter.toExerciseDto(exercise);
    }

    public ExerciseDto updateExercise(Long exerciseId, ExerciseDto exerciseDto, String token) {
        String email = jwtUtil.extractEmailToken(token.substring(7));

        ExerciseModel exercise = exerciseRepository.findById(exerciseId)
                .orElseThrow(() -> new ResourceNotFoundException("Exercise not found."));

        if (!exercise.getWorkout().getUser().getEmail().equals(email)) {
            throw new ResourceNotFoundException("Exercise not found.");
        }

        if (exerciseDto.getExerciseName() != null && !exerciseDto.getExerciseName().isBlank()) {
            exercise.setExerciseName(exerciseDto.getExerciseName());
        }
        if (exerciseDto.getSeries() > 0) {
            exercise.setSeries(exerciseDto.getSeries());
        }
        if (exerciseDto.getRepetitions() > 0) {
            exercise.setRepetitions(exerciseDto.getRepetitions());
        }

        exercise = exerciseRepository.save(exercise);
        return exerciseConverter.toExerciseDto(exercise);
    }

    public void deleteExercise(Long exerciseId, String token) {
        String email = jwtUtil.extractEmailToken(token.substring(7));

        ExerciseModel exercise = exerciseRepository.findById(exerciseId)
                .orElseThrow(() -> new ResourceNotFoundException("Exercise not found."));

        if (!exercise.getWorkout().getUser().getEmail().equals(email)) {
            throw new ResourceNotFoundException("Exercise not found.");
        }

        exerciseRepository.delete(exercise);
    }

    public List<ExerciseDto> getAllExercises() {
        return exerciseRepository.findAll().stream()
                .map(exerciseConverter::toExerciseDto)
                .collect(Collectors.toList());
    }
}