package hunre.it.backendchess.repository;

import hunre.it.backendchess.models.AiAnalysis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AiAnalysisRepository extends JpaRepository<AiAnalysis, Integer> {
    List<AiAnalysis> findByGameIdOrderByMoveNumberAsc(int gameId);
}