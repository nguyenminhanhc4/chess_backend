package hunre.it.backendchess.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Report {
    private Map<String, Double> accuracies;
    private Map<String, Map<String, Integer>> classifications;
    private List<EvaluatedPosition> positions;
}
