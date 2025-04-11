package hunre.it.backendchess.controller;

import hunre.it.backendchess.DTO.GameOverMessage;
import hunre.it.backendchess.DTO.UserStats;
import hunre.it.backendchess.models.Game;
import hunre.it.backendchess.models.GameResult;
import hunre.it.backendchess.models.OpponentType;
import hunre.it.backendchess.models.User;
import hunre.it.backendchess.repository.GameRepository;
import hunre.it.backendchess.repository.UserRepository;
import hunre.it.backendchess.service.RatingService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.handler.annotation.Payload;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/game")
@RequiredArgsConstructor
public class GameController {

    private final GameRepository gameRepository;

    private final RatingService ratingService;

    private final UserRepository userRepository;
    // 📌 API: Lưu ván đấu
    @PostMapping("/save")
    public ResponseEntity<?> saveGame(@RequestBody Game gameRequest) {
        try {
            // Validate các trường bắt buộc
            if (gameRequest.getPlayerUsername() == null || gameRequest.getPlayerUsername().isBlank()) {
                return ResponseEntity.badRequest().body("Tên người chơi là bắt buộc");
            }
            if (gameRequest.getOpponent() == null || gameRequest.getOpponent().isBlank()) {
                return ResponseEntity.badRequest().body("Tên đối thủ là bắt buộc");
            }
            if (gameRequest.getOpponentType() == null) {
                return ResponseEntity.badRequest().body("Loại đối thủ là bắt buộc");
            }

            // Kiểm tra đối thủ là người thật
            if (gameRequest.getOpponentType() == OpponentType.HUMAN) {
                if (!userRepository.existsByUsername(gameRequest.getOpponent())) {
                    return ResponseEntity.badRequest().body("Đối thủ không tồn tại");
                }
            }

            // Xử lý matchId
            UUID matchId = gameRequest.getMatchId();
            boolean isNewMatch = (matchId == null);
            if (isNewMatch) {
                matchId = UUID.randomUUID();
                gameRequest.setMatchId(matchId);
            }

            // Kiểm tra trùng lặp
//            if (gameRepository.existsByMatchIdAndPlayerUsername(matchId, gameRequest.getPlayerUsername())) {
//                return ResponseEntity.status(HttpStatus.CONFLICT)
//                        .body("Người chơi đã lưu game này trước đó");
//            }

            // Tạo và lưu game
            Game gameToSave = new Game();
            gameToSave.setMatchId(matchId);
            gameToSave.setPlayerUsername(gameRequest.getPlayerUsername());
            gameToSave.setOpponent(gameRequest.getOpponent());
            gameToSave.setOpponentType(gameRequest.getOpponentType());
            gameToSave.setResult(gameRequest.getResult());
            gameToSave.setMoves(gameRequest.getMoves() != null && !gameRequest.getMoves().isBlank()
                    ? gameRequest.getMoves()
                    : "No moves");
            gameToSave.setFinalFen(gameRequest.getFinalFen());
            gameToSave.setCreatedAt(LocalDateTime.now());
            gameToSave.setUserId(gameRequest.getUserId());
            gameRepository.save(gameToSave);

            if (gameRequest.getResult() == GameResult.WIN) {
                // Cập nhật ELO nếu là PvP
                if (gameRequest.getOpponentType() == OpponentType.HUMAN) {
                    ratingService.updateElo(
                            gameToSave.getPlayerUsername(),
                            gameRequest.getOpponent(),
                            gameToSave.getResult()
                    );
                }
            }
            return ResponseEntity.ok("Lưu game thành công. Match ID: " + matchId);

        } catch (DataIntegrityViolationException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Lỗi trùng lặp dữ liệu: " + e.getMostSpecificCause().getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("Lỗi khi lưu game: " + e.getMessage());
        }
    }


    // Trong API Controller
    @GetMapping("/stats/user/{userId}")
    public ResponseEntity<?> getUserStats(@PathVariable Long userId) {
        // Kiểm tra user tồn tại
        if (!userRepository.existsById(userId)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Không tìm thấy user với ID: " + userId);
        }

        // Thống kê qua userId
        long totalGames = gameRepository.countByUserId(userId);
        long wins = gameRepository.countByUserIdAndResult(userId, GameResult.WIN);
        long losses = gameRepository.countByUserIdAndResult(userId, GameResult.LOSE);
        long draws = gameRepository.countByUserIdAndResult(userId, GameResult.DRAW);

        // Tính tỷ lệ
        double winRate = totalGames > 0 ? (wins * 100.0 / totalGames) : 0;
        double lossRate = totalGames > 0 ? (losses * 100.0 / totalGames) : 0;
        double drawRate = totalGames > 0 ? (draws * 100.0 / totalGames) : 0;

        return ResponseEntity.ok(new UserStats(
                totalGames, wins, losses, draws,
                winRate, lossRate, drawRate
        ));
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
    public ResponseEntity<List<Game>> getGameByMatchId(@PathVariable UUID matchId) {
        List<Game> games = gameRepository.findByMatchId(matchId);
        if (games.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
        return ResponseEntity.ok(games);
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
    @Transactional
    public GameOverMessage handleGameOver(@Payload GameOverMessage message) {
        // Validate matchId
        if (message.getMatchId() == null || message.getMatchId().isBlank()) {
            System.out.println("⚠ matchId is required");
            return message;
        }

        UUID matchId;
        try {
            matchId = UUID.fromString(message.getMatchId());
        } catch (IllegalArgumentException e) {
            System.out.println("⚠ Invalid matchId: " + message.getMatchId());
            return message;
        }

        // Lưu game cho cả hai người
        saveGameForPlayer(matchId, message.getSender(), message.getOpponent(), message.getResult());

        // Lấy opponentUsername từ database
        String opponentUsername = gameRepository.findOpponentByMatchId(matchId, message.getSender());
        System.out.println("Opponent: " + opponentUsername);

        // Validate opponent
        if (opponentUsername == null || opponentUsername.isBlank() || opponentUsername.equalsIgnoreCase("UNKNOWN")) {
            System.out.println("⚠ Opponent không hợp lệ");
            return message;
        }

        // Cập nhật ELO
        try {
            GameResult result = GameResult.valueOf(message.getResult().toUpperCase());
            ratingService.updateElo(message.getSender(), opponentUsername, result);
            System.out.println("✅ ELO updated: " + message.getSender() + " vs " + opponentUsername);
        } catch (IllegalArgumentException e) {
            System.out.println("⚠ Invalid result: " + message.getResult());
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
