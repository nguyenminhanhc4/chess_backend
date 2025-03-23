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

    Optional<Game> findByMatchId(UUID matchId);

    default String findOpponentByMatchId(UUID matchId) {
        return findByMatchId(matchId)
                .map(Game::getOpponent)
                .orElse("UNKNOWN");
    }
}

