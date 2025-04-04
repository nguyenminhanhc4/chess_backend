package hunre.it.backendchess.models;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "games")
public class Game {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "BINARY(16)")
    private UUID matchId; // Thêm trường matchId

    private String playerUsername;
    private String opponent;

    @Enumerated(EnumType.STRING)
    private OpponentType opponentType; // "human" hoặc "bot"

    @Column(columnDefinition = "TEXT")
    private String moves; // Lưu chuỗi nước đi (UCI format)

    private String finalFen;

    @Enumerated(EnumType.STRING)
    private GameResult result; // "win", "lose", "draw", "abort"

    private Integer whiteTimeRemaining;
    private Integer blackTimeRemaining;

    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "user_id")
    private Long userId;
}
