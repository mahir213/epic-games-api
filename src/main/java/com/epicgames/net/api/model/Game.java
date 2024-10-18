package com.epicgames.net.api.model;

import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Data
@Entity
public class Game {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Lob
    private String name;

    private String coverUrl;

    @ManyToMany
    private List<Genre> genres;

    @Lob
    private String summary;

    private Double rating;

    private Long versionParent;
}
