package hunre.it.backendchess.controller;

import hunre.it.backendchess.DTO.GameOverMessage;
import hunre.it.backendchess.models.Game;
import hunre.it.backendchess.models.GameResult;
import hunre.it.backendchess.models.OpponentType;
import hunre.it.backendchess.repository.GameRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.handler.annotation.Payload;


@RestController
@RequestMapping("/api/game")
@RequiredArgsConstructor
public class GameController {

    private final GameRepository gameRepository;

    // 📌 API: Lưu ván đấu
    @PostMapping("/save")
    public ResponseEntity<String> saveGame(@RequestBody Game game) {
        try {
            if (game.getMoves() == null || game.getMoves().trim().isEmpty()) {
                game.setMoves("No moves");
            }

            if (game.getMatchId() == null) {
                game.setMatchId(UUID.randomUUID()); // Gán matchId nếu chưa có
            }

            gameRepository.save(game);
            return ResponseEntity.ok("Game saved successfully");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error saving game: " + e.getMessage());
        }
    }





    // 📌 API: Lấy danh sách ván đấu của một người chơi
    @GetMapping("/history/{username}")
    public ResponseEntity<List<Game>> getUserGames(@PathVariable String username) {
        List<Game> games = gameRepository.findByPlayerUsernameOrderByCreatedAtDesc(username)
                .stream()
                .map(game -> {
                    game.setOpponentType(OpponentType.fromString(game.getOpponentType().toString()));
                    return game;
                })
                .collect(Collectors.toList());
        return ResponseEntity.ok(games);
    }


    // 📌 API: Lấy chi tiết một ván đấu theo ID
    @GetMapping("/{id}")
    public ResponseEntity<?> getGameById(@PathVariable Long id) {
        Optional<Game> game = gameRepository.findById(id);
        if (game.isPresent()) {
            return ResponseEntity.ok(game.get());
        } else {
            return ResponseEntity.status(404).body("Không tìm thấy ván đấu");
        }
    }

    @GetMapping("/match/{matchId}")
    public ResponseEntity<Game> getGameByMatchId(@PathVariable UUID matchId) {
        return ResponseEntity.of(gameRepository.findByMatchId(matchId));
    }


    // 📌 API: Xóa ván đấu
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteGame(@PathVariable Long id) {
        if (gameRepository.existsById(id)) {
            gameRepository.deleteById(id);
            return ResponseEntity.ok("Ván đấu đã được xóa.");
        } else {
            return ResponseEntity.status(404).body("Không tìm thấy ván đấu để xóa.");
        }
    }

    @MessageMapping("/game-over")
    @SendTo("/topic/game-over")
    public GameOverMessage handleGameOver(@Payload GameOverMessage message) {
        System.out.println("Received game-over message: " + message);

        if (message.getMatchId() == null || message.getMatchId().trim().isEmpty()) {
            System.out.println("⚠ matchId is null, skipping game save.");
            return message;
        }

        UUID matchId;
        try {
            matchId = UUID.fromString(message.getMatchId());
        } catch (IllegalArgumentException e) {
            System.out.println("⚠ Invalid matchId format: " + message.getMatchId());
            return message;
        }

        String opponentUsername = gameRepository.findOpponentByMatchId(matchId);
        if (opponentUsername.equals("UNKNOWN")) {
            opponentUsername = "Unknown Player";
        }

        if (!gameRepository.existsByPlayerUsernameAndResult(message.getSender(), GameResult.valueOf(message.getResult().toUpperCase()))) {
            saveGameForPlayer(matchId, message.getSender(), opponentUsername, message.getResult());
        }

        return message;
    }

    private void saveGameForPlayer(UUID matchId, String playerUsername, String opponentUsername, String result) {
        if (gameRepository.existsByPlayerUsernameAndResult(playerUsername, GameResult.valueOf(result.toUpperCase()))) {
            System.out.println("Game already exists for " + playerUsername + ". Skipping save.");
            return;
        }

        Game game = new Game();
        game.setMatchId(matchId);
        game.setPlayerUsername(playerUsername);
        game.setOpponent(opponentUsername.equals("UNKNOWN") ? "Unknown Player" : opponentUsername);
        game.setResult(GameResult.valueOf(result.toUpperCase()));
        game.setOpponentType(opponentUsername.equals("UNKNOWN") ? OpponentType.BOT : OpponentType.HUMAN);
        game.setMoves("No moves");

        gameRepository.save(game);
    }
}
