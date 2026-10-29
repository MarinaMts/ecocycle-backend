package com.ecocycle.backend.seed;

import com.ecocycle.backend.entity.Alternativa;
import com.ecocycle.backend.entity.ConteudoEducativo;
import com.ecocycle.backend.entity.Figurinha;
import com.ecocycle.backend.entity.Pergunta;
import com.ecocycle.backend.entity.PontoColeta;
import com.ecocycle.backend.entity.Quiz;
import com.ecocycle.backend.enums.TipoFigurinha;
import com.ecocycle.backend.repository.ConteudoEducativoRepository;
import com.ecocycle.backend.repository.FigurinhaRepository;
import com.ecocycle.backend.repository.PontoColetaRepository;
import com.ecocycle.backend.repository.QuizRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Sincroniza o banco com o {@link CatalogoSeed} a cada start da aplicacao
 * (sem painel de administracao nesta fase - ver documento de decisoes
 * tecnicas, secao 2).
 *
 * Regras:
 * - cada item e localizado pela chave natural (codigo do conteudo/figurinha,
 *   nome do ponto, ordem da pergunta no quiz, letra da alternativa);
 * - o que nao existe e criado; o que existe e atualizado NO LUGAR (mesmo id),
 *   pois ConteudoLido, QuizResultado e FigurinhaUsuario apontam para esses ids;
 * - nada e removido, e o campo "ativo" de pontos existentes nunca e alterado;
 * - tudo roda numa unica transacao: se algo falhar, nada e gravado e o start
 *   da aplicacao falha (no Render, a versao anterior continua no ar).
 */
