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
        // Khởi tạo hàng đợi cho các chế độ "Siêu Chớp"
        waitingUsersByMode.put("1+0", new ConcurrentLinkedQueue<>());
        waitingUsersByMode.put("1+1", new ConcurrentLinkedQueue<>());
        waitingUsersByMode.put("2+1", new ConcurrentLinkedQueue<>());

        // Khởi tạo hàng đợi cho các chế độ "Chớp"
        waitingUsersByMode.put("3+0", new ConcurrentLinkedQueue<>());
        waitingUsersByMode.put("3+2", new ConcurrentLinkedQueue<>());
        waitingUsersByMode.put("5+0", new ConcurrentLinkedQueue<>());

        // Khởi tạo hàng đợi cho các chế độ "Cờ Chớp"
        waitingUsersByMode.put("10+0", new ConcurrentLinkedQueue<>());
        waitingUsersByMode.put("15+10", new ConcurrentLinkedQueue<>());
        waitingUsersByMode.put("30+0", new ConcurrentLinkedQueue<>());

        // Khởi tạo hàng đợi cho các chế độ "Hàng Ngày"
        waitingUsersByMode.put("1d", new ConcurrentLinkedQueue<>());
        waitingUsersByMode.put("3d", new ConcurrentLinkedQueue<>());
        waitingUsersByMode.put("7d", new ConcurrentLinkedQueue<>());
    }

    /**
     * Người chơi tham gia hàng đợi ghép đôi.
     * Yêu cầu requestBody cần có thông tin gameMode.
     */
    @PostMapping("/join")
    public ResponseEntity<String> joinQueue(@RequestBody User user) {
        System.out.println("joinQueue called with user: " + user);
        String mode = user.getGameMode();
        System.out.println("User game mode: " + mode);
        if(mode == null || !waitingUsersByMode.containsKey(mode)) {
            System.out.println("Invalid game mode: " + mode);
            return ResponseEntity.badRequest().body("Game mode không hợp lệ");
        }

        Queue<User> queue = waitingUsersByMode.get(mode);
        System.out.println("Queue size for mode '" + mode + "' before polling: " + queue.size());
        User opponent = queue.poll();
        if (opponent != null) {
            System.out.println("Found opponent: " + opponent.getUsername() + " for user: " + user.getUsername());
            createMatch(opponent, user);
        } else {
            System.out.println("No opponent found, adding user " + user.getUsername() + " to queue");
            queue.offer(user);
            System.out.println("Queue size for mode '" + mode + "' after offer: " + queue.size());
        }
        return ResponseEntity.ok("Đã đăng ký tìm trận trong chế độ " + mode);
    }

    private void createMatch(User user1, User user2) {
        boolean assignUser1White = new Random().nextBoolean();
        String whiteUsername = assignUser1White ? user1.getUsername() : user2.getUsername();
        String blackUsername = assignUser1White ? user2.getUsername() : user1.getUsername();

        String matchId = UUID.randomUUID().toString();

        Map<String, String> matchInfo = new HashMap<>();
        matchInfo.put("white", whiteUsername);
        matchInfo.put("black", blackUsername);
        matchInfo.put("matchId", matchId);

        matchStore.put(matchId, matchInfo);

        // Log thông tin match trước khi gửi thông báo
        System.out.println("Creating match between " + user1.getUsername() + " and " + user2.getUsername() + ", matchId: " + matchId);
        System.out.println("Match info: " + matchInfo);

        messagingTemplate.convertAndSend("/topic/match", matchInfo);
        System.out.println("Match info sent to topic /topic/match");
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
