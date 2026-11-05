package com.resumatch.optimizer;

import com.resumatch.project.StudentProject;
import com.resumatch.project.StudentProjectRepository;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ResumeOptimizerServiceTest {

    private final StudentProjectRepository repository = mock(StudentProjectRepository.class);
    private final ResumeOptimizerService service = new ResumeOptimizerService(repository);

    @Test
    void ranksByKeywordRelevanceAndRecencyAndExcludesUnmatchedProjects() {
        StudentProject recentPartialMatch = project("Java REST API", "Built backend services",
                Set.of("Java", "REST"), LocalDate.now().minusMonths(3));
        StudentProject fullMatch = project("Java REST Spring Boot API",
                "Built a Java Spring Boot REST API", Set.of("Java", "REST", "Spring Boot"),
                LocalDate.now().minusYears(4));
        StudentProject unrelated = project("Art Portfolio", "Digital illustration",
                Set.of("Design"), LocalDate.now());
        when(repository.findAll()).thenReturn(List.of(recentPartialMatch, fullMatch, unrelated));

        OptimizeResumeResponse response = service.optimize("Java Spring Boot REST API");

        assertEquals(2, response.recommendedProjects().size());
        assertEquals("Java REST Spring Boot API", response.recommendedProjects().getFirst().title());
        assertEquals("Java REST API", response.recommendedProjects().get(1).title());
        assertEquals(88.57, response.recommendedProjects().getFirst().score());
    }

    @Test
    void returnsNoRecommendationsWhenJobDescriptionHasNoMatchingKeywords() {
        when(repository.findAll()).thenReturn(List.of(project("Java API", "Spring service",
                Set.of("Java"), LocalDate.now())));

        OptimizeResumeResponse response = service.optimize("financial accounting");

        assertEquals(List.of(), response.recommendedProjects());
    }

    private StudentProject project(String title, String description, Set<String> tags, LocalDate endDate) {
        return new StudentProject(title, description, tags, endDate.minusMonths(6), endDate);
    }
}
