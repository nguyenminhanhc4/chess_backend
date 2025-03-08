package hunre.it.backendchess.controller;

import hunre.it.backendchess.engine.StockfishEngine;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

@RestController
@RequestMapping("/api/engine")
public class EngineController {

    private final StockfishEngine stockfishEngine = new StockfishEngine();

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

    // Endpoint cập nhật vị trí (chuỗi moves) cho Stockfish
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

    // Endpoint lấy nước đi của máy, nhận tham số độ khó qua query string
    @GetMapping("/go")
    public ResponseEntity<String> getEngineMove(@RequestParam(defaultValue = "medium") String difficulty) {
        try {
            int depth;
            // Điều chỉnh độ sâu dựa theo độ khó
            switch (difficulty.toLowerCase()) {
                case "easy":
                    depth = 2;
                    break;
                case "hard":
                    depth = 20;
                    break;
                case "medium":
                default:
                    depth = 5;
                    break;
            }
            // Gửi lệnh tính nước đi với độ sâu tương ứng
            stockfishEngine.sendCommand("go depth " + depth);
            String output = stockfishEngine.readOutput();
            return ResponseEntity.ok(output);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi khi lấy nước đi của engine: " + e.getMessage());
        }
    }
}
