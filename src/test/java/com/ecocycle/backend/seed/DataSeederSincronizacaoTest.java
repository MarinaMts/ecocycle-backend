package com.ecocycle.backend.seed;

import com.ecocycle.backend.entity.ConteudoEducativo;
import com.ecocycle.backend.entity.ConteudoLido;
import com.ecocycle.backend.entity.Figurinha;
import com.ecocycle.backend.entity.FigurinhaUsuario;
import com.ecocycle.backend.entity.PontoColeta;
import com.ecocycle.backend.entity.Quiz;
import com.ecocycle.backend.entity.QuizResultado;
import com.ecocycle.backend.entity.User;
import com.ecocycle.backend.enums.TipoFigurinha;
import com.ecocycle.backend.repository.ConteudoEducativoRepository;
import com.ecocycle.backend.repository.ConteudoLidoRepository;
import com.ecocycle.backend.repository.FigurinhaRepository;
import com.ecocycle.backend.repository.FigurinhaUsuarioRepository;
import com.ecocycle.backend.repository.PontoColetaRepository;
import com.ecocycle.backend.repository.QuizRepository;
import com.ecocycle.backend.repository.QuizResultadoRepository;
import com.ecocycle.backend.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Sincronizacao do DataSeeder com o catalogo.
 *
 * Sem @Transactional de proposito: em producao o seed roda fora de qualquer
 * transacao de teste, abrindo a propria. Como o banco H2 e compartilhado com
 * as outras classes de teste (mesmo contexto em cache - por isso o
 * @AutoConfigureMockMvc, igual ao BaseIntegrationTest), o @AfterEach devolve
 * o banco ao estado do catalogo.
 *
 * Nunca apaga o PRIMEIRO ponto nem a PRIMEIRA figurinha: outros testes usam
 * /pontos-coleta/1 e /figurinhas/1.
 */
@SpringBootTest
@AutoConfigureMockMvc
class DataSeederSincronizacaoTest {

    private static final String PREFIXO_TESTE = "TST-";
    private static final String ALTERADO = "ALTERADO PELO TESTE";

    @Autowired
    private DataSeeder dataSeeder;
    @Autowired
    private JdbcTemplate jdbc;
    @Autowired
    private ConteudoEducativoRepository conteudoRepository;
    @Autowired
    private QuizRepository quizRepository;
    @Autowired
    private FigurinhaRepository figurinhaRepository;
    @Autowired
    private PontoColetaRepository pontoColetaRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ConteudoLidoRepository conteudoLidoRepository;
    @Autowired
    private QuizResultadoRepository quizResultadoRepository;
    @Autowired
    private FigurinhaUsuarioRepository figurinhaUsuarioRepository;

    private final CatalogoSeed catalogo = CatalogoSeed.padrao();
    private final List<Long> usuariosCriados = new ArrayList<>();

    @AfterEach
    void devolverBancoAoEstadoDoCatalogo() {
        for (Long userId : usuariosCriados) {
            jdbc.update("delete from figurinhas_usuario where user_id = ?", userId);
            jdbc.update("delete from quiz_resultados where user_id = ?", userId);
            jdbc.update("delete from conteudos_lidos where user_id = ?", userId);
            jdbc.update("delete from users where id = ?", userId);
        }
        jdbc.update("delete from figurinhas where codigo like ?", PREFIXO_TESTE + "%");
        for (FigurinhaScanSeed f : catalogo.figurinhasScan()) {
            jdbc.update("update figurinhas set tipo = 'SCAN' where codigo = ?", f.codigo());
        }
        jdbc.update("update pontos_coleta set ativo = true");
        dataSeeder.run();
    }

    // ==================== Idempotencia ====================

    @Test
    void rodarOSeedDuasVezesNaoMudaNenhumaContagem() {
        Map<String, Long> antes = contagens();

        dataSeeder.sincronizar(catalogo);
        DataSeeder.Resumo segunda = dataSeeder.sincronizar(catalogo);

        assertThat(contagens()).isEqualTo(antes);
        assertThat(antes.get("conteudos_educativos")).isEqualTo(7);
        assertThat(antes.get("figurinhas")).isEqualTo(10);
        assertThat(antes.get("pontos_coleta")).isEqualTo(15);
        assertNadaCriadoNemAtualizado(segunda);
    }

