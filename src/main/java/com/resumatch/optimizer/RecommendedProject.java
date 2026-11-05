package com.resumatch.optimizer;

import java.time.LocalDate;
import java.util.Set;

public record RecommendedProject(
        Long id,
        String title,
        String description,
        Set<String> skillTags,
        LocalDate startDate,
        LocalDate endDate,
        double score
) {
}
