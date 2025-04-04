package hunre.it.backendchess.DTO;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserStats {
    private long totalGames;
    private double winRate;
    private double lossRate;
    private double drawRate;
}
