package com.marquify.beta.response;
import com.marquify.beta.entity.ContaProfissional;
import java.time.Instant;
public record ConviteProfissionalResponse(String email, String conviteUrl, Instant expiraEm) {
 public static ConviteProfissionalResponse from(ContaProfissional conta, String url) { return new ConviteProfissionalResponse(conta.getEmail(), url, conta.getConviteExpiraEm()); }
}
