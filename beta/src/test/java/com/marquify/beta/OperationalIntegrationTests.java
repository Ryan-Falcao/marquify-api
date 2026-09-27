package com.marquify.beta;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = "api.login-rate-limit.max-attempts=2")
@AutoConfigureMockMvc
@ActiveProfiles("test")
class OperationalIntegrationTests {
    @Autowired MockMvc mvc;
    @Test
    void publicHealthDoesNotExposeDatabaseDetails() throws Exception {
        for (String path : new String[]{"/actuator/health","/actuator/health/liveness","/actuator/health/readiness"})
            mvc.perform(get(path)).andExpect(status().isOk()).andExpect(jsonPath("$.status").value("UP"))
                    .andExpect(jsonPath("$.components").doesNotExist()).andExpect(header().exists("X-Request-ID"));
        mvc.perform(get("/actuator/env")).andExpect(status().isUnauthorized());
    }
    @Test
    void loginIsThrottledBeforePasswordAuthentication() throws Exception {
        for (int i=0;i<3;i++) {
            var result = mvc.perform(post("/auth/login").servletPath("/auth/login")
                    .contentType("application/json").content("{\"login\":\"absent@example.test\",\"senha\":\"invalid-password\"}"));
            if (i<2) result.andExpect(status().isUnauthorized());
            else result.andExpect(status().isTooManyRequests()).andExpect(header().exists("Retry-After"))
                    .andExpect(jsonPath("$.codigo").value("LIMITE_LOGIN"));
        }
    }
}
