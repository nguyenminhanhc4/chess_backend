package hunre.it.backendchess.repository;

import hunre.it.backendchess.models.Game;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface GameRepository extends JpaRepository<Game, Long> {
    List<Game> findByPlayerUsernameOrderByCreatedAtDesc(String playerUsername);
}
