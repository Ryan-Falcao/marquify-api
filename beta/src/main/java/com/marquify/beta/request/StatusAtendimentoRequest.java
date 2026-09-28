package com.marquify.beta.request;
import com.marquify.beta.entity.Status;
import jakarta.validation.constraints.NotNull;
public record StatusAtendimentoRequest(@NotNull Status status) {}
