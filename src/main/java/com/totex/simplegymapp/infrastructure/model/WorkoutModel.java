package com.totex.simplegymapp.infrastructure.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "workout")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class WorkoutModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long workoutId;

    @Column(name = "workout_name")
    private String workoutName;

    @Column(name = "start_date")
    private LocalDate startDate;

    @OneToMany(mappedBy = "workout", cascade = CascadeType.ALL)
    private List<ExerciseModel> exercises = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "userId")
    private UserModel user;

}
