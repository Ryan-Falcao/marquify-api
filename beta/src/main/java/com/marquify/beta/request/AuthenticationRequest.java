package com.marquify.beta.request;

import lombok.AllArgsConstructor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class AuthenticationRequest {

    @NotBlank
    @Size(max = 255)
    private String login;

    @NotBlank
    @Size(max = 72)
    private String senha;
}
