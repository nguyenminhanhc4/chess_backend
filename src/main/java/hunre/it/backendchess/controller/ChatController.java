package hunre.it.backendchess.controller;

import hunre.it.backendchess.DTO.ChatMessage;
import hunre.it.backendchess.service.MatchmakingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.Map;

@Controller
public class ChatController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/chat")
    public void processChat(ChatMessage chatMessage) {
        String sender = chatMessage.getSender();
        System.out.println("Received chat from " + sender + ": " + chatMessage.getText());

        // Nếu đây là phản hồi xin hòa và accepted là true, gửi đến cả 2 bên
        if ("draw_response".equals(chatMessage.getMsgType()) && chatMessage.isAccepted()) {
            Map<String, String> matchInfo = MatchmakingService.getMatchInfo(chatMessage.getMatchId());
            if (matchInfo != null) {
                String white = matchInfo.get("white");
                String black = matchInfo.get("black");
                messagingTemplate.convertAndSendToUser(white, "/queue/chat", chatMessage);
                messagingTemplate.convertAndSendToUser(black, "/queue/chat", chatMessage);
                System.out.println("Broadcasted draw response to both players: " + white + ", " + black);
            }
        } else {
            // Nếu không phải draw response accepted, chỉ chuyển tiếp tin nhắn đến đối thủ
            Map<String, String> matchInfo = MatchmakingService.getMatchInfo(chatMessage.getMatchId());
            String opponentUsername = null;
            if (matchInfo != null) {
                String white = matchInfo.get("white");
                String black = matchInfo.get("black");
                if (sender.equals(white)) {
                    opponentUsername = black;
                } else if (sender.equals(black)) {
                    opponentUsername = white;
                }
            }
            if (opponentUsername != null) {
                messagingTemplate.convertAndSendToUser(opponentUsername, "/queue/chat", chatMessage);
                System.out.println("Forwarded chat to " + opponentUsername);
            } else {
                System.out.println("Opponent not found for chat message from " + sender);
            }
        }
    }
}
