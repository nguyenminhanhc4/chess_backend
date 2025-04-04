package hunre.it.backendchess.repository;

import hunre.it.backendchess.models.User;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByUsername(String username);

    Boolean existsByEmail(String email);
    Boolean existsByUsername(String username);

    // Khóa pessimistic lock khi đọc user
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT u FROM User u WHERE u.username = :username")
    Optional<User> findByUsernameWithLock(String username);

    // Native query cho cập nhật atomic
    @Modifying
    @Query(value = """
        UPDATE users 
        SET rating = CASE 
            WHEN username = :player THEN :playerElo 
            ELSE :opponentElo 
        END 
        WHERE username IN (:player, :opponent)
    """, nativeQuery = true)
    void updateBothRatings(
            @Param("player") String player,
            @Param("playerElo") int playerElo,
            @Param("opponent") String opponent,
            @Param("opponentElo") int opponentElo
    );
}
