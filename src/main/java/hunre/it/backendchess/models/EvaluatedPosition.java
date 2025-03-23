package hunre.it.backendchess.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EvaluatedPosition {
    private String fen;
    private String classification;
    private String worker;
    private String opening; // Tên khai cuộc nếu có
    // Có thể bổ sung thêm các trường khác nếu cần (ví dụ: move, topLines, v.v.)
}
