package com.epicgames.net.api.repository;

import com.epicgames.net.api.model.Genre;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GenreRepository extends JpaRepository<Genre, Long> {

}