    // ==================== Criacao do que falta ====================

    @Test
    void recriaUltimoPontoEUltimaFigurinhaScanApagados() {
        Map<String, Long> antes = contagens();
        PontoColetaSeed ultimoPonto = ultimo(catalogo.pontos());
        FigurinhaScanSeed ultimaScan = ultimo(catalogo.figurinhasScan());

        pontoColetaRepository.delete(pontoColetaRepository.findFirstByNomeOrderByIdAsc(ultimoPonto.nome()).orElseThrow());
        figurinhaRepository.delete(figurinhaRepository.findByCodigo(ultimaScan.codigo()).orElseThrow());
        assertThat(contagens().get("pontos_coleta")).isEqualTo(antes.get("pontos_coleta") - 1);
        assertThat(contagens().get("figurinhas")).isEqualTo(antes.get("figurinhas") - 1);

        DataSeeder.Resumo resumo = dataSeeder.sincronizar(catalogo);

        assertThat(resumo.pontos.criados()).isEqualTo(1);
        assertThat(resumo.figurinhasScan.criados()).isEqualTo(1);
        assertThat(resumo.conteudos.criados()).isZero();
        assertThat(contagens()).isEqualTo(antes);

        PontoColeta ponto = pontoColetaRepository.findFirstByNomeOrderByIdAsc(ultimoPonto.nome()).orElseThrow();
        assertThat(ponto.getEndereco()).isEqualTo(ultimoPonto.endereco());
        assertThat(ponto.getLatitude()).isEqualTo(ultimoPonto.latitude());
        assertThat(ponto.getLongitude()).isEqualTo(ultimoPonto.longitude());
        assertThat(ponto.getTiposResiduoAceitos()).containsExactlyElementsOf(ultimoPonto.tiposResiduo());
        assertThat(ponto.getAtivo()).isTrue();

        Figurinha figurinha = figurinhaRepository.findByCodigo(ultimaScan.codigo()).orElseThrow();
        assertThat(figurinha.getTipo()).isEqualTo(TipoFigurinha.SCAN);
        assertThat(figurinha.getIdentificadorScan()).isEqualTo(ultimaScan.identificadorScan());
        assertThat(figurinha.getNome()).isEqualTo(ultimaScan.nome());
    }

    // ==================== Atualizacao no lugar ====================

