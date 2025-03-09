package hunre.it.backendchess.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum OpponentType {
    HUMAN("HUMAN"),
    BOT("BOT");

    private final String value;

    OpponentType(String value) {
        this.value = value; // Không chuyển thành chữ thường
    }

    @JsonValue // Khi trả về JSON, nó sẽ giữ nguyên chữ hoa/thường
    public String getValue() {
        return value;
    }

    @JsonCreator // Khi nhận từ API, sẽ giữ nguyên giá trị
    public static OpponentType fromString(String value) {
        if (value == null) {
            throw new IllegalArgumentException("OpponentType cannot be null");
        }
        return switch (value) { // Giữ nguyên chữ hoa/thường từ request
            case "HUMAN", "human", "HuMaN" -> HUMAN;
            case "BOT", "bot", "BoT" -> BOT;
            default -> throw new IllegalArgumentException("Invalid OpponentType: " + value);
        };
    }
}
