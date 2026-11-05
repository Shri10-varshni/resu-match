package com.resumatch.optimizer;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record OptimizeResumeRequest(
        @NotBlank @Size(max = 20_000) String jobDescription
) {
}
