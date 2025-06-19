package com.totex.simplegymapp.business.service;

import com.totex.simplegymapp.business.converter.WorkoutConverter;
import com.totex.simplegymapp.business.dto.WorkoutDto;
import com.totex.simplegymapp.infrastructure.exception.ResourceNotFoundException;
import com.totex.simplegymapp.infrastructure.model.UserModel;
import com.totex.simplegymapp.infrastructure.model.WorkoutModel;
import com.totex.simplegymapp.infrastructure.repository.UserRepository;
import com.totex.simplegymapp.infrastructure.repository.WorkoutRepository;
import com.totex.simplegymapp.infrastructure.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WorkoutService {

    private final WorkoutRepository workoutRepository;
    private final UserRepository userRepository;
    private final WorkoutConverter workoutConverter;
    private final JwtUtil jwtUtil;

    public WorkoutDto createWorkout(String token, WorkoutDto workoutDto) {
        String email = jwtUtil.extractEmailToken(token.substring(7));

        UserModel user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found."));

        WorkoutModel workout = workoutConverter.toWorkoutModel(workoutDto, user);
        workout = workoutRepository.save(workout);

        return workoutConverter.toWorkoutDto(workout);
    }

    public List<WorkoutDto> getUserWorkouts(String token) {
        String email = jwtUtil.extractEmailToken(token.substring(7));

        UserModel user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found."));

        return user.getWorkouts().stream()
                .map(workoutConverter::toWorkoutDto)
                .collect(Collectors.toList());
    }

    public WorkoutDto getWorkoutById(Long workoutId, String token) {
        String email = jwtUtil.extractEmailToken(token.substring(7));

        WorkoutModel workout = workoutRepository.findById(workoutId)
                .orElseThrow(() -> new ResourceNotFoundException("Workout not found."));

        if (!workout.getUser().getEmail().equals(email)) {
            throw new ResourceNotFoundException("Workout not found.");
        }

        return workoutConverter.toWorkoutDto(workout);
    }

    public WorkoutDto updateWorkout(Long workoutId, WorkoutDto workoutDto, String token) {
        String email = jwtUtil.extractEmailToken(token.substring(7));

        WorkoutModel workout = workoutRepository.findById(workoutId)
                .orElseThrow(() -> new ResourceNotFoundException("Workout not found."));

        if (!workout.getUser().getEmail().equals(email)) {
            throw new ResourceNotFoundException("Workout not found.");
        }

        if (workoutDto.getWorkoutName() != null && !workoutDto.getWorkoutName().isBlank()) {
            workout.setWorkoutName(workoutDto.getWorkoutName());
        }
        if (workoutDto.getStartDate() != null) {
            workout.setStartDate(workoutDto.getStartDate());
        }

        workout = workoutRepository.save(workout);
        return workoutConverter.toWorkoutDto(workout);
    }

    public void deleteWorkout(Long workoutId, String token) {
        String email = jwtUtil.extractEmailToken(token.substring(7));

        WorkoutModel workout = workoutRepository.findById(workoutId)
                .orElseThrow(() -> new ResourceNotFoundException("Workout not found."));

        if (!workout.getUser().getEmail().equals(email)) {
            throw new ResourceNotFoundException("Workout not found.");
        }

        workoutRepository.delete(workout);
    }

    public Page<WorkoutDto> getAllWorkouts(Pageable pageable) {
        return workoutRepository.findAll(pageable)
                .map(workoutConverter::toWorkoutDto);
    }
}