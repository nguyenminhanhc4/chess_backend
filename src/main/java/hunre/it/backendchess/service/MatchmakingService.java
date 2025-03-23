package hunre.it.backendchess.service;

import hunre.it.backendchess.models.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Queue;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.HashMap;
import java.util.Map;

@Service
public class    MatchmakingService {

    // Hàng đợi người chơi chờ ghép đôi
    private final Queue<User> waitingUsers = new ConcurrentLinkedQueue<>();
    private final SimpMessagingTemplate messagingTemplate;

    // In-memory match store: mapping từ matchId đến matchInfo (bao gồm white, black, matchId)
    private static final Map<String, Map<String, String>> matchStore = new ConcurrentHashMap<>();

    @Autowired
    public MatchmakingService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void joinQueue(User user) {
        User opponent = waitingUsers.poll();
        if (opponent != null) {
            createMatch(opponent, user);
        } else {
            waitingUsers.offer(user);
        }
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
        waitingUsers.removeIf(u -> u.getUsername().equals(user.getUsername()));
    }

}
