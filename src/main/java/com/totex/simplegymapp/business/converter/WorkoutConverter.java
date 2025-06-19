package com.totex.simplegymapp.business.converter;

import com.totex.simplegymapp.business.dto.WorkoutDto;
import com.totex.simplegymapp.infrastructure.model.UserModel;
import com.totex.simplegymapp.infrastructure.model.WorkoutModel;
import org.springframework.stereotype.Component;

@Component
public class WorkoutConverter {
    public WorkoutDto toWorkoutDto(WorkoutModel model) {
        WorkoutDto dto = new WorkoutDto();
        dto.setWorkoutId(model.getWorkoutId());
        dto.setWorkoutName(model.getWorkoutName());
        dto.setUserId(model.getUser().getUserId());
        dto.setUsername(model.getUser().getUsername());
        dto.setStartDate(model.getStartDate());
        return dto;
    }

    public WorkoutModel toWorkoutModel(WorkoutDto dto, UserModel user) {
        WorkoutModel model = new WorkoutModel();
        model.setWorkoutName(dto.getWorkoutName());
        model.setUser(user);
        model.setStartDate(dto.getStartDate());
        return model;
    }
}
