package hunre.it.backendchess.controller;

import hunre.it.backendchess.DTO.MoveData;
import hunre.it.backendchess.service.MatchmakingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.Map;

@Controller
public class MoveController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/move")
    public void processMove(MoveData moveData) {
        // Lấy sender từ payload
        String sender = moveData.getSender();
        System.out.println("Received move from " + sender + ": "
                + moveData.getFrom() + "->" + moveData.getTo());

        // Xác định đối thủ dựa trên matchId
        String opponentUsername = determineOpponent(sender, moveData.getMatchId());

        if (opponentUsername != null) {
            messagingTemplate.convertAndSendToUser(opponentUsername, "/queue/move", moveData);
            System.out.println("Forwarded move to " + opponentUsername);
        } else {
            System.out.println("Opponent not found for " + sender);
        }
    }

    private String determineOpponent(String sender, String matchId) {
        // Lấy matchInfo từ MatchmakingService
        Map<String, String> matchInfo = MatchmakingService.getMatchInfo(matchId);
        if (matchInfo != null) {
            String white = matchInfo.get("white");
            String black = matchInfo.get("black");
            // Nếu sender là người chơi bên trắng, đối thủ là bên đen; ngược lại, nếu sender là bên đen, đối thủ là bên trắng.
            if (sender.equals(white)) {
                return black;
            } else if (sender.equals(black)) {
                return white;
            }
        }
        return null;
    }

}
