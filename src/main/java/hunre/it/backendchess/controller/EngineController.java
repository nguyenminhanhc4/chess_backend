package hunre.it.backendchess.controller;

import hunre.it.backendchess.engine.StockfishEngine;
import hunre.it.backendchess.models.AiAnalysis;
import hunre.it.backendchess.models.MoveCategory;
import hunre.it.backendchess.repository.AiAnalysisRepository;
import hunre.it.backendchess.service.OpeningBookService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/api/engine")
@RequiredArgsConstructor
public class EngineController {

    private final StockfishEngine stockfishEngine = new StockfishEngine();
    private final AiAnalysisRepository aiAnalysisRepository;
    private final OpeningBookService openingBookService;

    @PostConstruct
    public void init() throws Exception {
        stockfishEngine.startEngine();
        stockfishEngine.sendCommand("uci");
        stockfishEngine.sendCommand("isready");
    }

    @PreDestroy
    public void shutdownEngine() {
        stockfishEngine.stopEngine();
    }

    @PostMapping("/position")
    public ResponseEntity<String> updatePosition(@RequestBody String moves) {
        try {
            stockfishEngine.sendCommand("position startpos moves " + moves);
            return ResponseEntity.ok("Position updated");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi khi cập nhật vị trí: " + e.getMessage());
        }
    }

    @GetMapping("/go")
    public ResponseEntity<String> getEngineMove(@RequestParam(defaultValue = "medium") String difficulty) {
        try {
            int depth = switch (difficulty.toLowerCase()) {
                case "easy" -> 2;
                case "hard" -> 20;
                default -> 5;
            };
            stockfishEngine.sendCommand("go depth " + depth);
            String output = stockfishEngine.readOutput();
            return ResponseEntity.ok(output);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi khi lấy nước đi của engine: " + e.getMessage());
        }
    }

    @PostMapping("/analyzeDetailed")
    public ResponseEntity<?> analyzeDetailed(@RequestBody Map<String, Object> payload) {
        // Lấy các trường bắt buộc từ payload
        String fen = (String) payload.get("fen");
        String playerMove = (String) payload.get("playerMove");
        int moveNumber = payload.containsKey("moveNumber") ? Integer.parseInt(payload.get("moveNumber").toString()) : 0;
        int gameId = payload.containsKey("gameId") ? Integer.parseInt(payload.get("gameId").toString()) : 0;
        int depth = payload.containsKey("depth") ? Integer.parseInt(payload.get("depth").toString()) : 15;

        if (fen == null || fen.trim().isEmpty() || playerMove == null || playerMove.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("FEN and playerMove are required");
        }

        try {
            long startTime = System.currentTimeMillis();

            // Kiểm tra khai cuộc
            boolean autoBookMove = openingBookService.isBookMove(fen, moveNumber);
            String openingName = openingBookService.findOpeningName(fen);

            // 1. Lấy dữ liệu của nước đi tối ưu
            stockfishEngine.sendCommand("position fen " + fen);
            stockfishEngine.sendCommand("go depth " + depth);
            String bestOutput = stockfishEngine.readOutput();
            String bestMove = extractBestMove(bestOutput);
            int bestEvaluation = extractEvaluation(bestOutput);

            // 2. Lấy evaluation của nước đi của người chơi
            stockfishEngine.sendCommand("position fen " + fen + " moves " + playerMove);
            stockfishEngine.sendCommand("go depth " + depth);
            String playerOutput = stockfishEngine.readOutput();
            int playerEvaluation = extractEvaluation(playerOutput);

            // Tính delta
            int delta = bestEvaluation - playerEvaluation;
            String moveCategoryStr;
            if (autoBookMove) {
                moveCategoryStr = "BOOK_MOVE";
            } else if (playerMove.equals(bestMove)) {
                moveCategoryStr = "BEST_MOVE";
            } else {
                int absLoss = Math.abs(delta);
                int ref = Math.abs(bestEvaluation);
                // Áp dụng dynamic thresholds theo thứ tự từ tốt đến xấu
                if (absLoss <= Math.max(10, ref / 100)) {
                    moveCategoryStr = "BRILLIANT_MOVE";
                } else if (absLoss <= Math.max(20, ref / 50)) {
                    moveCategoryStr = "GREAT_MOVE";
                } else if (absLoss <= Math.max(40, ref / 25)) {
                    moveCategoryStr = "GOOD_MOVE";
                } else if (absLoss <= Math.max(60, ref / 15)) {
                    moveCategoryStr = "INACCURACY";
                } else if (absLoss <= Math.max(80, ref / 10)) {
                    moveCategoryStr = "MISTAKE";
                } else {
                    moveCategoryStr = "BLUNDER";
                }
            }

            int totalAnalysisTime = (int) (System.currentTimeMillis() - startTime);

            // Build response
            Map<String, Object> responseBody = new HashMap<>();
            responseBody.put("bestMove", bestMove);
            responseBody.put("bestEvaluation", bestEvaluation);
            responseBody.put("playerEvaluation", playerEvaluation);
            responseBody.put("evaluationDelta", delta);
            responseBody.put("moveCategory", moveCategoryStr);
            responseBody.put("analysisTime", totalAnalysisTime);
            responseBody.put("isBookMove", autoBookMove);
            responseBody.put("openingName", openingName);

            // Lưu vào DB
            AiAnalysis analysis = new AiAnalysis();
            analysis.setGameId(gameId);
            analysis.setMoveNumber(moveNumber);
            analysis.setMove(playerMove);
            analysis.setBestMove(bestMove);
            analysis.setEvaluation(new BigDecimal(bestEvaluation));
            analysis.setEvaluationDelta(new BigDecimal(delta));
            // Chuyển chuỗi thành MoveCategory (enum của bạn phải khớp, ví dụ: BOOK_MOVE, BEST_MOVE, ...)
            analysis.setMoveCategory(MoveCategory.valueOf(moveCategoryStr));
            analysis.setAnalysisTime(totalAnalysisTime);
            aiAnalysisRepository.save(analysis);

            return ResponseEntity.ok(responseBody);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error during detailed analysis: " + e.getMessage());
        }
    }

    // Helper: trích xuất best move từ output
    private String extractBestMove(String output) {
        String[] lines = output.split("\n");
        for (String line : lines) {
            if (line.startsWith("bestmove")) {
                String[] parts = line.split(" ");
                if (parts.length >= 2) {
                    return parts[1];
                }
            }
        }
        return null;
    }

    // Helper: trích xuất evaluation từ output
    private int extractEvaluation(String output) {
        Pattern cpPattern = Pattern.compile("score cp (-?\\d+)");
        Matcher cpMatcher = cpPattern.matcher(output);
        if (cpMatcher.find()) {
            return Integer.parseInt(cpMatcher.group(1));
        }
        Pattern matePattern = Pattern.compile("score mate (-?\\d+)");
        Matcher mateMatcher = matePattern.matcher(output);
        if (mateMatcher.find()) {
            int mate = Integer.parseInt(mateMatcher.group(1));
            return mate > 0 ? 10000 - mate : -10000 - mate;
        }
        return 0;
    }
}
