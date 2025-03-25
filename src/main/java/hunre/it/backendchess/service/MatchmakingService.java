package hunre.it.backendchess.service;

import hunre.it.backendchess.models.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Queue;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.util.HashMap;

@Service
public class MatchmakingService {

    // Map chứa hàng đợi theo gameMode
    // Ví dụ: key "standard" -> hàng đợi người chơi chế độ tiêu chuẩn,...
    private final Map<String, Queue<User>> waitingUsersByMode = new ConcurrentHashMap<>();

    private final SimpMessagingTemplate messagingTemplate;

    // In-memory match store: mapping từ matchId đến matchInfo (bao gồm white, black, matchId)
    private static final Map<String, Map<String, String>> matchStore = new ConcurrentHashMap<>();

    @Autowired
    public MatchmakingService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
        // Khởi tạo hàng đợi cho các chế độ
        waitingUsersByMode.put("standard", new ConcurrentLinkedQueue<>());
        waitingUsersByMode.put("rapid", new ConcurrentLinkedQueue<>());
        waitingUsersByMode.put("bullet", new ConcurrentLinkedQueue<>());
    }

    /**
     * Người chơi tham gia hàng đợi ghép đôi.
     * Yêu cầu requestBody cần có thông tin gameMode.
     */
    @PostMapping("/join")
    public ResponseEntity<String> joinQueue(@RequestBody User user) {
        // Giả sử User có thêm thuộc tính gameMode
        String mode = user.getGameMode();
        if(mode == null || !waitingUsersByMode.containsKey(mode)) {
            return ResponseEntity.badRequest().body("Game mode không hợp lệ");
        }

        Queue<User> queue = waitingUsersByMode.get(mode);
        User opponent = queue.poll();
        if (opponent != null) {
            createMatch(opponent, user);
        } else {
            queue.offer(user);
        }
        return ResponseEntity.ok("Đã đăng ký tìm trận trong chế độ " + mode);
    }

    private void createMatch(User user1, User user2) {
        // Phân định màu ngẫu nhiên
        boolean assignUser1White = new Random().nextBoolean();
        String whiteUsername = assignUser1White ? user1.getUsername() : user2.getUsername();
        String blackUsername = assignUser1White ? user2.getUsername() : user1.getUsername();

        // Tạo matchId
        String matchId = UUID.randomUUID().toString();

        // Tạo matchInfo chứa thông tin về match
        Map<String, String> matchInfo = new HashMap<>();
        matchInfo.put("white", whiteUsername);
        matchInfo.put("black", blackUsername);
        matchInfo.put("matchId", matchId);
        // Có thể lưu thêm thông tin gameMode nếu cần: matchInfo.put("mode", user1.getGameMode());

        // Lưu matchInfo vào store
        matchStore.put(matchId, matchInfo);

        // Gửi thông báo match cho cả 2 người chơi (ở đây dùng topic để test)
        messagingTemplate.convertAndSend("/topic/match", matchInfo);

        System.out.println("Creating match between " + user1.getUsername() + " and " + user2.getUsername() + ", matchId: " + matchId);
    }

    // Phương thức truy xuất match info theo matchId
    public static Map<String, String> getMatchInfo(String matchId) {
        return matchStore.get(matchId);
    }

    public void cancelQueue(User user) {
        // Xóa người chơi khỏi hàng đợi tương ứng với gameMode của họ
        String mode = user.getGameMode();
        if(mode != null && waitingUsersByMode.containsKey(mode)) {
            waitingUsersByMode.get(mode).removeIf(u -> u.getUsername().equals(user.getUsername()));
        }
    }
}