    @Test
    void edicaoDiretaNoBancoVoltaAoValorDoCatalogoMantendoOsIds() {
        ConteudoSeed edu01 = catalogo.conteudos().get(0);
        PerguntaSeed primeiraPergunta = edu01.perguntas().get(0);
        FigurinhaScanSeed fig08 = catalogo.figurinhasScan().get(0);
        PontoColetaSeed primeiroPonto = catalogo.pontos().get(0);

        Long conteudoId = id("select id from conteudos_educativos where codigo = ?", edu01.codigo());
        Long perguntaId = id("select p.id from perguntas p join quizzes q on q.id = p.quiz_id "
                + "where q.conteudo_educativo_id = ? and p.ordem = 1", conteudoId);
        Long alternativaId = id("select id from alternativas where pergunta_id = ? and letra = 'b'", perguntaId);
        Long figurinhaQuizId = id("select id from figurinhas where codigo = ?", edu01.figurinha().codigo());
        Long figurinhaScanId = id("select id from figurinhas where codigo = ?", fig08.codigo());
        Long pontoId = id("select min(id) from pontos_coleta where nome = ?", primeiroPonto.nome());
        Map<String, Long> antes = contagens();

        jdbc.update("update conteudos_educativos set titulo = ?, corpo = ? where id = ?", ALTERADO, ALTERADO, conteudoId);
        jdbc.update("update perguntas set enunciado = ? where id = ?", ALTERADO, perguntaId);
        jdbc.update("update alternativas set texto = ?, correta = false where id = ?", ALTERADO, alternativaId);
        jdbc.update("update figurinhas set nome = ?, descricao = ? where id = ?", ALTERADO, ALTERADO, figurinhaQuizId);
        jdbc.update("update figurinhas set nome = ?, identificador_scan = 'OUTRO' where id = ?", ALTERADO, figurinhaScanId);
        jdbc.update("update pontos_coleta set endereco = ?, latitude = 0, horario_funcionamento = ? where id = ?",
                ALTERADO, ALTERADO, pontoId);
        jdbc.update("delete from ponto_coleta_tipos_residuo where ponto_coleta_id = ?", pontoId);
        jdbc.update("insert into ponto_coleta_tipos_residuo (ponto_coleta_id, tipo_residuo) values (?, ?)", pontoId, ALTERADO);

        DataSeeder.Resumo resumo = dataSeeder.sincronizar(catalogo);

        assertThat(resumo.conteudos.atualizados()).isEqualTo(1);
        assertThat(resumo.quizzes.atualizados()).isEqualTo(1);
        assertThat(resumo.figurinhasQuiz.atualizados()).isEqualTo(1);
        assertThat(resumo.figurinhasScan.atualizados()).isEqualTo(1);
        assertThat(resumo.pontos.atualizados()).isEqualTo(1);
        assertThat(contagens()).isEqualTo(antes);

        // mesmos ids, valores do catalogo
        assertThat(texto("select titulo from conteudos_educativos where id = ?", conteudoId)).isEqualTo(edu01.titulo());
        assertThat(texto("select corpo from conteudos_educativos where id = ?", conteudoId)).isEqualTo(edu01.corpo());
        assertThat(texto("select enunciado from perguntas where id = ?", perguntaId)).isEqualTo(primeiraPergunta.enunciado());
        assertThat(texto("select texto from alternativas where id = ?", alternativaId))
                .isEqualTo(primeiraPergunta.alternativas().get(1));
        assertThat(jdbc.queryForObject("select correta from alternativas where id = ?", Boolean.class, alternativaId))
                .isEqualTo(primeiraPergunta.indiceCorreta() == 1);
        assertThat(texto("select nome from figurinhas where id = ?", figurinhaQuizId)).isEqualTo(edu01.figurinha().nome());
        assertThat(texto("select descricao from figurinhas where id = ?", figurinhaQuizId))
                .isEqualTo(edu01.figurinha().descricao());
        assertThat(texto("select identificador_scan from figurinhas where id = ?", figurinhaScanId))
                .isEqualTo(fig08.identificadorScan());

        PontoColeta ponto = pontoColetaRepository.findById(pontoId).orElseThrow();
        assertThat(ponto.getEndereco()).isEqualTo(primeiroPonto.endereco());
        assertThat(ponto.getLatitude()).isEqualTo(primeiroPonto.latitude());
        assertThat(ponto.getHorarioFuncionamento()).isEqualTo(primeiroPonto.horario());
        assertThat(ponto.getTiposResiduoAceitos()).containsExactlyElementsOf(primeiroPonto.tiposResiduo());
    }

    @Test
    void pontoInativoContinuaInativoDepoisDoSeed() {
        Long pontoId = id("select min(id) from pontos_coleta where nome = ?", catalogo.pontos().get(0).nome());
        jdbc.update("update pontos_coleta set ativo = false where id = ?", pontoId);

        DataSeeder.Resumo resumo = dataSeeder.sincronizar(catalogo);

        assertThat(pontoColetaRepository.findById(pontoId).orElseThrow().getAtivo()).isFalse();
        assertNadaCriadoNemAtualizado(resumo);
    }

