package hunre.it.backendchess.controller;

import hunre.it.backendchess.models.Game;
import hunre.it.backendchess.models.OpponentType;
import hunre.it.backendchess.repository.GameRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/game")
@RequiredArgsConstructor
public class GameController {

    private final GameRepository gameRepository;

    // 📌 API: Lưu ván đấu
    @PostMapping("/save")
    public ResponseEntity<String> saveGame(@RequestBody Game game) {
        try {
            // Kiểm tra nếu opponentType là null để tránh lỗi
            if (game.getOpponentType() != null) {
                // Chuyển opponentType từ String sang Enum đúng cách
                game.setOpponentType(OpponentType.valueOf(game.getOpponentType().name().toUpperCase()));
            }
            gameRepository.save(game);
            return ResponseEntity.ok("Game saved successfully");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Invalid OpponentType value");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error saving game");
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
}
