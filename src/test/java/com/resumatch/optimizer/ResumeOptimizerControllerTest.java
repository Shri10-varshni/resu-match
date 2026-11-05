package com.resumatch.optimizer;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ResumeOptimizerController.class)
class ResumeOptimizerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ResumeOptimizerService optimizerService;

    @Test
    void acceptsJobDescriptionAndReturnsRecommendations() throws Exception {
        when(optimizerService.optimize("Java developer"))
                .thenReturn(new OptimizeResumeResponse(List.of()));

        mockMvc.perform(post("/api/v1/optimize-resume")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"jobDescription":"Java developer"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.recommendedProjects").isArray())
                .andExpect(jsonPath("$.recommendedProjects.length()").value(0));
    }

    @Test
    void rejectsBlankJobDescriptions() throws Exception {
        mockMvc.perform(post("/api/v1/optimize-resume")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"jobDescription":"   "}
                                """))
                .andExpect(status().isBadRequest());
    }
}