    @Test
    void dadosDeUsuarioSobrevivemAoSeedComOsMesmosIds() {
        User user = userRepository.save(User.builder()
                .email("seed" + UUID.randomUUID().toString().substring(0, 8) + "@teste.com")
                .apelido("seed_" + UUID.randomUUID().toString().substring(0, 8))
                .senhaHash("nao-usado")
                .build());
        usuariosCriados.add(user.getId());

        ConteudoEducativo conteudo = conteudoRepository.findByCodigo("EDU-01").orElseThrow();
        Quiz quiz = quizRepository.findByConteudoEducativo_Id(conteudo.getId()).orElseThrow();
        Figurinha figurinha = figurinhaRepository.findByCodigo("FIG-01").orElseThrow();

        Long lidoId = conteudoLidoRepository.save(ConteudoLido.builder()
                .user(user).conteudoEducativo(conteudo).build()).getId();
        Long resultadoId = quizResultadoRepository.save(QuizResultado.builder()
                .user(user).quiz(quiz).acertos(5).totalPerguntas(5).xpConcedido(true).build()).getId();
        Long desbloqueioId = figurinhaUsuarioRepository.save(FigurinhaUsuario.builder()
                .user(user).figurinha(figurinha).build()).getId();

        // forca o seed a passar pelo caminho de atualizacao dessas linhas
        jdbc.update("update conteudos_educativos set titulo = ? where id = ?", ALTERADO, conteudo.getId());
        jdbc.update("update figurinhas set nome = ? where id = ?", ALTERADO, figurinha.getId());

        DataSeeder.Resumo resumo = dataSeeder.sincronizar(catalogo);
        assertThat(resumo.conteudos.atualizados()).isEqualTo(1);
        assertThat(resumo.figurinhasQuiz.atualizados()).isEqualTo(1);

        assertThat(jdbc.queryForObject("select conteudo_educativo_id from conteudos_lidos where id = ?", Long.class, lidoId))
                .isEqualTo(conteudo.getId());
        assertThat(jdbc.queryForObject("select quiz_id from quiz_resultados where id = ?", Long.class, resultadoId))
                .isEqualTo(quiz.getId());
        assertThat(jdbc.queryForObject("select figurinha_id from figurinhas_usuario where id = ?", Long.class, desbloqueioId))
                .isEqualTo(figurinha.getId());
        assertThat(figurinhaUsuarioRepository.existsByUser_IdAndFigurinha_Id(user.getId(), figurinha.getId())).isTrue();
        assertThat(figurinhaRepository.findByCodigo("FIG-01").orElseThrow().getId()).isEqualTo(figurinha.getId());
    }

    // ==================== Conflitos no banco ====================

    @Test
    void identificadorScanRepetidoEmOutraFigurinhaFalhaSemGravarNada() {
        Long conteudoId = id("select id from conteudos_educativos where codigo = 'EDU-01'");
        jdbc.update("update conteudos_educativos set titulo = ? where id = ?", ALTERADO, conteudoId);
        figurinhaRepository.save(Figurinha.builder()
                .codigo(PREFIXO_TESTE + "SCAN")
                .nome("Duplicada")
                .tipo(TipoFigurinha.SCAN)
                .identificadorScan("PILHA")
                .descricao("Mesmo identificador da FIG-08")
                .ordem(99)
                .build());

        assertThatThrownBy(() -> dataSeeder.run())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("identificadorScan 'PILHA'");

        // tudo ou nada: a correcao do conteudo (feita antes das figurinhas SCAN) foi desfeita
        assertThat(texto("select titulo from conteudos_educativos where id = ?", conteudoId)).isEqualTo(ALTERADO);
    }

    @Test
    void codigoDeFigurinhaExistenteComOutroTipoFalha() {
        jdbc.update("update figurinhas set tipo = 'QUIZ' where codigo = 'FIG-10'");

        assertThatThrownBy(() -> dataSeeder.run())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("figurinha FIG-10 ja existe no banco com tipo QUIZ");
    }

    // ==================== Validacao do catalogo ====================

    @Test
    void catalogoPadraoEValido() {
        assertThatCode(catalogo::validar).doesNotThrowAnyException();
    }

    @Test
    void validacaoRejeitaCodigoRepetido() {
        List<ConteudoSeed> conteudos = new ArrayList<>(catalogo.conteudos());
        conteudos.add(catalogo.conteudos().get(0));

        assertInvalido(new CatalogoSeed(conteudos, catalogo.figurinhasScan(), catalogo.pontos()),
                "codigo de conteudo repetido: EDU-01");
    }

    @Test
    void validacaoRejeitaIdentificadorEmMinusculas() {
        assertInvalido(comFigurinhaScan(new FigurinhaScanSeed("FIG-99", "Teste", "pilha_nova", "Descricao", 99)),
                "identificadorScan 'pilha_nova' fora do padrao");
    }

