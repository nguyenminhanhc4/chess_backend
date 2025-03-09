package hunre.it.backendchess.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum GameResult {
    WIN("WIN"),
    LOSE("LOSE"),
    DRAW("DRAW"),
    ABORT("ABORT");

    private final String value;

    GameResult(String value) {
        this.value = value;
    }

    @JsonValue // Khi trả về JSON, giữ nguyên định dạng gốc
    public String getValue() {
        return value;
    }

    @JsonCreator // Khi nhận từ API, chuyển đổi linh hoạt giá trị
    public static GameResult fromString(String value) {
        if (value == null) {
            throw new IllegalArgumentException("GameResult cannot be null");
        }
        return switch (value.toUpperCase()) { // Chấp nhận mọi kiểu chữ
            case "WIN" -> WIN;
            case "LOSE" -> LOSE;
            case "DRAW" -> DRAW;
            case "ABORT" -> ABORT;
            default -> throw new IllegalArgumentException("Invalid GameResult: " + value);
        };
    }
}
