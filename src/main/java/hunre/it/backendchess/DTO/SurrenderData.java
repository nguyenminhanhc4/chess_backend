package hunre.it.backendchess.DTO;

public class SurrenderData {
    private String sender;
    private String matchId;

    public SurrenderData() {}

    public SurrenderData(String sender, String matchId) {
        this.sender = sender;
        this.matchId = matchId;
    }

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
}
