package com.marquify.beta;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import java.util.UUID;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthenticationRequestLifecycleTests {
    @Autowired MockMvc mvc;

    // Each HTTP request must run without a shared test transaction/persistence context.
    @Test
    void loginThenPersonalizationAcrossSeparateRequests() throws Exception {
        String email = "Conta." + UUID.randomUUID() + "@Example.test";
        mvc.perform(post("/auth/cadastro-comercial").contentType("application/json").content("""
                {"nome":"Teste","estabelecimento":"Loja teste","email":"%s",
                 "senha":"test-password","fusoHorario":"America/Sao_Paulo"}
                """.formatted(email))).andExpect(status().isCreated());
        String response = mvc.perform(post("/auth/login").contentType("application/json")
                .content("{\"login\":\" " + email + " \",\"senha\":\"test-password\"}"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        String token = JsonPath.read(response, "$.token");
        mvc.perform(post("/auth/login").contentType("application/json")
                .content("{\"login\":\"" + email.toLowerCase(java.util.Locale.ROOT) + "\",\"senha\":\"test-password\"}"))
                .andExpect(status().isOk());
        mvc.perform(post("/auth/login").contentType("application/json")
                .content("{\"login\":\"" + email + "\",\"senha\":\"TEST-PASSWORD\"}"))
                .andExpect(status().isUnauthorized());
        mvc.perform(post("/auth/cadastro-comercial").contentType("application/json").content("""
                {"nome":"Duplicado","estabelecimento":"Outra loja","email":"%s",
                 "senha":"test-password","fusoHorario":"America/Sao_Paulo"}
                """.formatted(email.toUpperCase(java.util.Locale.ROOT)))).andExpect(status().isConflict());
        mvc.perform(get("/vendedor/me/dashboard").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
        mvc.perform(get("/vendedor/me/personalizacao").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
        mvc.perform(put("/vendedor/me/personalizacao").header("Authorization", "Bearer " + token)
                .contentType("application/json").content("""
                {"nome":"Loja publicada","corPrimaria":"#335F55","capaPosicaoX":50,"capaPosicaoY":50,"tema":"{}"}
                """)).andExpect(status().isOk());
        mvc.perform(get("/vendedor/me/personalizacao").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk()).andExpect(jsonPath("$.nome").value("Loja publicada"));
    }
}
