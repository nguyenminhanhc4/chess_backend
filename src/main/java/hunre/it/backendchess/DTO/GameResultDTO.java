package hunre.it.backendchess.DTO;
import lombok.Getter;
import lombok.Setter;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class GameResultDTO {
    private String playerId;
    private int currentElo;
    private String result; // "WIN", "LOSE", "DRAW"
}

