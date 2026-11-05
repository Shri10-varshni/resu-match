package com.resumatch.optimizer;

import com.resumatch.project.StudentProject;
import com.resumatch.project.StudentProjectRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class ResumeOptimizerService {

    private static final double RELEVANCE_WEIGHT = 0.8;
    private static final double RECENCY_WEIGHT = 0.2;
    private static final double RECENCY_HALF_LIFE_YEARS = 3.0;
    private static final Set<String> STOP_WORDS = Set.of(
            "a", "an", "and", "are", "as", "at", "be", "by", "for", "from",
            "in", "into", "is", "it", "of", "on", "or", "that", "the", "this",
            "to", "with", "you", "your"
    );

    private final StudentProjectRepository projectRepository;

    public ResumeOptimizerService(StudentProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    @Transactional(readOnly = true)
    public OptimizeResumeResponse optimize(String jobDescription) {
        Set<String> jobKeywords = keywords(jobDescription);
        LocalDate today = LocalDate.now();

        var recommendations = projectRepository.findAll().stream()
                .map(project -> recommendation(project, jobKeywords, today))
                .filter(candidate -> candidate.relevance() > 0)
                .sorted(Comparator.comparingDouble(Candidate::score).reversed()
                        .thenComparing(Candidate::timelineDate, Comparator.reverseOrder())
                        .thenComparing(candidate -> candidate.project().getId()))
                .map(candidate -> toResponse(candidate.project(), candidate.score()))
                .toList();

        return new OptimizeResumeResponse(recommendations);
    }

    private Candidate recommendation(StudentProject project, Set<String> jobKeywords, LocalDate today) {
        Set<String> projectKeywords = Stream.concat(
                        Stream.of(project.getTitle(), project.getDescription()),
                        project.getSkillTags().stream())
                .flatMap(text -> keywords(text).stream())
                .collect(Collectors.toSet());

        long matchingKeywords = jobKeywords.stream().filter(projectKeywords::contains).count();
        double relevance = jobKeywords.isEmpty() ? 0 : (double) matchingKeywords / jobKeywords.size();
        LocalDate timelineDate = project.getEndDate() == null ? today : project.getEndDate();
        long elapsedDays = Math.max(0, ChronoUnit.DAYS.between(timelineDate, today));
        double yearsAgo = elapsedDays / 365.25;
        double recency = 1.0 / (1.0 + yearsAgo / RECENCY_HALF_LIFE_YEARS);
        double score = round((RELEVANCE_WEIGHT * relevance + RECENCY_WEIGHT * recency) * 100.0);

        return new Candidate(project, relevance, timelineDate, score);
    }

    private Set<String> keywords(String text) {
        return Arrays.stream(text.toLowerCase(Locale.ROOT).split("[^a-z0-9+#.]+"))
                .filter(token -> !token.isBlank() && !STOP_WORDS.contains(token))
                .collect(Collectors.toSet());
    }

    private RecommendedProject toResponse(StudentProject project, double score) {
        return new RecommendedProject(
                project.getId(),
                project.getTitle(),
                project.getDescription(),
                project.getSkillTags(),
                project.getStartDate(),
                project.getEndDate(),
                score
        );
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    private record Candidate(StudentProject project, double relevance, LocalDate timelineDate, double score) {
    }
}
