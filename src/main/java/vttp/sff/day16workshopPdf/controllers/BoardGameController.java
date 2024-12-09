package vttp.sff.day16workshopPdf.controllers;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import vttp.sff.day16workshopPdf.common.Constants;
import vttp.sff.day16workshopPdf.services.BoardGameService;

@RestController
@RequestMapping
public class BoardGameController {

    @Autowired
    private BoardGameService boardGameSvc;

    // Task 1: User will send a Json file (copied from game.json) in the POST
    //      request
    @PostMapping(path = "/api/boardgame", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> postBoardGame(@RequestBody String payload) {

        boardGameSvc.insertBoardGameArray(Constants.REDIS_JSON_KEY, payload);

        JsonObject response = Json.createObjectBuilder()
                .add("insert_count", 1)
                .add("id", Constants.REDIS_JSON_KEY)
                .build();

        return ResponseEntity.status(201).body(response.toString());
    }

    // Task 2: GET /api/boardgame/<boardgame_id> --> retrieves a given board, if not
    //      found return 404 w/ appropriate error object
    @GetMapping(path = "/api/boardgame/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getBoardGameById(@PathVariable(name = "id") String id) {

        Optional<String> opt = boardGameSvc.getBoardById(Constants.REDIS_JSON_KEY, id);

        if (opt.isEmpty()) {

            // Creating the custom Error Json Object
            JsonObject response = Json.createObjectBuilder()
                    .add("error", "BoardGame Not Found")
                    .add("message", "The board game with ID %s does not exist.".formatted(id))
                    .add("timestamp", System.currentTimeMillis())
                    .build();

            return ResponseEntity.status(HttpStatusCode.valueOf(404)).body(response.toString());
        }

        return ResponseEntity.ok(opt.get());
    }

    /*
     * Task 3: PUT /api/boardgame/<boardgame_id> --> takes payload and attempts to
     *      update the data stored in Redis with the corresponding key
     *      <boardgame_id>. If <boardgame_id> does not exist, endpoint should return a
     *      400 status code and an appropriate error object.
     *      Endpoint takes optional parameter: upsert -> if true, it should insert even
     *      if <boardgame_id> does not exist
     */

     // Note: Did not utilize the {id} in the path as I am accounting for situations where there may be
     //     multiple JsonObjects in the array
    @PutMapping(path = "/api/boardgame/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> putBoardGameById(@PathVariable(name = "id") String id,
            @RequestBody String payload,
            @RequestParam(name = "upsert", required = false) boolean upsert) {

        int updateSuccess = boardGameSvc.insertBoardGameArrayWithCheck(Constants.REDIS_JSON_KEY, payload, upsert);

        // 0 = no successful updates = fail
        if (updateSuccess == 0) {
            JsonObject response = Json.createObjectBuilder()
                    .add("error", "BoardGame Not Found")
                    .add("message", "One or more board game ids do not exist. Upsert false")
                    .add("timestamp", System.currentTimeMillis())
                    .build();

            return ResponseEntity.status(400).body(response.toString());
        }

        JsonObject response = Json.createObjectBuilder()
                .add("update_count", updateSuccess)
                .add("id", Constants.REDIS_JSON_KEY)
                .build();

        return ResponseEntity.status(200).body(response.toString());
    }

}
