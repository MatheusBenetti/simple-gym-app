package com.totex.simplegymapp.business.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserCreateDto {
    private Long id;
    private String username;
    private String password;
    private String email;
    private List<WorkoutDto> workouts;
}
