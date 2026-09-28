package com.marquify.beta.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.marquify.beta.entity.Assinatura;
import com.marquify.beta.entity.Vendedor;
import com.marquify.beta.infra.security.CurrentUser;
import com.marquify.beta.response.CheckoutAssinaturaResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
public class MercadoPagoAssinaturaService {
    private static final String API_URL = "https://api.mercadopago.com";
    private final AssinaturaService assinaturas;
    private final CurrentUser currentUser;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private volatile HttpClient http;

    @Value("${payments.mercado-pago.access-token:}")
    private String accessToken;
    @Value("${payments.mercado-pago.webhook-secret:}")
    private String webhookSecret;
    @Value("${payments.mercado-pago.valor-profissional:}")
    private String valorProfissional;
    @Value("${api.public-web-url:http://localhost:5173}")
    private String publicWebUrl;

    public CheckoutAssinaturaResponse iniciarCheckout() {
        if (accessToken.isBlank() || valorProfissional.isBlank()) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "O pagamento ainda não está configurado. Informe as credenciais e o valor do plano Profissional.");
        }
        BigDecimal valor = valor();
        Vendedor vendedor = currentUser.vendedor();
        Assinatura assinatura = assinaturas.assinaturaParaCheckout(vendedor.getEstabelecimento().getId());
        String referencia = "marquify-assinatura-" + assinatura.getId();
        String base = publicWebUrl.replaceAll("/+$", "");
        Map<String, Object> payload = Map.of(
                "reason", "Marquify Profissional",
                "external_reference", referencia,
                "payer_email", vendedor.getEmail(),
                "back_url", base + "/configuracoes?pagamento=sucesso",
                "status", "pending",
                "auto_recurring", Map.of(
                        "frequency", 1,
                        "frequency_type", "months",
                        "free_trial", Map.of(
                                "frequency", AssinaturaService.DIAS_TESTE_GRATIS,
                                "frequency_type", "days"
                        ),
                        "transaction_amount", valor,
                        "currency_id", "BRL"
                )
        );
        JsonNode resposta = chamar("POST", "/preapproval", objectMapper.valueToTree(payload));
        String id = textoObrigatorio(resposta, "id", "O Mercado Pago não retornou a assinatura.");
        String url = textoObrigatorio(resposta, "init_point", "O Mercado Pago não retornou o link de pagamento.");
        assinatura.iniciarCobrancaMercadoPago(id);
        return new CheckoutAssinaturaResponse(url);
    }

    /** Confirma o estado consultando a API do Mercado Pago; o corpo do webhook não é confiável por si só. */
    public void processarWebhook(String assinaturaId, String assinatura, String requestId) {
        if (webhookSecret.isBlank() || !assinaturaValida(assinaturaId, assinatura, requestId)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Assinatura do webhook inválida");
        }
        JsonNode resposta = chamar("GET", "/preapproval/" + assinaturaId, null);
        String status = resposta.path("status").asText();
        if ("authorized".equalsIgnoreCase(status)) {
            assinaturas.iniciarTesteAposCartaoMercadoPago(assinaturaId);
        } else if ("cancelled".equalsIgnoreCase(status)) {
            assinaturas.cancelarPorMercadoPago(assinaturaId);
        }
    }

    private boolean assinaturaValida(String dataId, String cabecalhoAssinatura, String requestId) {
        if (dataId == null || dataId.isBlank() || cabecalhoAssinatura == null || requestId == null) return false;
        String ts = null;
        String v1 = null;
        for (String parte : cabecalhoAssinatura.split(",")) {
            String[] chaveValor = parte.trim().split("=", 2);
            if (chaveValor.length != 2) continue;
            if ("ts".equals(chaveValor[0])) ts = chaveValor[1];
            if ("v1".equals(chaveValor[0])) v1 = chaveValor[1];
        }
        if (ts == null || v1 == null) return false;
        String manifesto = "id:" + dataId + ";request-id:" + requestId + ";ts:" + ts + ";";
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(webhookSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] esperado = mac.doFinal(manifesto.getBytes(StandardCharsets.UTF_8));
            byte[] recebido = hex(v1);
            return MessageDigest.isEqual(esperado, recebido);
        } catch (Exception exception) {
            return false;
        }
    }

    private JsonNode chamar(String metodo, String caminho, JsonNode corpo) {
        try {
            HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create(API_URL + caminho))
                    .timeout(Duration.ofSeconds(20))
                    .header("Authorization", "Bearer " + accessToken)
                    .header("Content-Type", "application/json");
            if ("POST".equals(metodo)) builder.POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(corpo)));
            else builder.GET();
            HttpResponse<String> resposta = http().send(builder.build(), HttpResponse.BodyHandlers.ofString());
            JsonNode json = objectMapper.readTree(resposta.body());
            if (resposta.statusCode() < 200 || resposta.statusCode() >= 300) {
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Não foi possível iniciar o pagamento pelo Mercado Pago.");
            }
            return json;
        } catch (ResponseStatusException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Não foi possível comunicar com o Mercado Pago.");
        }
    }

    private BigDecimal valor() {
        try {
            BigDecimal valor = new BigDecimal(valorProfissional);
            if (valor.signum() <= 0) throw new NumberFormatException();
            return valor;
        } catch (NumberFormatException exception) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "O valor do plano Profissional é inválido.");
        }
    }

    private HttpClient http() {
        HttpClient atual = http;
        if (atual == null) {
            synchronized (this) {
                atual = http;
                if (atual == null) {
                    atual = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
                    http = atual;
                }
            }
        }
        return atual;
    }

    private static String textoObrigatorio(JsonNode json, String campo, String mensagem) {
        String valor = json.path(campo).asText();
        if (valor.isBlank()) throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, mensagem);
        return valor;
    }

    private static byte[] hex(String valor) {
        if ((valor.length() & 1) != 0) return new byte[0];
        byte[] bytes = new byte[valor.length() / 2];
        for (int i = 0; i < valor.length(); i += 2) {
            int digito = Character.digit(valor.charAt(i), 16);
            int proximo = Character.digit(valor.charAt(i + 1), 16);
            if (digito < 0 || proximo < 0) return new byte[0];
            bytes[i / 2] = (byte) ((digito << 4) + proximo);
        }
        return bytes;
    }
}