@Component
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);
    private static final List<String> LETRAS = List.of("a", "b", "c", "d");

    private final ConteudoEducativoRepository conteudoRepository;
    private final QuizRepository quizRepository;
    private final FigurinhaRepository figurinhaRepository;
    private final PontoColetaRepository pontoColetaRepository;
    private final TransactionTemplate transactionTemplate;

    public DataSeeder(
            ConteudoEducativoRepository conteudoRepository,
            QuizRepository quizRepository,
            FigurinhaRepository figurinhaRepository,
            PontoColetaRepository pontoColetaRepository,
            PlatformTransactionManager transactionManager
    ) {
        this.conteudoRepository = conteudoRepository;
        this.quizRepository = quizRepository;
        this.figurinhaRepository = figurinhaRepository;
        this.pontoColetaRepository = pontoColetaRepository;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
    }

    @Override
    public void run(String... args) {
        sincronizar(CatalogoSeed.padrao());
    }

    Resumo sincronizar(CatalogoSeed catalogo) {
        catalogo.validar();

        Resumo resumo;
        try {
            resumo = transactionTemplate.execute(status -> {
                validarTiposDasFigurinhasNoBanco(catalogo);
                Resumo r = new Resumo(catalogo);
                for (ConteudoSeed conteudo : catalogo.conteudos()) {
                    sincronizarConteudo(conteudo, r);
                }
                sincronizarFigurinhasScan(catalogo.figurinhasScan(), r);
                for (PontoColetaSeed ponto : catalogo.pontos()) {
                    sincronizarPonto(ponto, r);
                }
                return r;
            });
        } catch (RuntimeException e) {
            throw new IllegalStateException("Seed: falha ao sincronizar o catalogo com o banco - "
                    + "transacao desfeita, nada foi gravado. Causa: " + e.getMessage(), e);
        }

        resumo.logar();
        return resumo;
    }

    /**
     * Um codigo de figurinha do catalogo que ja existe no banco com outro tipo
     * indica conflito de chave (ex.: FIG-11 reaproveitado por engano).
     */
    private void validarTiposDasFigurinhasNoBanco(CatalogoSeed catalogo) {
        List<String> erros = new ArrayList<>();
        for (ConteudoSeed c : catalogo.conteudos()) {
            verificarTipo(erros, c.figurinha().codigo(), TipoFigurinha.QUIZ);
        }
        for (FigurinhaScanSeed f : catalogo.figurinhasScan()) {
            verificarTipo(erros, f.codigo(), TipoFigurinha.SCAN);
        }
        if (!erros.isEmpty()) {
            throw new IllegalStateException(String.join("; ", erros));
        }
    }

    private void verificarTipo(List<String> erros, String codigo, TipoFigurinha esperado) {
        figurinhaRepository.findByCodigo(codigo)
                .filter(f -> f.getTipo() != esperado)
                .ifPresent(f -> erros.add("figurinha " + codigo + " ja existe no banco com tipo "
                        + f.getTipo() + ", mas o catalogo define " + esperado));
    }

    // ==================== Conteudo + quiz + figurinha QUIZ ====================

    private void sincronizarConteudo(ConteudoSeed seed, Resumo resumo) {
        Optional<ConteudoEducativo> existente = conteudoRepository.findByCodigo(seed.codigo());
        ConteudoEducativo conteudo;
        if (existente.isEmpty()) {
            conteudo = conteudoRepository.save(ConteudoEducativo.builder()
                    .codigo(seed.codigo())
                    .trilha(seed.trilha())
                    .titulo(seed.titulo())
                    .corpo(seed.corpo())
                    .ordem(seed.ordem())
                    .imagemSugerida(seed.imagemSugerida())
                    .build());
            resumo.conteudos.criado();
        } else {
            conteudo = existente.get();
            boolean mudou = atualizar(conteudo.getTrilha(), seed.trilha(), conteudo::setTrilha);
            mudou |= atualizar(conteudo.getTitulo(), seed.titulo(), conteudo::setTitulo);
            mudou |= atualizar(conteudo.getCorpo(), seed.corpo(), conteudo::setCorpo);
            mudou |= atualizar(conteudo.getOrdem(), seed.ordem(), conteudo::setOrdem);
            mudou |= atualizar(conteudo.getImagemSugerida(), seed.imagemSugerida(), conteudo::setImagemSugerida);
            resumo.conteudos.existente(mudou);
        }

        sincronizarQuiz(conteudo, seed, resumo);
        sincronizarFigurinhaQuiz(conteudo, seed.figurinha(), resumo);
    }

    private void sincronizarQuiz(ConteudoEducativo conteudo, ConteudoSeed seed, Resumo resumo) {
        Optional<Quiz> existente = quizRepository.findByConteudoEducativo_Id(conteudo.getId());
        if (existente.isEmpty()) {
            Quiz quiz = Quiz.builder().conteudoEducativo(conteudo).xpRecompensa(seed.xpQuiz()).build();
            for (int i = 0; i < seed.perguntas().size(); i++) {
                quiz.getPerguntas().add(novaPergunta(quiz, seed.perguntas().get(i), i + 1));
            }
            // Persiste Perguntas + Alternativas em cascata (cascade = ALL)
            quizRepository.save(quiz);
            resumo.quizzes.criado();
            return;
        }

        Quiz quiz = existente.get();
        boolean mudou = atualizar(quiz.getXpRecompensa(), seed.xpQuiz(), quiz::setXpRecompensa);

        // Se houver ordem duplicada no banco, usa a pergunta mais antiga
        Map<Integer, Pergunta> porOrdem = new HashMap<>();
        for (Pergunta p : quiz.getPerguntas()) {
            porOrdem.merge(p.getOrdem(), p, (a, b) -> a.getId() <= b.getId() ? a : b);
        }

        for (int i = 0; i < seed.perguntas().size(); i++) {
            PerguntaSeed perguntaSeed = seed.perguntas().get(i);
            Pergunta pergunta = porOrdem.get(i + 1);
            if (pergunta == null) {
                quiz.getPerguntas().add(novaPergunta(quiz, perguntaSeed, i + 1));
                mudou = true;
            } else {
                mudou |= atualizar(pergunta.getEnunciado(), perguntaSeed.enunciado(), pergunta::setEnunciado);
                mudou |= sincronizarAlternativas(pergunta, perguntaSeed);
            }
        }
        resumo.quizzes.existente(mudou);
    }

    private boolean sincronizarAlternativas(Pergunta pergunta, PerguntaSeed seed) {
        Map<String, Alternativa> porLetra = new HashMap<>();
        for (Alternativa a : pergunta.getAlternativas()) {
            porLetra.merge(a.getLetra(), a, (x, y) -> x.getId() <= y.getId() ? x : y);
        }

        boolean mudou = false;
        for (int i = 0; i < seed.alternativas().size(); i++) {
            String letra = LETRAS.get(i);
            String texto = seed.alternativas().get(i);
            boolean correta = i == seed.indiceCorreta();
            Alternativa alternativa = porLetra.get(letra);
            if (alternativa == null) {
                pergunta.getAlternativas().add(novaAlternativa(pergunta, letra, texto, correta));
                mudou = true;
            } else {
                mudou |= atualizar(alternativa.getTexto(), texto, alternativa::setTexto);
                mudou |= atualizar(alternativa.getCorreta(), correta, alternativa::setCorreta);
            }
        }
        return mudou;
    }

    private Pergunta novaPergunta(Quiz quiz, PerguntaSeed seed, int ordem) {
        Pergunta pergunta = Pergunta.builder()
                .quiz(quiz)
                .enunciado(seed.enunciado())
                .ordem(ordem)
                .build();
        for (int i = 0; i < seed.alternativas().size(); i++) {
            pergunta.getAlternativas().add(novaAlternativa(
                    pergunta, LETRAS.get(i), seed.alternativas().get(i), i == seed.indiceCorreta()));
        }
        return pergunta;
    }

    private Alternativa novaAlternativa(Pergunta pergunta, String letra, String texto, boolean correta) {
        return Alternativa.builder()
                .pergunta(pergunta)
                .texto(texto)
                .letra(letra)
                .correta(correta)
                .build();
    }

    private void sincronizarFigurinhaQuiz(ConteudoEducativo conteudo, FigurinhaQuizSeed seed, Resumo resumo) {
        Optional<Figurinha> existente = figurinhaRepository.findByCodigo(seed.codigo());
        if (existente.isEmpty()) {
            figurinhaRepository.save(Figurinha.builder()
                    .codigo(seed.codigo())
                    .nome(seed.nome())
                    .tipo(TipoFigurinha.QUIZ)
                    .conteudoEducativo(conteudo)
                    .descricao(seed.descricao())
                    .ordem(seed.ordem())
                    .build());
            resumo.figurinhasQuiz.criado();
            return;
        }

        Figurinha figurinha = existente.get();
        boolean mudou = atualizar(figurinha.getNome(), seed.nome(), figurinha::setNome);
        mudou |= atualizar(figurinha.getDescricao(), seed.descricao(), figurinha::setDescricao);
        mudou |= atualizar(figurinha.getOrdem(), seed.ordem(), figurinha::setOrdem);
        resumo.figurinhasQuiz.existente(mudou);
    }

    // ==================== Figurinhas SCAN ====================

    private void sincronizarFigurinhasScan(List<FigurinhaScanSeed> seeds, Resumo resumo) {
        // O ScannerService pega a primeira figurinha SCAN com o identificador:
        // duas com o mesmo identificador fariam uma delas nunca ser desbloqueada.
        for (FigurinhaScanSeed seed : seeds) {
            if (figurinhaRepository.existsByTipoAndIdentificadorScanAndCodigoNot(
                    TipoFigurinha.SCAN, seed.identificadorScan(), seed.codigo())) {
                throw new IllegalStateException("ja existe outra figurinha SCAN com identificadorScan '"
                        + seed.identificadorScan() + "' (codigo diferente de " + seed.codigo()
                        + "); corrija o banco ou o catalogo");
            }
        }

        for (FigurinhaScanSeed seed : seeds) {
            Optional<Figurinha> existente = figurinhaRepository.findByCodigo(seed.codigo());
            if (existente.isEmpty()) {
                figurinhaRepository.save(Figurinha.builder()
                        .codigo(seed.codigo())
                        .nome(seed.nome())
                        .tipo(TipoFigurinha.SCAN)
                        .identificadorScan(seed.identificadorScan())
                        .descricao(seed.descricao())
                        .ordem(seed.ordem())
                        .build());
                resumo.figurinhasScan.criado();
                continue;
            }

            Figurinha figurinha = existente.get();
            boolean mudou = atualizar(figurinha.getNome(), seed.nome(), figurinha::setNome);
            mudou |= atualizar(figurinha.getIdentificadorScan(), seed.identificadorScan(), figurinha::setIdentificadorScan);
            mudou |= atualizar(figurinha.getDescricao(), seed.descricao(), figurinha::setDescricao);
            mudou |= atualizar(figurinha.getOrdem(), seed.ordem(), figurinha::setOrdem);
            resumo.figurinhasScan.existente(mudou);
        }
    }

    // ==================== Pontos de coleta ====================

    private void sincronizarPonto(PontoColetaSeed seed, Resumo resumo) {
        Optional<PontoColeta> existente = pontoColetaRepository.findFirstByNomeOrderByIdAsc(seed.nome());
        if (existente.isEmpty()) {
            pontoColetaRepository.save(PontoColeta.builder()
                    .nome(seed.nome())
                    .endereco(seed.endereco())
                    .latitude(seed.latitude())
                    .longitude(seed.longitude())
                    .tiposResiduoAceitos(new ArrayList<>(seed.tiposResiduo()))
                    .horarioFuncionamento(seed.horario())
                    .ativo(true)
                    .build());
            resumo.pontos.criado();
            return;
        }

        // "ativo" nao e tocado: tirar um ponto do mapa e decisao feita direto no banco
        PontoColeta ponto = existente.get();
        boolean mudou = atualizar(ponto.getEndereco(), seed.endereco(), ponto::setEndereco);
        mudou |= atualizar(ponto.getLatitude(), seed.latitude(), ponto::setLatitude);
        mudou |= atualizar(ponto.getLongitude(), seed.longitude(), ponto::setLongitude);
        mudou |= atualizar(ponto.getHorarioFuncionamento(), seed.horario(), ponto::setHorarioFuncionamento);

        // PersistentBag nao implementa equals por conteudo: compara uma copia
        // e atualiza a propria colecao gerenciada (clear + addAll)
        if (!new ArrayList<>(ponto.getTiposResiduoAceitos()).equals(seed.tiposResiduo())) {
            ponto.getTiposResiduoAceitos().clear();
            ponto.getTiposResiduoAceitos().addAll(seed.tiposResiduo());
            mudou = true;
        }
        resumo.pontos.existente(mudou);
    }

    // ==================== Helpers ====================

    /** Aplica o valor do catalogo so se for diferente do atual. Retorna true se mudou. */
    private static <T> boolean atualizar(T atual, T novo, Consumer<T> setter) {
        if (Objects.equals(atual, novo)) {
            return false;
        }
        setter.accept(novo);
        return true;
    }

    static final class Contador {
        private final String tipo;
        private final int noCatalogo;
        private int criados;
        private int atualizados;

        Contador(String tipo, int noCatalogo) {
            this.tipo = tipo;
            this.noCatalogo = noCatalogo;
        }

        void criado() {
            criados++;
        }

        void existente(boolean mudou) {
            if (mudou) {
                atualizados++;
            }
        }

        int criados() {
            return criados;
        }

        int atualizados() {
            return atualizados;
        }
    }

    static final class Resumo {
        final Contador conteudos;
        final Contador quizzes;
        final Contador figurinhasQuiz;
        final Contador figurinhasScan;
        final Contador pontos;

        Resumo(CatalogoSeed catalogo) {
            this.conteudos = new Contador("conteudos", catalogo.conteudos().size());
            this.quizzes = new Contador("quizzes", catalogo.conteudos().size());
            this.figurinhasQuiz = new Contador("figurinhas QUIZ", catalogo.conteudos().size());
            this.figurinhasScan = new Contador("figurinhas SCAN", catalogo.figurinhasScan().size());
            this.pontos = new Contador("pontos de coleta", catalogo.pontos().size());
        }

        private void logar() {
            for (Contador c : List.of(conteudos, quizzes, figurinhasQuiz, figurinhasScan, pontos)) {
                log.info("Seed sincronizado - {}: {} no catalogo, {} criados, {} atualizados.",
                        c.tipo, c.noCatalogo, c.criados, c.atualizados);
            }
        }
    }
}
