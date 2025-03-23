package hunre.it.backendchess.controller;

import hunre.it.backendchess.models.User;
import hunre.it.backendchess.service.MatchmakingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/matchmaking")
public class MatchmakingController {

    private final MatchmakingService matchmakingService;

    @Autowired
    public MatchmakingController(MatchmakingService matchmakingService) {
        this.matchmakingService = matchmakingService;
    }

    /**
     * Endpoint cho người chơi tham gia hàng đợi ghép đôi.
     * Frontend gửi request POST với thông tin người chơi.
     */
    @PostMapping("/join")
    public ResponseEntity<String> joinQueue(@RequestBody User user) {
        matchmakingService.joinQueue(user);
        return ResponseEntity.ok("Đã đăng ký tìm trận");
    }

    @PostMapping("/cancel")
    public ResponseEntity<String> cancelQueue(@RequestBody User user) {
        matchmakingService.cancelQueue(user);
        return ResponseEntity.ok("Đã hủy tìm trận");
    }

}
