package com.epicgames.net.api.controller;

import com.epicgames.net.api.model.Game;
import com.epicgames.net.api.service.GameService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class GameController {

    @Autowired
    private GameService gameService;

    @GetMapping("/fetchGames")
    public List<Game> fetchGames() {
        return gameService.fetchAndSaveGames();
    }
}
