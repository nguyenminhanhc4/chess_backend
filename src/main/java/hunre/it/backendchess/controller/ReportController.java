package hunre.it.backendchess.controller;

import hunre.it.backendchess.models.Report;
import hunre.it.backendchess.models.EvaluatedPosition;
import hunre.it.backendchess.service.AnalysisService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/report")
@RequiredArgsConstructor
public class ReportController {

    private final AnalysisService analysisService;

    @PostMapping("/analyze")
    public ResponseEntity<Report> analyzeGame(@RequestBody List<EvaluatedPosition> positions) {
        Report report = analysisService.analyzePositions(positions);
        return ResponseEntity.ok(report);
    }
}

