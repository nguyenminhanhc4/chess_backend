package hunre.it.backendchess.DTO;

public class GameOverMessage {
    private String sender;
    private String matchId; // thay đổi từ Long sang String
    private String result;

    public GameOverMessage() {
    }

    public GameOverMessage(String sender, String matchId, String result) {
        this.sender = sender;
        this.matchId = matchId;
        this.result = result;
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
}
