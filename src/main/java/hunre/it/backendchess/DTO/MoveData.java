package hunre.it.backendchess.DTO;

public class MoveData {
    private String sender; // Tên người gửi (username)
    private String from;   // Ô xuất phát (ví dụ: "e2")
    private String to;     // Ô đích (ví dụ: "e4")
    private String promotion; // Loại quân phong cấp, nếu có
    private String matchId;   // ID của ván đấu, nếu dùng

    // Constructor không đối số
    public MoveData() {
    }

    // Constructor có đối số
    public MoveData(String sender, String from, String to, String promotion, String matchId) {
        this.sender = sender;
        this.from = from;
        this.to = to;
        this.promotion = promotion;
        this.matchId = matchId;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getPromotion() {
        return promotion;
    }

    public void setPromotion(String promotion) {
        this.promotion = promotion;
    }

    public String getMatchId() {
        return matchId;
    }

    public void setMatchId(String matchId) {
        this.matchId = matchId;
    }
}
