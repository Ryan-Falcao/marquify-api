package com.marquify.beta.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record PosicaoFotoRequest(@Min(0) @Max(100) int x, @Min(0) @Max(100) int y) {}
