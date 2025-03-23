package hunre.it.backendchess.controller;

import hunre.it.backendchess.DTO.SurrenderData;
import hunre.it.backendchess.service.MatchmakingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.Map;

@Controller
public class SurrenderController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/surrender")
    public void processSurrender(SurrenderData surrenderData) {
        String sender = surrenderData.getSender();
        System.out.println("Received surrender from " + sender);

        // Lấy matchInfo từ store (MatchmakingService)
        Map<String, String> matchInfo = MatchmakingService.getMatchInfo(surrenderData.getMatchId());
        if (matchInfo != null) {
            String white = matchInfo.get("white");
            String black = matchInfo.get("black");
            String opponent = null;
            if (sender.equals(white)) {
                opponent = black;
            } else if (sender.equals(black)) {
                opponent = white;
            }
            if (opponent != null) {
                String message = "Đối thủ đã đầu hàng, bạn chiến thắng!";
                messagingTemplate.convertAndSendToUser(opponent, "/queue/surrender", message);
                System.out.println("Forwarded surrender notification to " + opponent);
            } else {
                System.out.println("Opponent not found for surrender from " + sender);
            }
        } else {
            System.out.println("Match info not found for matchId: " + surrenderData.getMatchId());
        }
    }
}
