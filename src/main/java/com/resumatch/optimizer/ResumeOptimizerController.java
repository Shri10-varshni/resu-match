package com.resumatch.optimizer;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class ResumeOptimizerController {

    private final ResumeOptimizerService optimizerService;

    public ResumeOptimizerController(ResumeOptimizerService optimizerService) {
        this.optimizerService = optimizerService;
    }

    @PostMapping("/optimize-resume")
    public OptimizeResumeResponse optimize(@Valid @RequestBody OptimizeResumeRequest request) {
        return optimizerService.optimize(request.jobDescription());
    }
}
