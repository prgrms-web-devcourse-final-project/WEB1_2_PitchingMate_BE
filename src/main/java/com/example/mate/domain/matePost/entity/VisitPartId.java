package com.example.mate.domain.matePost.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VisitPartId implements Serializable {
    private Long member;
    private Long visit;
}
