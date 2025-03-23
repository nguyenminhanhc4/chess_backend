package hunre.it.backendchess.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import hunre.it.backendchess.models.Opening;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.InputStream;
import java.util.List;

@Service
public class OpeningBookService {

    private List<Opening> openings;

    @PostConstruct
    public void init() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            InputStream is = getClass().getResourceAsStream("/openings.json");
            openings = mapper.readValue(is, new TypeReference<List<Opening>>() {});
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * So sánh phần piece placement của FEN hiện tại với khai cuộc từ file JSON.
     * Trả về tên khai cuộc nếu khớp chính xác; nếu không, trả về null.
     */
    public String findOpeningName(String fen) {
        if (openings == null || fen == null) return null;
        // Tách phần piece placement (phần trước dấu cách đầu tiên)
        String piecePlacement = fen.split(" ")[0];
        for (Opening opening : openings) {
            String openingPiecePlacement = opening.getFen().split(" ")[0];
            if (piecePlacement.equals(openingPiecePlacement)) {
                return opening.getName();
            }
        }
        return null;
    }

    /**
     * Xác định book move dựa trên FEN và số nước đi (moveNumber).
     * Chỉ xem là book move nếu moveNumber ≤ threshold (ví dụ 10) và khai cuộc khớp chính xác.
     */
    public boolean isBookMove(String fen, int moveNumber) {
        // Nếu số nước đi vượt quá giới hạn khai cuộc (ví dụ 10), không coi là book move.
        if (moveNumber > 10) {
            return false;
        }
        return findOpeningName(fen) != null;
    }
}
