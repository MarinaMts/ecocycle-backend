package com.ecocycle.backend.seed;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Catalogo declarativo de tudo que o seed garante existir no banco:
 * conteudos (com quiz e figurinha QUIZ), figurinhas SCAN e pontos de coleta.
 *
 * Para adicionar ou corrigir um item, basta editar as listas abaixo (ou
 * {@link CatalogoConteudos}) e subir a aplicacao: o {@link DataSeeder}
 * sincroniza item a item pela chave natural (codigo ou nome), criando o que
 * falta e atualizando no lugar o que mudou. Nada e removido do banco.
 */
record CatalogoSeed(
        List<ConteudoSeed> conteudos,
        List<FigurinhaScanSeed> figurinhasScan,
        List<PontoColetaSeed> pontos
) {

    // Tamanhos das colunas (ver entidades e V1__init.sql)
    static final int MAX_CODIGO = 20;
    static final int MAX_NOME = 150;
    static final int MAX_DESCRICAO = 500;
    static final int MAX_ENDERECO = 300;
    static final int MAX_HORARIO = 200;
    static final int MAX_TIPO_RESIDUO = 100;
    static final int MAX_IDENTIFICADOR_SCAN = 100;
    static final int MAX_IMAGEM_SUGERIDA = 300;
    static final int MAX_TEXTO_ALTERNATIVA = 500;
    static final int ALTERNATIVAS_POR_PERGUNTA = 4;

    private static final Pattern IDENTIFICADOR_SCAN = Pattern.compile("[A-Z0-9_]+");

    static CatalogoSeed padrao() {
        return new CatalogoSeed(CatalogoConteudos.todos(), FIGURINHAS_SCAN, PONTOS_COLETA);
    }

    // ==================== Figurinhas SCAN ====================

    // Identificadores alinhados as 3 classes do modelo YOLO11n-cls
    // rodando on-device (ver mudancas no back-end para o scanner com IA, 07/10/2026).
    private static final List<FigurinhaScanSeed> FIGURINHAS_SCAN = List.of(
            new FigurinhaScanSeed("FIG-08", "Pilha", "PILHA",
                    "Pilhas têm metais como zinco e manganês, e alguns tipos têm níquel ou cádmio. " +
                            "Nunca vão no lixo comum: leve a um ponto de coleta de pilhas.", 8),
            new FigurinhaScanSeed("FIG-09", "Mouse", "MOUSE",
                    "O mouse tem placa eletrônica, plástico e cobre no cabo. " +
                            "Descarte em um ponto de coleta de lixo eletrônico.", 9),
            new FigurinhaScanSeed("FIG-10", "Ferro de passar", "FERRO_PASSAR",
                    "O ferro tem resistência metálica, fios de cobre e plástico. " +
                            "Leve a um ponto de coleta de eletroeletrônicos.", 10)
    );

    // ==================== Pontos de coleta ====================

    private static final List<String> TIPOS_SUCATA_DIGITAL_COTIA = List.of(
            "Computadores", "Monitores", "Teclados", "Impressoras", "Celulares", "Tablets",
            "Carregadores", "Cabos", "Câmeras", "Televisores pequenos", "Micro-ondas", "Rádios");

    // O nome e a chave do ponto: os 6 primeiros (placeholder - Sao Paulo) ficam
    // sem acento porque ja estao gravados assim no banco. Novos pontos entram no FINAL.
    private static final List<PontoColetaSeed> PONTOS_COLETA = List.of(
            new PontoColetaSeed(
                    "Eco Ponto Pinheiros",
                    "Rua dos Pinheiros, 500 - Pinheiros, Sao Paulo - SP",
                    -23.5629, -46.6822,
                    List.of("Pilhas", "Baterias", "Celulares", "Cabos"),
                    "Seg a Sex, 9h-18h"),
            new PontoColetaSeed(
                    "PEV Shopping Higienopolis",
                    "Av. Higienopolis, 618 - Higienopolis, Sao Paulo - SP",
                    -23.5411, -46.6558,
                    List.of("Celulares", "Notebooks", "Pilhas", "Baterias"),
                    "Todos os dias, 10h-22h"),
            new PontoColetaSeed(
                    "Ecoponto Vila Madalena",
                    "Rua Harmonia, 950 - Vila Madalena, Sao Paulo - SP",
                    -23.5505, -46.6889,
                    List.of("Eletrodomesticos pequenos", "Pilhas", "Baterias"),
                    "Ter a Sab, 8h-17h"),
            new PontoColetaSeed(
                    "Ponto de Coleta Mackenzie - Higienopolis",
                    "Rua da Consolacao, 930 - Consolacao, Sao Paulo - SP",
                    -23.5433, -46.6528,
                    List.of("Celulares", "Computadores", "Pilhas", "Baterias", "Cabos"),
                    "Seg a Sex, 8h-20h (durante o periodo letivo)"),
            new PontoColetaSeed(
                    "Eco Coleta Paraiso",
                    "Rua Bahia, 120 - Paraiso, Sao Paulo - SP",
                    -23.5735, -46.6389,
                    List.of("Pilhas", "Baterias", "Lampadas fluorescentes"),
                    "Seg a Sex, 9h-17h"),
            new PontoColetaSeed(
                    "PEV Moema",
                    "Av. Ibirapuera, 2332 - Moema, Sao Paulo - SP",
                    -23.6003, -46.6633,
                    List.of("Celulares", "Notebooks", "Tablets", "Pilhas", "Baterias"),
                    "Seg a Sab, 9h-19h"),
            new PontoColetaSeed(
                    "Mini Ecoponto Adalgisa (Osasco)",
                    "Rua Octávio Catelani, s/nº - Adalgisa, Osasco - SP",
                    -23.5623, -46.7665,
                    List.of("Eletrônicos em geral"),
                    "Seg a Sáb, 8h-16h (fecha das 12h às 13h)"),
            new PontoColetaSeed(
                    "SuperShopping Osasco",
                    "Av. dos Autonomistas, 1828 - Industrial Autonomistas, Osasco - SP",
                    -23.5351, -46.7713,
                    List.of("Celulares", "Tablets", "Notebooks", "Pilhas", "Baterias", "Carregadores", "Eletroportáteis"),
                    "Seg a Sáb, 10h-22h; Dom, 12h-20h"),
            new PontoColetaSeed(
                    "Shopping União de Osasco (lojas TIM, Vivo, Claro e C&A)",
                    "Av. dos Autonomistas, 1400 - Vila Yara, Osasco - SP",
                    -23.5391, -46.7654,
                    List.of("Celulares", "Tablets", "Carregadores", "Baterias"),
                    "Seg a Sáb, 10h-22h; Dom, 11h-21h"),
            new PontoColetaSeed(
                    "Sodimac Osasco",
                    "Av. Franz Voegeli, 751 - Continental, Osasco - SP",
                    -23.5379, -46.7634,
                    List.of("Celulares", "Tablets", "Notebooks", "Pilhas", "Baterias", "Carregadores", "Eletroportáteis"),
                    "Seg a Sáb, 8h-22h; Dom, 9h-20h"),
            new PontoColetaSeed(
                    "Casas Bahia Osasco (Centro)",
                    "R. Dona Primitiva Vianco, 132 - Centro, Osasco - SP",
                    -23.5294, -46.7759,
                    List.of("Celulares", "Pilhas", "Baterias", "Carregadores"),
                    "Seg a Sex, 8h30-19h; Sáb, 9h-17h; Dom, 9h-14h"),
            new PontoColetaSeed(
                    "CEPAC Barueri",
                    "R. Martim Afonso de Souza, 72 - Parque Imperial, Barueri - SP",
                    -23.4861, -46.8123,
                    List.of("Computadores", "Notebooks", "Monitores", "HDs e pen drives", "Celulares", "Tablets",
                            "Pilhas", "Baterias", "Cabos", "Impressoras", "Televisores", "Eletrodomésticos"),
                    "Seg a Sex, 8h-16h"),
            new PontoColetaSeed(
                    "Leroy Merlin Barueri (Tamboré)",
                    "Av. Fernando Cerqueira César Coimbra, 1000 - Alphaville Empresarial, Barueri - SP",
                    -23.5083, -46.8357,
                    List.of("Celulares", "Tablets", "Notebooks", "Pilhas", "Baterias", "Carregadores", "Eletroportáteis"),
                    "Seg a Sáb, 7h-22h; Dom, 9h-20h"),
            new PontoColetaSeed(
                    "Centro Administrativo de Cotia (Sucata Digital)",
                    "R. Jorge Caixe, 306 A - Jardim Nomura, Cotia - SP",
                    -23.6027, -46.9072,
                    TIPOS_SUCATA_DIGITAL_COTIA,
                    "Seg a Sex, 9h-17h"),
            new PontoColetaSeed(
                    "Prefeitura de Cotia (Sucata Digital)",
                    "Av. Prof. Manoel José Pedroso, 1347 - Parque Bahia, Cotia - SP",
                    -23.6061, -46.9154,
                    TIPOS_SUCATA_DIGITAL_COTIA,
                    "Seg a Sex, 8h-17h")
    );

    // ==================== Validacao ====================

    /**
     * Valida o catalogo inteiro antes de qualquer gravacao. Junta todos os
     * problemas encontrados numa unica excecao, para corrigir tudo de uma vez.
     *
     * @throws IllegalStateException se houver qualquer item invalido
     */
    void validar() {
        List<String> erros = new ArrayList<>();
        Set<String> codigosConteudo = new HashSet<>();
        Set<String> codigosFigurinha = new HashSet<>();
        Set<String> identificadoresScan = new HashSet<>();
        Set<String> nomesPonto = new HashSet<>();

        for (ConteudoSeed c : conteudos) {
            String ctx = "conteudo " + c.codigo();
            texto(erros, ctx, "codigo", c.codigo(), MAX_CODIGO, true);
            if (c.codigo() != null && !codigosConteudo.add(c.codigo())) {
                erros.add("codigo de conteudo repetido: " + c.codigo());
            }
            if (c.trilha() == null) {
                erros.add(ctx + ": trilha obrigatoria");
            }
            texto(erros, ctx, "titulo", c.titulo(), MAX_NOME, true);
            texto(erros, ctx, "corpo", c.corpo(), Integer.MAX_VALUE, true);
            texto(erros, ctx, "imagemSugerida", c.imagemSugerida(), MAX_IMAGEM_SUGERIDA, false);

            if (c.perguntas() == null || c.perguntas().isEmpty()) {
                erros.add(ctx + ": quiz sem perguntas");
            } else {
                for (int i = 0; i < c.perguntas().size(); i++) {
                    validarPergunta(erros, ctx + ", pergunta " + (i + 1), c.perguntas().get(i));
                }
            }

            FigurinhaQuizSeed f = c.figurinha();
            if (f == null) {
                erros.add(ctx + ": figurinha obrigatoria");
            } else {
                validarFigurinha(erros, codigosFigurinha, f.codigo(), f.nome(), f.descricao());
            }
        }

        for (FigurinhaScanSeed f : figurinhasScan) {
            validarFigurinha(erros, codigosFigurinha, f.codigo(), f.nome(), f.descricao());
            String id = f.identificadorScan();
            String ctx = "figurinha " + f.codigo();
            texto(erros, ctx, "identificadorScan", id, MAX_IDENTIFICADOR_SCAN, true);
            if (id != null && !IDENTIFICADOR_SCAN.matcher(id).matches()) {
                erros.add(ctx + ": identificadorScan '" + id + "' fora do padrao [A-Z0-9_]+");
            }
            if (id != null && !identificadoresScan.add(id)) {
                erros.add("identificadorScan repetido: " + id);
            }
        }

        for (PontoColetaSeed p : pontos) {
            String ctx = "ponto '" + p.nome() + "'";
            texto(erros, ctx, "nome", p.nome(), MAX_NOME, true);
            if (p.nome() != null && !nomesPonto.add(p.nome())) {
                erros.add("nome de ponto de coleta repetido: " + p.nome());
            }
            texto(erros, ctx, "endereco", p.endereco(), MAX_ENDERECO, true);
            texto(erros, ctx, "horario", p.horario(), MAX_HORARIO, false);
            if (!(p.latitude() >= -90 && p.latitude() <= 90)) {
                erros.add(ctx + ": latitude invalida (" + p.latitude() + ")");
            }
            if (!(p.longitude() >= -180 && p.longitude() <= 180)) {
                erros.add(ctx + ": longitude invalida (" + p.longitude() + ")");
            }
            if (p.tiposResiduo() == null) {
                erros.add(ctx + ": tiposResiduo obrigatorio");
            } else {
                for (String tipo : p.tiposResiduo()) {
                    texto(erros, ctx, "tipo de residuo", tipo, MAX_TIPO_RESIDUO, true);
                }
            }
        }

        if (!erros.isEmpty()) {
            throw new IllegalStateException("Catalogo do seed invalido (nada foi gravado):\n - "
                    + String.join("\n - ", erros));
        }
    }

    private static void validarPergunta(List<String> erros, String ctx, PerguntaSeed p) {
        texto(erros, ctx, "enunciado", p.enunciado(), Integer.MAX_VALUE, true);
        if (p.alternativas() == null || p.alternativas().size() != ALTERNATIVAS_POR_PERGUNTA) {
            erros.add(ctx + ": precisa ter exatamente " + ALTERNATIVAS_POR_PERGUNTA + " alternativas");
            return;
        }
        if (p.indiceCorreta() < 0 || p.indiceCorreta() >= ALTERNATIVAS_POR_PERGUNTA) {
            erros.add(ctx + ": precisa ter exatamente 1 alternativa correta (indiceCorreta="
                    + p.indiceCorreta() + ")");
        }
        for (String alternativa : p.alternativas()) {
            texto(erros, ctx, "alternativa", alternativa, MAX_TEXTO_ALTERNATIVA, true);
        }
    }

    private static void validarFigurinha(List<String> erros, Set<String> codigos,
                                         String codigo, String nome, String descricao) {
        String ctx = "figurinha " + codigo;
        texto(erros, ctx, "codigo", codigo, MAX_CODIGO, true);
        if (codigo != null && !codigos.add(codigo)) {
            erros.add("codigo de figurinha repetido: " + codigo);
        }
        texto(erros, ctx, "nome", nome, MAX_NOME, true);
        texto(erros, ctx, "descricao", descricao, MAX_DESCRICAO, true);
    }

    private static void texto(List<String> erros, String ctx, String campo, String valor,
                              int max, boolean obrigatorio) {
        if (valor == null || valor.isBlank()) {
            if (obrigatorio) {
                erros.add(ctx + ": " + campo + " obrigatorio");
            }
            return;
        }
        if (valor.length() > max) {
            erros.add(ctx + ": " + campo + " com " + valor.length() + " caracteres (maximo " + max + ")");
        }
    }
}
