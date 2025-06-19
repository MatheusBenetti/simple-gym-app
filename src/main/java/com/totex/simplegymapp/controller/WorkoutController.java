package com.totex.simplegymapp.controller;

import com.totex.simplegymapp.business.dto.WorkoutDto;
import com.totex.simplegymapp.business.service.WorkoutService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/workouts")
@RequiredArgsConstructor
public class WorkoutController {

    private final WorkoutService workoutService;

    @PostMapping
    public ResponseEntity<WorkoutDto> createWorkout(
            @RequestBody @Valid WorkoutDto workoutDto,
            @RequestHeader("Authorization") String token) {
        WorkoutDto createdWorkout = workoutService.createWorkout(token, workoutDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdWorkout);
    }

    @GetMapping("/my-workouts")
    public ResponseEntity<List<WorkoutDto>> getMyWorkouts(
            @RequestHeader("Authorization") String token) {
        List<WorkoutDto> workouts = workoutService.getUserWorkouts(token);
        return ResponseEntity.ok(workouts);
    }

    @GetMapping("/{workoutId}")
    public ResponseEntity<WorkoutDto> getWorkoutById(
            @PathVariable Long workoutId,
            @RequestHeader("Authorization") String token) {
        WorkoutDto workout = workoutService.getWorkoutById(workoutId, token);
        return ResponseEntity.ok(workout);
    }

    @PutMapping("/{workoutId}")
    public ResponseEntity<WorkoutDto> updateWorkout(
            @PathVariable Long workoutId,
            @RequestBody @Valid WorkoutDto workoutDto,
            @RequestHeader("Authorization") String token) {
        WorkoutDto updatedWorkout = workoutService.updateWorkout(workoutId, workoutDto, token);
        return ResponseEntity.ok(updatedWorkout);
    }

    @DeleteMapping("/{workoutId}")
    public ResponseEntity<Void> deleteWorkout(
            @PathVariable Long workoutId,
            @RequestHeader("Authorization") String token) {
        workoutService.deleteWorkout(workoutId, token);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/all")
    public ResponseEntity<Page<WorkoutDto>> getAllWorkouts(
            @PageableDefault(size = 20, sort = "workoutId") Pageable pageable) {
        Page<WorkoutDto> workouts = workoutService.getAllWorkouts(pageable);
        return ResponseEntity.ok(workouts);
    }
}