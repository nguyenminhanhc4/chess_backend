package hunre.it.backendchess.repository;

import hunre.it.backendchess.models.Game;
import hunre.it.backendchess.models.GameResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface GameRepository extends JpaRepository<Game, Long> {

    List<Game> findByPlayerUsernameOrderByCreatedAtDesc(String playerUsername);

    boolean existsByPlayerUsernameAndResult(String playerUsername, GameResult result);
    long countByPlayerUsername(String username);
    long countByPlayerUsernameAndResult(String username, GameResult result);

    List<Game> findByMatchId(UUID matchId);

    boolean existsByMatchId(UUID matchId);
    long countByUserId(Long userId);
    long countByUserIdAndResult(Long userId, GameResult result);

    default String findOpponentByMatchId(UUID matchId, String sender) {
        return findByMatchId(matchId) // Trả về List<Game>
                .stream() // Chuyển List thành Stream
                .filter(game -> !game.getPlayerUsername().equals(sender)) // Lọc game của đối thủ
                .findFirst() // Lấy game đầu tiên thỏa mãn
                .map(Game::getPlayerUsername) // Trích xuất username
                .orElse("UNKNOWN"); // Mặc định nếu không tìm thấy
    }
}

