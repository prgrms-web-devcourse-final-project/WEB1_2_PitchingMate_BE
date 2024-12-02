package com.example.mate.domain.match.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Weather {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private Float temperature;

    @Column
    private Float pop;

    @Column
    private Integer cloudiness;

    @Column
    private LocalDateTime wtTime;

    @OneToOne(mappedBy = "weather")
    private Match match;

    @Builder
    public Weather(Float temperature, Float pop, Integer cloudiness, LocalDateTime wtTime) {
        this.temperature = temperature;
        this.pop = pop;
        this.cloudiness = cloudiness;
        this.wtTime = wtTime;
    }

    protected void setMatch(Match match) {
        this.match = match;
    }
}