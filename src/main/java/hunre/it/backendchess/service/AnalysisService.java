package hunre.it.backendchess.service;

import hunre.it.backendchess.models.EvaluatedPosition;
import hunre.it.backendchess.models.Opening;
import hunre.it.backendchess.models.Report;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AnalysisService {

    private final OpeningBookService openingBookService;

    public AnalysisService(OpeningBookService openingBookService) {
        this.openingBookService = openingBookService;
    }

    /**
     * Phân tích danh sách EvaluatedPosition và tích hợp thông tin khai cuộc.
     */
    public Report analyzePositions(List<EvaluatedPosition> positions) {
        // Gán tên khai cuộc cho mỗi vị trí nếu có
        for (EvaluatedPosition pos : positions) {
            String openingName = openingBookService.findOpeningName(pos.getFen());
            if (openingName != null) {
                pos.setOpening(openingName);
                // Nếu muốn, thay đổi classification thành BOOK
                pos.setClassification("BOOK");
            }
        }

        // Ví dụ tính toán accuracy và thống kê classification
        double whiteTotal = 0, whiteCount = 0;
        double blackTotal = 0, blackCount = 0;
        Map<String, Map<String, Integer>> classifications = new HashMap<>();
        classifications.put("white", new HashMap<>());
        classifications.put("black", new HashMap<>());

        for (int i = 0; i < positions.size(); i++) {
            EvaluatedPosition pos = positions.get(i);
            String moveColour = (i % 2 == 0) ? "black" : "white"; // Luân phiên trắng-đen

            // Sửa đúng logic
            int value = getClassificationValue(pos.getClassification());

            if ("white".equals(moveColour)) {
                whiteTotal += value;
                whiteCount++;
            } else {
                blackTotal += value;
                blackCount++;
            }
            System.out.println("Move Classification: " + pos.getClassification() + ", Color: " + moveColour);

            classifications.computeIfAbsent(moveColour, k -> new HashMap<>());
            Map<String, Integer> map = classifications.get(moveColour);
            map.put(pos.getClassification(), map.getOrDefault(pos.getClassification(), 0) + 1);
        }
        System.out.println("White Total: " + whiteTotal + ", White Count: " + whiteCount);
        System.out.println("Black Total: " + blackTotal + ", Black Count: " + blackCount);

        // Cập nhật công thức tính Accuracy với hệ số phạt x2
        double whitePenalty = Math.abs(Math.min(0, whiteTotal)) * 2;  // Nhân hệ số để phạt lỗi mạnh hơn
        double whiteScore = whiteTotal - whitePenalty;
        double whiteAccuracy = whiteCount > 0 ? Math.min(100, Math.max(1, (whiteScore / (whiteTotal + whitePenalty) * 100))) : 0;

        double blackPenalty = Math.abs(Math.min(0, blackTotal)) * 2;
        double blackScore = blackTotal - blackPenalty;
        double blackAccuracy = blackCount > 0 ? Math.min(100, Math.max(1, (blackScore / (blackTotal + blackPenalty) * 100))) : 0;

        Report report = new Report();
        report.setAccuracies(Map.of("white", whiteAccuracy, "black", blackAccuracy));
        report.setClassifications(classifications);
        report.setPositions(positions);
        return report;
    }

    private int getClassificationValue(String classification) {
        Map<String, Integer> values = Map.of(
                "BRILLIANT", 10,   // Nước đi cực kỳ xuất sắc
                "GREAT_MOVE", 8,   // Nước đi rất tốt
                "BEST_MOVE", 7,    // Nước đi tốt nhất
                "EXCELLENT", 6,    // Nước đi tuyệt vời, gần mức BEST_MOVE
                "GOOD_MOVE", 5,    // Nước đi ổn nhưng không hoàn hảo
                "INACCURACY", -2,  // Nước đi không chính xác (trừ điểm)
                "MISTAKE", -4,     // Nước đi sai lầm nghiêm trọng hơn inaccuracy (trừ nhiều hơn)
                "BLUNDER", -6,     // Nước đi cực kỳ tệ, ảnh hưởng lớn đến ván đấu
                "BOOK", 5
        );
        int value = values.getOrDefault(classification, 0);
        System.out.println("Move: " + classification + ", Value: " + value);
        return value;
    }
}