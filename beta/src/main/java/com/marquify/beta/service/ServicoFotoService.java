package com.marquify.beta.service;

import com.marquify.beta.entity.Servicos;
import com.marquify.beta.entity.Vendedor;
import com.marquify.beta.infra.security.CurrentUser;
import com.marquify.beta.repository.servicoRepository;
import com.marquify.beta.request.PosicaoFotoRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.ByteArrayInputStream;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ServicoFotoService {
    private static final long TAMANHO_MAXIMO = 5 * 1024 * 1024;
    private static final int DIMENSAO_MAXIMA = 6000;
    private static final long PIXELS_MAXIMOS = 25_000_000;
    private final servicoRepository servicos;
    private final CurrentUser currentUser;
    @Value("${api.storage.service-images:./data/service-images}") private String diretorio;

    @Transactional
    public Servicos salvar(Long estabelecimentoId, Long servicoId, MultipartFile foto) {
        var vendedor = currentUser.vendedor();
        if (!vendedor.getEstabelecimento().getId().equals(estabelecimentoId)) {
            throw new org.springframework.security.access.AccessDeniedException("Acesso negado");
        }
        Servicos servico = servicos.findByIdAndEstabelecimentoId(servicoId, estabelecimentoId).orElseThrow(this::notFound);
        if (foto == null || foto.isEmpty() || foto.getSize() > TAMANHO_MAXIMO) {
            throw imagemInvalida();
        }
        try {
            byte[] bytes = foto.getBytes();
            String formato = formatoReal(bytes);
            BufferedImage imagem = ImageIO.read(new ByteArrayInputStream(bytes));
            if (imagem == null || imagem.getWidth() <= 0 || imagem.getHeight() <= 0
                    || imagem.getWidth() > DIMENSAO_MAXIMA || imagem.getHeight() > DIMENSAO_MAXIMA
                    || (long) imagem.getWidth() * imagem.getHeight() > PIXELS_MAXIMOS) {
                throw imagemInvalida();
            }
            Path pasta = Path.of(diretorio).toAbsolutePath().normalize(); Files.createDirectories(pasta);
            String antigo = servico.getFotoArquivo(); String nome = UUID.randomUUID() + "." + formato;
            Path destino = pasta.resolve(nome);
            // Decodificar e regravar mantém somente os pixels, eliminando scripts, conteúdo anexado e metadados.
            if (!ImageIO.write(imagem, formato, destino.toFile())) throw imagemInvalida();
            servico.setFotoArquivo(nome); servicos.save(servico);
            if (antigo != null) Files.deleteIfExists(pasta.resolve(antigo));
            return servico;
        } catch (IOException exception) { throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Não foi possível salvar a foto"); }
    }

    public Servicos salvarAtual(Long servicoId, MultipartFile foto) {
        Vendedor vendedor = currentUser.vendedor();
        return salvar(vendedor.getEstabelecimento().getId(), servicoId, foto);
    }

    private String formatoReal(byte[] bytes) {
        boolean jpeg = bytes.length >= 3 && (bytes[0] & 0xff) == 0xff && (bytes[1] & 0xff) == 0xd8 && (bytes[2] & 0xff) == 0xff;
        boolean png = bytes.length >= 8 && (bytes[0] & 0xff) == 0x89 && bytes[1] == 0x50 && bytes[2] == 0x4e
                && bytes[3] == 0x47 && bytes[4] == 0x0d && bytes[5] == 0x0a && bytes[6] == 0x1a && bytes[7] == 0x0a;
        if (jpeg) return "jpg";
        if (png) return "png";
        throw imagemInvalida();
    }

    private ResponseStatusException imagemInvalida() {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, "Envie uma imagem JPG ou PNG válida de até 5 MB");
    }

    @Transactional(readOnly = true)
    public Resource carregar(Long servicoId) {
        Servicos servico = servicos.findById(servicoId).orElseThrow(this::notFound);
        if (!servico.isAtivo() || servico.getFotoArquivo() == null) throw notFound();
        try { Resource resource = new UrlResource(Path.of(diretorio).toAbsolutePath().normalize().resolve(servico.getFotoArquivo()).toUri()); if (!resource.exists()) throw notFound(); return resource; }
        catch (java.net.MalformedURLException exception) { throw notFound(); }
    }

    @Transactional
    public Servicos posicionar(Long estabelecimentoId, Long servicoId, PosicaoFotoRequest posicao) {
        var vendedor = currentUser.vendedor();
        if (!vendedor.getEstabelecimento().getId().equals(estabelecimentoId)) {
            throw new org.springframework.security.access.AccessDeniedException("Acesso negado");
        }
        Servicos servico = servicos.findByIdAndEstabelecimentoId(servicoId, estabelecimentoId).orElseThrow(this::notFound);
        servico.setFotoPosicaoX(posicao.x());
        servico.setFotoPosicaoY(posicao.y());
        return servicos.save(servico);
    }

    public Servicos posicionarAtual(Long servicoId, PosicaoFotoRequest posicao) {
        Vendedor vendedor = currentUser.vendedor();
        return posicionar(vendedor.getEstabelecimento().getId(), servicoId, posicao);
    }

    private ResponseStatusException notFound() { return new ResponseStatusException(HttpStatus.NOT_FOUND, "Foto não encontrada"); }
}
