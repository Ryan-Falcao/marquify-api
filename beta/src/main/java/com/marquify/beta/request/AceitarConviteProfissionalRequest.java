package com.marquify.beta.request;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
public record AceitarConviteProfissionalRequest(@NotBlank @Size(min = 8, max = 72) String senha) {}
