package vttp.sff.day16workshopPdf.services;

import java.io.StringReader;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonReader;

import vttp.sff.day16workshopPdf.repositories.BoardGameRepository;

@Service
public class BoardGameService {

    @Autowired
    private BoardGameRepository boardGameRepo;

    // Comment: technically, the easier way would be to read array -> just send the
    // object
    public void insertBoardGameArray(String redisKey, String payload) {
        // Assume that payload always comes in format of JsonArray[JsonObject1,
        // JsonObject2 ...]
        // Break up the payload and save the Objects SEPARATELY
        JsonReader reader = Json.createReader(new StringReader(payload));
        JsonArray jsonArr = reader.readArray();

        insertJsonArrayToRepo(redisKey, jsonArr);
    }

    public Optional<String> getBoardById(String key, String id) {
        return boardGameRepo.getBoardGameById(key, id);
    }

    // returns number of board games updated
    public int insertBoardGameArrayWithCheck(String redisKey, String payload, boolean upsert) {

        JsonReader reader = Json.createReader(new StringReader(payload));
        JsonArray jsonArr = reader.readArray();

        if (upsert) {
            insertJsonArrayToRepo(redisKey, jsonArr);
            return jsonArr.size();
        }

        // If upsert false -> check if all game Ids to be added exist in the Database
        else {
            boolean allIdExist = jsonArr.stream()
                    .map(val -> val.asJsonObject())
                    .allMatch(obj -> boardGameRepo.boardGameIdExist(redisKey, String.valueOf(obj.getInt("gid"))));

            if (!allIdExist)
                return 0;

            insertJsonArrayToRepo(redisKey, jsonArr);
            return jsonArr.size();
        }
    }

    // Helper function
    public void insertJsonArrayToRepo(String redisKey, JsonArray jsonArr) {
        jsonArr.stream()
                .map(val -> val.asJsonObject())
                .forEach(boardGameAsJsonObj -> boardGameRepo.storeJson(redisKey, boardGameAsJsonObj));
    }
}
