package com.totex.simplegymapp.controller;

import com.totex.simplegymapp.business.dto.ExerciseDto;
import com.totex.simplegymapp.business.service.ExerciseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/exercises")
@RequiredArgsConstructor
public class ExerciseController {

    private final ExerciseService exerciseService;

    @PostMapping
    public ResponseEntity<ExerciseDto> createExercise(
            @RequestBody @Valid ExerciseDto exerciseDto,
            @RequestHeader("Authorization") String token) {
        ExerciseDto createdExercise = exerciseService.createExercise(exerciseDto, token);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdExercise);
    }

    @GetMapping("/workout/{workoutId}")
    public ResponseEntity<List<ExerciseDto>> getExercisesByWorkout(
            @PathVariable Long workoutId,
            @RequestHeader("Authorization") String token) {
        List<ExerciseDto> exercises = exerciseService.getExercisesByWorkout(workoutId, token);
        return ResponseEntity.ok(exercises);
    }

    @GetMapping("/{exerciseId}")
    public ResponseEntity<ExerciseDto> getExerciseById(
            @PathVariable Long exerciseId,
            @RequestHeader("Authorization") String token) {
        ExerciseDto exercise = exerciseService.getExerciseById(exerciseId, token);
        return ResponseEntity.ok(exercise);
    }

    @PutMapping("/{exerciseId}")
    public ResponseEntity<ExerciseDto> updateExercise(
            @PathVariable Long exerciseId,
            @RequestBody @Valid ExerciseDto exerciseDto,
            @RequestHeader("Authorization") String token) {
        ExerciseDto updatedExercise = exerciseService.updateExercise(exerciseId, exerciseDto, token);
        return ResponseEntity.ok(updatedExercise);
    }

    @DeleteMapping("/{exerciseId}")
    public ResponseEntity<Void> deleteExercise(
            @PathVariable Long exerciseId,
            @RequestHeader("Authorization") String token) {
        exerciseService.deleteExercise(exerciseId, token);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/all")
    public ResponseEntity<List<ExerciseDto>> getAllExercises() {
        List<ExerciseDto> exercises = exerciseService.getAllExercises();
        return ResponseEntity.ok(exercises);
    }
}