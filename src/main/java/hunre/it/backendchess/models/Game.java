package hunre.it.backendchess.models;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "games")
public class Game {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String playerUsername;
    private String opponent;

    @Enumerated(EnumType.STRING)
    private OpponentType opponentType; // "human" hoặc "bot"

    @Column(columnDefinition = "TEXT")
    private String moves; // Lưu chuỗi nước đi (UCI format)

    private String finalFen;

    @Enumerated(EnumType.STRING)
    private GameResult result; // "win", "lose", "draw", "abort"

    private Integer whiteTimeRemaining; // NULL nếu chơi với bot
    private Integer blackTimeRemaining; // NULL nếu chơi với bot

    private LocalDateTime createdAt = LocalDateTime.now();
}
