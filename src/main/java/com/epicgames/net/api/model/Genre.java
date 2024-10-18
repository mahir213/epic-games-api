package com.epicgames.net.api.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
public class Genre {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
}
