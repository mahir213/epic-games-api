package com.epicgames.net.api.service;

import com.epicgames.net.api.model.Game;
import com.epicgames.net.api.model.Genre;
import com.epicgames.net.api.repository.GameRepository;
import com.epicgames.net.api.repository.GenreRepository;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import jakarta.transaction.Transactional;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class GameService {

    private static final Logger logger = LoggerFactory.getLogger(GameService.class);

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private GenreRepository genreRepository;

    private final String CLIENT_ID = "31z9q49tpfl3beh8cjx2eq2a9kgyu3";
    private final String CLIENT_SECRET = "r504ztq6gkgtgmn4938pwbqc73ic9t";
    private final String TOKEN_URL = "https://id.twitch.tv/oauth2/token";
    private final String IGDB_URL = "https://api.igdb.com/v4/games";

    private String getAccessToken() {
        try {
            HttpResponse<JsonNode> response = Unirest.post(TOKEN_URL)
                    .queryString("client_id", CLIENT_ID)
                    .queryString("client_secret", CLIENT_SECRET)
                    .queryString("grant_type", "client_credentials")
                    .asJson();

            if (response.getStatus() == 200) {
                String accessToken = response.getBody().getObject().getString("access_token");
                logger.info("Access Token: {}", accessToken);
                return accessToken;
            } else {
                logger.error("Failed to get access token: {}", response.getStatusText());
            }
        } catch (Exception e) {
            logger.error("Error fetching access token: {}", e.getMessage());
        }
        return null;
    }

    @Transactional
    public List<Game> fetchAndSaveGames() {
        String accessToken = getAccessToken();
        if (accessToken == null) {
            logger.error("Access token is null. Cannot fetch games.");
            return new ArrayList<>();
        }

        try {
            HttpResponse<JsonNode> jsonResponse = Unirest.post(IGDB_URL)
                    .header("Client-ID", CLIENT_ID)
                    .header("Authorization", "Bearer " + accessToken)
                    .header("Accept", "application/json")
                    .body("fields id, name, genres, cover.url, summary, rating;")
                    .asJson();

            List<Game> games = convertJsonToGames(jsonResponse.getBody());
            System.out.println(games);

            Set<Long> existingGameIds = gameRepository.findAll()
                    .stream()
                    .map(Game::getId)
                    .collect(Collectors.toSet());

            List<Game> newGames = games.stream()
                    .filter(game -> !existingGameIds.contains(game.getId()))
                    .collect(Collectors.toList());

            if (!newGames.isEmpty()) {
                gameRepository.saveAll(newGames);
            }

            return games;
        } catch (Exception e) {
            logger.error("Error fetching data from IGDB API: {}", e.getMessage());
        }
        return new ArrayList<>();
    }

    private List<Game> convertJsonToGames(JsonNode jsonResponse) {
        List<Game> games = new ArrayList<>();
        JSONArray gameArray = jsonResponse.getArray();

        for (int i = 0; i < gameArray.length(); i++) {
            JSONObject gameObject = gameArray.getJSONObject(i);
            Game game = new Game();

            game.setId(gameObject.optLong("id"));
            game.setName(gameObject.optString("name"));

            if (gameObject.has("cover")) {
                JSONObject coverObject = gameObject.getJSONObject("cover");
                String coverUrl = coverObject.optString("url", "");
                game.setCoverUrl("https:" + coverUrl);
            }

            game.setSummary(gameObject.optString("summary"));
            game.setRating(gameObject.optDouble("rating", 0.0));

            List<Genre> genreList = new ArrayList<>();
            if (gameObject.has("genres")) {
                JSONArray genresArray = gameObject.getJSONArray("genres");
                for (int j = 0; j < genresArray.length(); j++) {
                    Long genreId = genresArray.getLong(j);

                    Genre genre = genreRepository.findById(genreId).orElseGet(() -> {
                        Genre newGenre = new Genre();
                        newGenre.setId(genreId);
                        return genreRepository.save(newGenre);
                    });

                    genreList.add(genre);
                }
            }
            game.setGenres(genreList);
            games.add(game);
        }

        return games;
    }

}
