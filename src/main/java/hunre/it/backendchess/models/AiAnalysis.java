package hunre.it.backendchess.models;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "ai_analysis")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AiAnalysis {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "game_id", nullable = false)
    private Integer gameId;

    @Column(name = "move_number", nullable = false)
    private Integer moveNumber;

    @Column(name = "move", nullable = false, length = 20)
    private String move;

    @Column(name = "best_move", length = 20)
    private String bestMove;

    @Column(name = "evaluation", precision = 7, scale = 3)
    private BigDecimal evaluation;

    @Column(name = "evaluation_delta", precision = 7, scale = 3)
    private BigDecimal evaluationDelta;

    @Enumerated(EnumType.STRING)
    @Column(name = "move_category", columnDefinition = "ENUM('blunder','missed_move','mistake','inaccuracy','book_move','good_move','great_move','best_move','brilliant_move','genius_move') DEFAULT 'good_move'")
    private MoveCategory moveCategory;

    @Column(name = "analysis_time")
    private Integer analysisTime;

    @Column(name = "created_at", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdAt = LocalDateTime.now();
}