    @Test
    void validacaoRejeitaDescricaoMaiorQueAColuna() {
        assertInvalido(comFigurinhaScan(new FigurinhaScanSeed("FIG-99", "Teste", "NOVO", "x".repeat(501), 99)),
                "descricao com 501 caracteres (maximo 500)");
    }

    @Test
    void validacaoRejeitaNomeDePontoRepetido() {
        assertInvalido(comPonto(catalogo.pontos().get(0)),
                "nome de ponto de coleta repetido: " + catalogo.pontos().get(0).nome());
    }

    @Test
    void validacaoRejeitaCoordenadaInvalida() {
        assertInvalido(comPonto(new PontoColetaSeed("Ponto Teste", "Rua Teste, 1", 91.0, -46.6, List.of("Pilhas"), null)),
                "latitude invalida");
    }

    @Test
    void validacaoRejeitaPerguntaSemAlternativaCorreta() {
        ConteudoSeed c = catalogo.conteudos().get(0);
        List<PerguntaSeed> perguntas = new ArrayList<>(c.perguntas());
        PerguntaSeed p = perguntas.get(0);
        perguntas.set(0, new PerguntaSeed(p.enunciado(), p.alternativas(), -1));
        ConteudoSeed semCorreta = new ConteudoSeed(c.codigo(), c.trilha(), c.titulo(), c.corpo(), c.ordem(),
                c.imagemSugerida(), c.xpQuiz(), perguntas, c.figurinha());

        List<ConteudoSeed> conteudos = new ArrayList<>(catalogo.conteudos());
        conteudos.set(0, semCorreta);

        assertInvalido(new CatalogoSeed(conteudos, catalogo.figurinhasScan(), catalogo.pontos()),
                "conteudo EDU-01, pergunta 1: precisa ter exatamente 1 alternativa correta");
    }

    // ==================== Helpers ====================

    private void assertInvalido(CatalogoSeed invalido, String trechoDaMensagem) {
        Map<String, Long> antes = contagens();
        assertThatThrownBy(() -> dataSeeder.sincronizar(invalido))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Catalogo do seed invalido")
                .hasMessageContaining(trechoDaMensagem);
        assertThat(contagens()).isEqualTo(antes);
    }

    private CatalogoSeed comFigurinhaScan(FigurinhaScanSeed extra) {
        List<FigurinhaScanSeed> scans = new ArrayList<>(catalogo.figurinhasScan());
        scans.add(extra);
        return new CatalogoSeed(catalogo.conteudos(), scans, catalogo.pontos());
    }

    private CatalogoSeed comPonto(PontoColetaSeed extra) {
        List<PontoColetaSeed> pontos = new ArrayList<>(catalogo.pontos());
        pontos.add(extra);
        return new CatalogoSeed(catalogo.conteudos(), catalogo.figurinhasScan(), pontos);
    }

    private void assertNadaCriadoNemAtualizado(DataSeeder.Resumo resumo) {
        for (DataSeeder.Contador c : List.of(resumo.conteudos, resumo.quizzes, resumo.figurinhasQuiz,
                resumo.figurinhasScan, resumo.pontos)) {
            assertThat(c.criados()).isZero();
            assertThat(c.atualizados()).isZero();
        }
    }

    private Map<String, Long> contagens() {
        Map<String, Long> contagens = new LinkedHashMap<>();
        for (String tabela : List.of("conteudos_educativos", "quizzes", "perguntas", "alternativas",
                "figurinhas", "pontos_coleta", "ponto_coleta_tipos_residuo")) {
            contagens.put(tabela, jdbc.queryForObject("select count(*) from " + tabela, Long.class));
        }
        return contagens;
    }

    private static <T> T ultimo(List<T> lista) {
        return lista.get(lista.size() - 1);
    }

    private Long id(String sql, Object... args) {
        return jdbc.queryForObject(sql, Long.class, args);
    }

    private String texto(String sql, Object... args) {
        return jdbc.queryForObject(sql, String.class, args);
    }
}
