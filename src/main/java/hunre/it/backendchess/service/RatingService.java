package hunre.it.backendchess.service;

import hunre.it.backendchess.models.GameResult;
import hunre.it.backendchess.models.User;
import hunre.it.backendchess.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RatingService {

    private final UserRepository userRepository;
    private static final Logger logger = LoggerFactory.getLogger(RatingService.class);
    private static final int K = 32;

    public int calculateElo(int currentElo, int opponentElo, double score) {
        double expectedScore = 1 / (1 + Math.pow(10, (opponentElo - currentElo) / 400.0));
        return (int) (currentElo + K * (score - expectedScore));
    }

    @Retryable(
            maxAttempts = 3,
            backoff = @Backoff(delay = 100),
            retryFor = {JpaSystemException.class, InterruptedException.class}
    )
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateElo(String playerUsername, String opponentUsername, GameResult result) {
        logger.info("⚡ Bắt đầu cập nhật ELO - Player: {}, Opponent: {}, Result: {}",
                playerUsername, opponentUsername, result);

        try {
            // 1. Sắp xếp username
            List<String> sortedUsernames = Arrays.asList(playerUsername, opponentUsername);
            Collections.sort(sortedUsernames);
            logger.debug("🔀 Username đã sắp xếp: {}", sortedUsernames);

            // 2. Khóa user theo thứ tự
            User firstUser = userRepository.findByUsernameWithLock(sortedUsernames.get(0))
                    .orElseThrow(() -> new RuntimeException("User not found: " + sortedUsernames.get(0)));
            User secondUser = userRepository.findByUsernameWithLock(sortedUsernames.get(1))
                    .orElseThrow(() -> new RuntimeException("User not found: " + sortedUsernames.get(1)));
            logger.info("🔒 Đã khóa user: {} (ELO: {}) và {} (ELO: {})",
                    firstUser.getUsername(), firstUser.getRating(),
                    secondUser.getUsername(), secondUser.getRating());

            // 3. Xác định người thắng và tính toán score
            String winner = (result == GameResult.WIN) ? playerUsername : opponentUsername;
            boolean isWinnerFirst = sortedUsernames.get(0).equals(winner);
            double score = isWinnerFirst ? 1.0 : 0.0;
            logger.debug("🎯 Score được sử dụng: {}", score);

            // 4. Tính toán ELO
            int newFirstUserElo = calculateElo(firstUser.getRating(), secondUser.getRating(), score);
            int newSecondUserElo = calculateElo(secondUser.getRating(), firstUser.getRating(), 1 - score);
            logger.info("🧮 ELO mới - {}: {} → {}, {}: {} → {}",
                    firstUser.getUsername(), firstUser.getRating(), newFirstUserElo,
                    secondUser.getUsername(), secondUser.getRating(), newSecondUserElo);

            // 5. Cập nhật atomic
            userRepository.updateBothRatings(
                    firstUser.getUsername(), newFirstUserElo,
                    secondUser.getUsername(), newSecondUserElo
            );
            logger.info("✅ Cập nhật ELO thành công!");

        } catch (Exception e) {
            logger.error("⚠ Lỗi khi cập nhật ELO: {}", e.getMessage(), e);
            throw e;
        }
    }
}