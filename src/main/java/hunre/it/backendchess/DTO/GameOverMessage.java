package hunre.it.backendchess.DTO;

public class GameOverMessage {
    private String sender;
    private String matchId; // thay đổi từ Long sang String
    private String result;
    private String opponent;  // Thêm trường opponent

    public GameOverMessage() {
    }

    public GameOverMessage(String sender, String matchId, String result, String opponent) {
        this.sender = sender;
        this.matchId = matchId;
        this.result = result;
        this.opponent = opponent;  // Khởi tạo opponent
    }

    // Getters and Setters
    public String getSender() {
        return sender;
    }
    public void setSender(String sender) {
        this.sender = sender;
    }
    public String getMatchId() {
        return matchId;
    }
    public void setMatchId(String matchId) {
        this.matchId = matchId;
    }
    public String getResult() {
        return result;
    }
    public void setResult(String result) {
        this.result = result;
    }

    public String getOpponent() {  // Thêm getter cho opponent
        return opponent;
    }
    public void setOpponent(String opponent) {  // Thêm setter cho opponent
        this.opponent = opponent;
    }
}
