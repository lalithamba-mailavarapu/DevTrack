package in.strikes.devTrack.controller;

import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import in.strikes.devTrack.service.GeminiAiService;
import in.strikes.devTrack.service.ProjectAiContextService;

@WebMvcTest(AiController.class)
class AiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GeminiAiService geminiAiService;
    @MockitoBean
    ProjectAiContextService projectAiContextService;

    @Test
    void shouldAnalyzeIssueSuccessfully() throws Exception {

        when(geminiAiService.generateText(anyString()))
                .thenReturn("Suggested Priority: HIGH");

        mockMvc.perform(post("/api/ai/analyze-issue")
                        .with(user("test@example.com"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Login failure",
                                  "description": "Users cannot log in after resetting their password."
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.analysis")
                        .value("Suggested Priority: HIGH"));

        verify(geminiAiService, times(1))
                .generateText(anyString());
    }

    @Test
    void shouldRejectEmptyTitle() throws Exception {

        mockMvc.perform(post("/api/ai/analyze-issue")
                        .with(user("test@example.com"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "",
                                  "description": "Some description"
                                }
                                """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(geminiAiService);
    }

    @Test
    void shouldRejectEmptyDescription() throws Exception {

        mockMvc.perform(post("/api/ai/analyze-issue")
                        .with(user("test@example.com"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Login failure",
                                  "description": ""
                                }
                                """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(geminiAiService);
    }

    @Test
    void shouldRejectMissingRequestBody() throws Exception {

        mockMvc.perform(post("/api/ai/analyze-issue")
                        .with(user("test@example.com"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(geminiAiService);
    }

    @Test
    void shouldReturnServerErrorWhenAiServiceFails() throws Exception {

        when(geminiAiService.generateText(anyString()))
                .thenThrow(new IllegalStateException(
                        "Gemini API request failed."
                ));

        mockMvc.perform(post("/api/ai/analyze-issue")
                        .with(user("test@example.com"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Login failure",
                                  "description": "Users cannot log in."
                                }
                                """))
                .andExpect(status().isBadGateway());

        verify(geminiAiService, times(1))
                .generateText(anyString());
    }

    @Test
void shouldBreakdownProjectGoalSuccessfully() throws Exception {

    when(geminiAiService.generateText(anyString()))
            .thenReturn("1. Design database\n2. Implement authentication\n3. Build APIs");

    mockMvc.perform(post("/api/ai/breakdown-task")
                    .with(user("test@example.com"))
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                            {
                              "goal": "Build an e-commerce application",
                              "description": "Create authentication, products, cart and payment features."
                            }
                            """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.analysis")
                    .value("1. Design database\n2. Implement authentication\n3. Build APIs"));

    verify(geminiAiService, times(1))
            .generateText(anyString());
}
@Test
void shouldRejectEmptyProjectGoal() throws Exception {

    mockMvc.perform(post("/api/ai/breakdown-task")
                    .with(user("test@example.com"))
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                            {
                              "goal": "",
                              "description": "Build an e-commerce application."
                            }
                            """))
            .andExpect(status().isBadRequest());

    verifyNoInteractions(geminiAiService);
}

@Test
void shouldHandleTaskBreakdownAiFailure() throws Exception {

    when(geminiAiService.generateText(anyString()))
            .thenThrow(new IllegalStateException("Gemini API request failed."));

    mockMvc.perform(post("/api/ai/breakdown-task")
                    .with(user("test@example.com"))
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                            {
                              "goal": "Build an e-commerce application",
                              "description": "Create authentication and product management."
                            }
                            """))
            .andExpect(status().isBadGateway());

    verify(geminiAiService, times(1))
            .generateText(anyString());
}


@Test
void shouldAnalyzeProjectSuccessfully() throws Exception {

    when(projectAiContextService.buildProjectContext(1L))
            .thenReturn("""
                    Project: DevTrack
                    Description: Project management application
                    Language: Java
                    Technologies: Spring Boot, PostgreSQL
                    Issues: 2 open issues
                    """);

    when(geminiAiService.generateText(anyString()))
            .thenReturn("Project Health: Good");

    mockMvc.perform(post("/api/ai/projects/1/analyze")
                    .with(user("test@example.com"))
                    .with(csrf()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.analysis")
                    .value("Project Health: Good"));

    verify(projectAiContextService, times(1))
            .buildProjectContext(1L);

    verify(geminiAiService, times(1))
            .generateText(anyString());
}

@Test
void shouldBreakdownProjectTasksSuccessfully() throws Exception {

    when(projectAiContextService.buildProjectContext(1L))
            .thenReturn("""
                    Project: DevTrack
                    Description: Project management application
                    Language: Java
                    Technologies: Spring Boot, PostgreSQL
                    Issues: 2 open issues
                    """);

    when(geminiAiService.generateText(anyString()))
            .thenReturn("1. Improve authentication\n2. Add project reports");

    mockMvc.perform(post("/api/ai/projects/1/breakdown-tasks")
                    .with(user("test@example.com"))
                    .with(csrf()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.analysis")
                    .value("1. Improve authentication\n2. Add project reports"));

    verify(projectAiContextService, times(1))
            .buildProjectContext(1L);

    verify(geminiAiService, times(1))
            .generateText(anyString());
}

}