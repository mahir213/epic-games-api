package com.epicgames.net.api.repository;

import com.epicgames.net.api.model.Game;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GameRepository extends JpaRepository<Game, Long> {

}
