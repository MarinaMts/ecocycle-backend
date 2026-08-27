package com.ecocycle.backend.seed;

import com.ecocycle.backend.entity.ConteudoEducativo;
import com.ecocycle.backend.entity.Figurinha;
import com.ecocycle.backend.entity.PontoColeta;
import com.ecocycle.backend.entity.Quiz;
import com.ecocycle.backend.enums.TipoFigurinha;
import com.ecocycle.backend.enums.Trilha;
import com.ecocycle.backend.repository.ConteudoEducativoRepository;
import com.ecocycle.backend.repository.FigurinhaRepository;
import com.ecocycle.backend.repository.PontoColetaRepository;
import com.ecocycle.backend.repository.QuizRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Seed fixo do conteudo educativo, quizzes e album de figurinhas
 * (sem painel de administracao nesta fase - ver documento de decisoes
 * tecnicas, secao 2). Roda apenas se a base ainda estiver vazia,
 * para nao duplicar dados a cada reinicio da aplicacao.
 *
 * Conteudo de origem: EcoCycle_Conteudo_Educativo_Trilhas.docx
 * (Trilha 1 - Os 4Rs: EDU-01 a EDU-04 | Trilha 2 - Lixo Eletronico: EDU-05 a EDU-07)
 */
@Component
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);
    private static final String[] LETRAS = {"a", "b", "c", "d"};

    private final ConteudoEducativoRepository conteudoRepository;
    private final QuizRepository quizRepository;
    private final FigurinhaRepository figurinhaRepository;
    private final PontoColetaRepository pontoColetaRepository;

    public DataSeeder(
            ConteudoEducativoRepository conteudoRepository,
            QuizRepository quizRepository,
            FigurinhaRepository figurinhaRepository,
            PontoColetaRepository pontoColetaRepository
    ) {
        this.conteudoRepository = conteudoRepository;
        this.quizRepository = quizRepository;
        this.figurinhaRepository = figurinhaRepository;
        this.pontoColetaRepository = pontoColetaRepository;
    }

    @Override
    public void run(String... args) {
        seedConteudoEducativoSeNecessario();
        seedPontosColetaSeNecessario();
    }

    private void seedConteudoEducativoSeNecessario() {
        if (conteudoRepository.count() > 0) {
            log.info("Seed de conteudo educativo ja existente - pulando (idempotente).");
            return;
        }

        log.info("Executando seed de conteudo educativo, quizzes e album de figurinhas...");

        ConteudoEducativo edu01 = criarEdu01Reciclar();
        criarQuizEdu01(edu01);
        criarFigurinhaParaConteudo("FIG-01", "Reciclador Urbano", edu01, 1,
                "Concedida ao concluir o quiz de Reciclar. Representa o processo de mineração urbana.");

        ConteudoEducativo edu02 = criarEdu02Reutilizar();
        criarQuizEdu02(edu02);
        criarFigurinhaParaConteudo("FIG-02", "Segunda Vida", edu02, 2,
                "Concedida ao concluir o quiz de Reutilizar. Representa um aparelho ganhando um novo uso.");

        ConteudoEducativo edu03 = criarEdu03Reduzir();
        criarQuizEdu03(edu03);
        criarFigurinhaParaConteudo("FIG-03", "Consumo Consciente", edu03, 3,
                "Concedida ao concluir o quiz de Reduzir. Representa a economia circular.");

        ConteudoEducativo edu04 = criarEdu04Repensar();
        criarQuizEdu04(edu04);
        criarFigurinhaParaConteudo("FIG-04", "Mão na Massa", edu04, 4,
                "Concedida ao concluir o quiz de Repensar/Reparar. Representa o direito ao reparo.");

        ConteudoEducativo edu05 = criarEdu05OQueELixoEletronico();
        criarQuizEdu05(edu05);
        criarFigurinhaParaConteudo("FIG-05", "Guardião do REEE", edu05, 5,
                "Concedida ao concluir o quiz sobre o que é lixo eletrônico.");

        ConteudoEducativo edu06 = criarEdu06ComponentesToxicos();
        criarQuizEdu06(edu06);
        criarFigurinhaParaConteudo("FIG-06", "Escudo Tóxico", edu06, 6,
                "Concedida ao concluir o quiz sobre componentes tóxicos.");

        ConteudoEducativo edu07 = criarEdu07OndeComoDescartar();
        criarQuizEdu07(edu07);
        criarFigurinhaParaConteudo("FIG-07", "Ponto de Coleta", edu07, 7,
                "Concedida ao concluir o quiz sobre onde e como descartar.");

        criarFigurinhasScanPlaceholder();

        log.info("Seed concluido: 7 conteudos, 7 quizzes (35 perguntas), 10 figurinhas.");
    }

    // ==================== EDU-01 · Reciclar ====================

    private ConteudoEducativo criarEdu01Reciclar() {
        String corpo = """
                ## O que é

                Reciclar é transformar o que já foi usado em matéria-prima de novo. No lixo eletrônico, isso significa desmontar aparelhos peça por peça e separar metais, plásticos, vidro e componentes eletrônicos para que voltem à indústria como insumo — em vez de irem parar num aterro ou numa gaveta esquecida.

                ## O tamanho do problema no Brasil

                - Só 3% do lixo eletrônico brasileiro é coletado e reciclado oficialmente
                - O restante some em gavetas, vai para o lixo comum ou é descartado irregularmente
                - O Brasil é um dos maiores geradores de lixo eletrônico da América Latina, mas um dos que menos recicla proporcionalmente

                ## O que tem dentro de um celular

                Um smartphone comum contém mais de 30 elementos químicos diferentes, incluindo:

                - Metais comuns: alumínio, cobre, ferro
                - Metais preciosos: ouro, prata, paládio
                - Terras-raras: usadas em telas e vibração
                - Plásticos de diferentes composições

                ## Você sabia?

                Uma tonelada de placas de circuito de celulares pode conter mais ouro do que uma tonelada de minério extraído diretamente de uma mina. Por isso o setor de reciclagem eletrônica é chamado, às vezes, de "mineração urbana".

                ## Por que reciclar eletrônico é diferente

                Reciclar plástico ou papel é relativamente simples: separa, lava, tritura. Eletrônico não funciona assim:

                - Os materiais estão miniaturizados e misturados dentro de placas e chips
                - Separar exige processos químicos e mecânicos especializados
                - Alguns componentes precisam de tratamento específico por serem tóxicos

                Por isso existem os PEVs (Pontos de Entrega Voluntária) — pontos específicos que encaminham o material para empresas certificadas, capazes de fazer essa separação com segurança.

                ## O caminho do material depois da coleta

                - Aparelho chega ao ponto de coleta
                - Empresa certificada faz a triagem e desmontagem
                - Materiais são separados por tipo (metais, plásticos, componentes tóxicos)
                - Cada fração segue para reaproveitamento industrial específico
                - Metais preciosos e comuns voltam para a cadeia produtiva

                ## Impacto além do ambiental

                Reciclar eletrônico também gera emprego e renda. Cooperativas de catadores e empresas de logística reversa dependem desse fluxo de materiais para funcionar — o que conecta sustentabilidade ambiental a impacto econômico e social direto nas comunidades envolvidas.
                """;

        ConteudoEducativo conteudo = ConteudoEducativo.builder()
                .codigo("EDU-01")
                .trilha(Trilha.OS_4RS)
                .titulo("Reciclar")
                .corpo(corpo)
                .ordem(1)
                .imagemSugerida("Celular \"explodido\" em camadas — carcaça, placa, bateria, tela — com ícones indicando o destino de cada material.")
                .build();
        return conteudoRepository.save(conteudo);
    }

    private void criarQuizEdu01(ConteudoEducativo conteudo) {
        Quiz quiz = Quiz.builder().conteudoEducativo(conteudo).xpRecompensa(10).build();
        quiz = quizRepository.save(quiz);

        List<Object[]> perguntas = new ArrayList<>();
        perguntas.add(new Object[]{
                "O que significa, na prática, reciclar um aparelho eletrônico?",
                new String[]{
                        "Guardar o aparelho na gaveta indefinidamente",
                        "Separar e reaproveitar os materiais como matéria-prima industrial",
                        "Jogar o aparelho em qualquer lixeira",
                        "Vender o aparelho usado sem conserto"
                }, 1
        });
        perguntas.add(new Object[]{
                "Qual é a taxa oficial de coleta e reciclagem de lixo eletrônico no Brasil?",
                new String[]{"Cerca de 30%", "Cerca de 50%", "Apenas 3%", "Praticamente 100%"}, 2
        });
        perguntas.add(new Object[]{
                "Por que reciclar eletrônico exige um processo diferente do de plástico ou papel?",
                new String[]{
                        "Porque não existe reciclagem de eletrônicos",
                        "Porque os materiais estão miniaturizados e misturados, exigindo separação especializada",
                        "Porque eletrônicos não têm valor de mercado",
                        "Porque é proibido reciclar esse tipo de material"
                }, 1
        });
        perguntas.add(new Object[]{
                "O que são os PEVs, citados no texto?",
                new String[]{
                        "Peças Eletrônicas Vendáveis",
                        "Pontos de Entrega Voluntária",
                        "Produtos Eletroeletrônicos Vencidos",
                        "Programas de Extração de Valor"
                }, 1
        });
        perguntas.add(new Object[]{
                "Além do benefício ambiental, que outro impacto a reciclagem de eletrônicos gera, segundo o texto?",
                new String[]{
                        "Nenhum, o impacto é só ambiental",
                        "Geração de emprego e renda em cooperativas e empresas de logística reversa",
                        "Aumento do preço dos aparelhos novos",
                        "Redução da qualidade dos materiais reciclados"
                }, 1
        });

        salvarPerguntas(quiz, perguntas);
    }

    // ==================== EDU-02 · Reutilizar ====================

    private ConteudoEducativo criarEdu02Reutilizar() {
        String corpo = """
                ## O que é

                Reutilizar é dar um novo uso a algo antes de sequer cogitar o descarte. É a etapa que vem antes da reciclagem — porque manter um objeto em uso é sempre mais eficiente do que desmontá-lo para recuperar materiais.

                ## Exemplos práticos do dia a dia

                - Celular antigo → câmera de segurança caseira ou tocador de música offline
                - Notebook lento → máquina dedicada só para tarefas simples (estudo, streaming)
                - Cabo ou carregador "sobrando" → reserva para outro aparelho compatível
                - Bateria externa de equipamento antigo → alimentação de pequenos projetos eletrônicos

                ## A lógica por trás da reutilização

                Quanto mais tempo um objeto permanece em uso, menos recursos naturais novos precisam ser extraídos para substituí-lo. Isso parece óbvio, mas o impacto é grande quando multiplicado por milhões de aparelhos.

                ## O custo escondido de "só comprar outro"

                Fabricar um aparelho novo do zero exige:

                - Mineração de lítio e cobalto (baterias)
                - Mineração de ouro e terras-raras (componentes internos e tela)
                - Processos industriais com alto consumo de água e energia
                - Em algumas regiões produtoras, condições de trabalho precárias na extração mineral

                Estender a vida útil de um único aparelho por mais 1 ou 2 anos já reduz proporcionalmente toda essa demanda por matéria-prima nova.

                ## O lado social da reutilização

                - Plataformas de doação conectam quem quer se desfazer de um aparelho funcional com quem precisa
                - ONGs recondicionam computadores antigos para uso em escolas públicas e projetos sociais
                - Bazares de troca e grupos de reutilização comunitária ganham força em várias cidades brasileiras

                Um aparelho "velho demais" pra você pode ser o primeiro computador de alguém.

                ## Reaproveitar peças também é reutilizar

                Uma tela quebrada não significa necessariamente o fim do aparelho inteiro. Componentes internos como bateria, câmera, alto-falante ou módulo de memória muitas vezes continuam funcionando perfeitamente — e podem virar peça de reposição em outro conserto.

                ## A pergunta que resolve muita coisa

                Antes de descartar, vale sempre parar e perguntar: "isso realmente não serve mais pra nada, ou só não serve mais pra mim?" Na maioria das vezes, a resposta abre uma segunda vida para o aparelho.
                """;

        ConteudoEducativo conteudo = ConteudoEducativo.builder()
                .codigo("EDU-02")
                .trilha(Trilha.OS_4RS)
                .titulo("Reutilizar")
                .corpo(corpo)
                .ordem(2)
                .imagemSugerida("Sequência ilustrada — celular antigo se transformando em câmera de segurança, depois em doação, depois em peças de reposição.")
                .build();
        return conteudoRepository.save(conteudo);
    }

    private void criarQuizEdu02(ConteudoEducativo conteudo) {
        Quiz quiz = Quiz.builder().conteudoEducativo(conteudo).xpRecompensa(10).build();
        quiz = quizRepository.save(quiz);

        List<Object[]> perguntas = new ArrayList<>();
        perguntas.add(new Object[]{
                "O que é reutilizar, segundo o texto?",
                new String[]{
                        "Reciclar o metal de um aparelho",
                        "Comprar um aparelho novo mais eficiente",
                        "Dar um novo uso a um objeto antes de pensar em descartá-lo",
                        "Deixar o aparelho guardado sem nenhum uso"
                }, 2
        });
        perguntas.add(new Object[]{
                "Por que a reutilização reduz o impacto ambiental?",
                new String[]{
                        "Porque aumenta a produção de aparelhos novos",
                        "Porque reduz a necessidade de extrair novos recursos naturais",
                        "Porque gera mais lixo eletrônico",
                        "Ela não tem relação com o meio ambiente"
                }, 1
        });
        perguntas.add(new Object[]{
                "Quais materiais são citados como parte do custo escondido de fabricar um aparelho novo?",
                new String[]{
                        "Apenas plástico e vidro",
                        "Lítio, cobalto, ouro e terras-raras",
                        "Somente papel e alumínio",
                        "Nenhum material específico é citado"
                }, 1
        });
        perguntas.add(new Object[]{
                "Qual exemplo de reutilização de peças o texto menciona?",
                new String[]{
                        "Bateria, câmera ou memória de um aparelho com tela quebrada podem ser reaproveitadas",
                        "Jogar o aparelho inteiro fora quando a tela quebra",
                        "Comprar peças novas sempre que possível",
                        "Reciclar apenas o vidro da tela"
                }, 0
        });
        perguntas.add(new Object[]{
                "Qual pergunta o texto sugere fazer antes de descartar um aparelho?",
                new String[]{
                        "\"Quanto esse aparelho ainda vale?\"",
                        "\"Isso realmente não serve mais pra nada, ou só não serve mais pra mim?\"",
                        "\"Onde posso comprar um substituto?\"",
                        "\"Quem vai reciclar isso por mim?\""
                }, 1
        });

        salvarPerguntas(quiz, perguntas);
    }

    // ==================== EDU-03 · Reduzir ====================

    private ConteudoEducativo criarEdu03Reduzir() {
        String corpo = """
                ## O que é

                Reduzir é comprar e consumir com mais consciência, evitando gerar lixo eletrônico que não precisaria existir. Isso não significa nunca trocar de aparelho — significa questionar a pressa de trocar por moda, lançamento ou pressão social, quando o atual ainda cumpre bem sua função.

                ## O nome desse problema: obsolescência programada

                É a estratégia — às vezes proposital, às vezes um efeito colateral de decisões de projeto — de fazer aparelhos durarem um tempo limitado, empurrando o consumidor para a troca constante. Aparece de formas nem sempre óbvias:

                - Atualizações de software que deixam aparelhos antigos visivelmente mais lentos
                - Baterias não substituíveis, que se degradam com o tempo
                - Peças de reposição difíceis de encontrar ou caras demais
                - Design que "cola" componentes internos, dificultando qualquer abertura do aparelho

                ## Os números do problema no Brasil

                - 2,4 milhões de toneladas de lixo eletrônico geradas em 2022
                - Equivalente a 10,2 kg por pessoa — um dos maiores volumes per capita da América Latina
                - Tendência de crescimento, puxada por wearables e gadgets conectados

                ## Reduzir na prática — perguntas antes de comprar

                - Isso é uma necessidade real ou um impulso de momento?
                - O aparelho atual ainda cumpre a função, mesmo que não seja o mais recente?
                - A marca tem histórico de dar suporte de software por mais tempo?
                - Existe opção de comprar recondicionado (seminovo certificado) em vez de novo?

                ## Pensando no ciclo de vida completo

                Reduzir também é pensar além do momento da compra: de onde vêm os materiais, quanto tempo o aparelho vai durar, e o que vai acontecer com ele quando não servir mais. Esse olhar de "ciclo completo" é a base do conceito de economia circular, que tenta fechar esse ciclo em vez de deixá-lo linear (extrair → usar → descartar).
                """;

        ConteudoEducativo conteudo = ConteudoEducativo.builder()
                .codigo("EDU-03")
                .trilha(Trilha.OS_4RS)
                .titulo("Reduzir")
                .corpo(corpo)
                .ordem(3)
                .imagemSugerida("Infográfico comparando \"consumo consciente\" (seta circular fechada) vs. \"consumo acelerado\" (pilha crescente de eletrônicos descartados).")
                .build();
        return conteudoRepository.save(conteudo);
    }

    private void criarQuizEdu03(ConteudoEducativo conteudo) {
        Quiz quiz = Quiz.builder().conteudoEducativo(conteudo).xpRecompensa(10).build();
        quiz = quizRepository.save(quiz);

        List<Object[]> perguntas = new ArrayList<>();
        perguntas.add(new Object[]{
                "O que significa reduzir, no contexto de consumo de eletrônicos?",
                new String[]{
                        "Nunca comprar nenhum aparelho novo",
                        "Consumir com mais consciência, evitando lixo eletrônico desnecessário",
                        "Comprar sempre o modelo mais barato disponível",
                        "Reciclar todos os aparelhos antigos imediatamente"
                }, 1
        });
        perguntas.add(new Object[]{
                "O que é obsolescência programada?",
                new String[]{
                        "Um tipo de certificação ambiental",
                        "A estratégia de fazer aparelhos durarem um tempo limitado, incentivando a troca",
                        "Um método de reciclagem de baterias",
                        "Uma lei brasileira sobre descarte de eletrônicos"
                }, 1
        });
        perguntas.add(new Object[]{
                "Quantas toneladas de lixo eletrônico o Brasil gerou em 2022?",
                new String[]{"500 mil toneladas", "2,4 milhões de toneladas", "10 milhões de toneladas", "100 mil toneladas"}, 1
        });
        perguntas.add(new Object[]{
                "Qual exemplo de obsolescência programada o texto cita?",
                new String[]{
                        "Baterias facilmente substituíveis",
                        "Atualizações de software que deixam aparelhos antigos mais lentos",
                        "Peças de reposição baratas e fáceis de achar",
                        "Design pensado para facilitar reparo"
                }, 1
        });
        perguntas.add(new Object[]{
                "O que é economia circular, segundo o texto?",
                new String[]{
                        "Um modelo linear de extrair, usar e descartar",
                        "Uma forma de pensar o ciclo de vida completo do produto, do material ao descarte",
                        "Um tipo específico de bateria recarregável",
                        "Um programa de troca de celulares por dinheiro"
                }, 1
        });

        salvarPerguntas(quiz, perguntas);
    }

    // ==================== EDU-04 · Repensar/Reparar ====================

    private ConteudoEducativo criarEdu04Repensar() {
        String corpo = """
                ## O que é

                Repensar é questionar hábitos de consumo antes mesmo de chegar à hora de descartar — parar e avaliar se a troca é realmente necessária. Reparar é a ação mais concreta dessa reflexão: em vez de descartar um eletrônico com defeito, consertá-lo e devolvê-lo ao uso normal.

                ## Por que reparar quase sempre compensa

                - Trocar bateria, tela ou conector geralmente custa uma fração do preço de um aparelho novo
                - Evita que um produto ainda "salvável" vire lixo eletrônico antes da hora
                - Reduz a demanda por matéria-prima nova e por todo o processo industrial de fabricação

                ## Onde encontrar ajuda para reparar

                - Assistências técnicas tradicionais, autorizadas ou independentes
                - Cooperativas de reparo comunitário, cada vez mais comuns em grandes cidades
                - Tutoriais detalhados online para quem quer aprender a consertar sozinho

                ## O movimento "direito ao reparo" (right to repair)

                É um debate global, que também chegou ao Brasil, sobre até que ponto fabricantes deveriam facilitar — ou dificultar — o conserto de seus próprios produtos. As principais demandas incluem:

                - Venda de peças de reposição originais para o público geral
                - Disponibilização de manuais técnicos e diagramas de reparo
                - Design que permita abertura e troca de peças sem "colagem" excessiva
                - Preços justos para peças e serviços de reparo

                Vários países já aprovaram legislações específicas sobre o tema, pressionando empresas de tecnologia a repensar como projetam seus produtos desde o início.

                ## O lado emocional do "repensar"

                Muitas vezes, um risco na tela, uma bateria que dura um pouco menos ou uma lentidão pontual são interpretados como "motivo suficiente" para trocar de aparelho — quando, na real, são apenas sinais de que está na hora de um reparo simples e pontual, não de um descarte completo.

                ## Reparar como hábito, não exceção

                Incorporar o reparo como primeira opção — e não como último recurso — muda a relação com os aparelhos: em vez de "usar até quebrar e trocar", passa a ser "usar, cuidar, consertar quando precisar, e só trocar quando realmente não há mais alternativa".
                """;

        ConteudoEducativo conteudo = ConteudoEducativo.builder()
                .codigo("EDU-04")
                .trilha(Trilha.OS_4RS)
                .titulo("Repensar/Reparar")
                .corpo(corpo)
                .ordem(4)
                .imagemSugerida("Pessoa consertando um celular sobre uma mesa — chave de fenda, tela removida, peças organizadas ao lado.")
                .build();
        return conteudoRepository.save(conteudo);
    }

    private void criarQuizEdu04(ConteudoEducativo conteudo) {
        Quiz quiz = Quiz.builder().conteudoEducativo(conteudo).xpRecompensa(10).build();
        quiz = quizRepository.save(quiz);

        List<Object[]> perguntas = new ArrayList<>();
        perguntas.add(new Object[]{
                "O que é \"repensar\", no contexto do consumo de eletrônicos?",
                new String[]{
                        "Comprar o quanto antes um aparelho novo",
                        "Questionar se a troca de aparelho é realmente necessária",
                        "Ignorar o problema do lixo eletrônico",
                        "Vender o aparelho velho sem conserto"
                }, 1
        });
        perguntas.add(new Object[]{
                "O que significa reparar um eletrônico?",
                new String[]{
                        "Descartá-lo e comprar outro imediatamente",
                        "Consertar o defeito para que o aparelho volte a funcionar",
                        "Reciclar todas as peças do aparelho",
                        "Doá-lo para reciclagem sem tentar consertar"
                }, 1
        });
        perguntas.add(new Object[]{
                "Por que reparar costuma ser vantajoso financeiramente?",
                new String[]{
                        "Porque é sempre mais caro que comprar um aparelho novo",
                        "Porque geralmente custa uma fração do preço de um aparelho novo",
                        "Porque gera mais lixo eletrônico",
                        "Não há vantagem financeira nenhuma"
                }, 1
        });
        perguntas.add(new Object[]{
                "O que o movimento \"direito ao reparo\" defende?",
                new String[]{
                        "Proibir qualquer conserto de eletrônicos por terceiros",
                        "Que fabricantes facilitem o conserto dos próprios produtos",
                        "Que só técnicos autorizados pela fábrica possam abrir aparelhos",
                        "Que aparelhos sejam descartados após 1 ano de uso"
                }, 1
        });
        perguntas.add(new Object[]{
                "Segundo o texto, o que geralmente indica que é hora de um reparo simples, e não de descarte total?",
                new String[]{
                        "Qualquer sinal de uso do aparelho",
                        "Um risco na tela ou uma bateria que dura um pouco menos",
                        "A cor do aparelho estar fora de moda",
                        "O lançamento de um modelo mais recente"
                }, 1
        });

        salvarPerguntas(quiz, perguntas);
    }

    // ==================== EDU-05 · O que é lixo eletrônico ====================

    private ConteudoEducativo criarEdu05OQueELixoEletronico() {
        String corpo = """
                ## O que é

                Lixo eletrônico — tecnicamente chamado de REEE (Resíduos de Equipamentos Elétricos e Eletrônicos) — é qualquer aparelho movido a eletricidade ou bateria que chegou ao fim da sua vida útil. A categoria é bem mais ampla do que parece à primeira vista.

                ## Exemplos que entram nessa categoria

                - Celulares, tablets e notebooks
                - Carregadores, cabos e fones de ouvido
                - Computadores de mesa e periféricos (teclado, mouse, monitor)
                - Eletrodomésticos (liquidificador, micro-ondas, geladeira)
                - Brinquedos eletrônicos e videogames
                - Pilhas, baterias e lâmpadas fluorescentes
                - Equipamentos médicos e de escritório fora de uso

                ## Por que essa categoria cresce tão rápido

                - A popularização de dispositivos aumenta o volume total em circulação
                - O ciclo de troca ficou mais curto — muita gente troca de celular a cada 2-3 anos
                - Novas categorias de gadgets (wearables, casa inteligente) somam volume que nem existia há uma década
                - O crescimento é mais rápido do que a capacidade de reciclagem consegue acompanhar

                ## A diferença crucial em relação a outros tipos de lixo

                Lixo orgânico se decompõe naturalmente em semanas ou poucos meses. O lixo eletrônico é o oposto: plástico, metal e componentes eletrônicos podem levar centenas de anos para se degradar — liberando substâncias nocivas de forma lenta e contínua ao longo desse tempo todo.

                ## Por que precisa de tratamento completamente separado

                - Não pode ir para aterro sanitário comum, junto com lixo doméstico
                - Não pode ir na reciclagem convencional (papel, plástico, vidro e metal misturados)
                - Precisa passar por desmontagem controlada, feita por empresas ou cooperativas especializadas
                """;

        ConteudoEducativo conteudo = ConteudoEducativo.builder()
                .codigo("EDU-05")
                .trilha(Trilha.LIXO_ELETRONICO)
                .titulo("O que é lixo eletrônico")
                .corpo(corpo)
                .ordem(1)
                .imagemSugerida("Composição com múltiplos exemplos de lixo eletrônico reunidos — celular, carregador, fone, pilha, controle remoto, pequeno eletrodoméstico.")
                .build();
        return conteudoRepository.save(conteudo);
    }

    private void criarQuizEdu05(ConteudoEducativo conteudo) {
        Quiz quiz = Quiz.builder().conteudoEducativo(conteudo).xpRecompensa(10).build();
        quiz = quizRepository.save(quiz);

        List<Object[]> perguntas = new ArrayList<>();
        perguntas.add(new Object[]{
                "O que significa a sigla REEE?",
                new String[]{
                        "Reciclagem de Equipamentos Elétricos Estatais",
                        "Resíduos de Equipamentos Elétricos e Eletrônicos",
                        "Rede de Empresas de Eletrônicos",
                        "Reaproveitamento de Energia Elétrica Estruturada"
                }, 1
        });
        perguntas.add(new Object[]{
                "Qual das opções abaixo é um exemplo de lixo eletrônico?",
                new String[]{"Casca de fruta", "Carregador de celular quebrado", "Jornal velho", "Garrafa de vidro"}, 1
        });
        perguntas.add(new Object[]{
                "O que diferencia o lixo eletrônico do lixo orgânico?",
                new String[]{
                        "Ele se decompõe mais rápido que o orgânico",
                        "Ele não se decompõe naturalmente e pode levar séculos para se degradar",
                        "Ele é sempre mais leve que o lixo comum",
                        "Não existe diferença relevante entre os dois"
                }, 1
        });
        perguntas.add(new Object[]{
                "Por que o lixo eletrônico não pode ir para a reciclagem convencional?",
                new String[]{
                        "Porque é proibido reciclar qualquer tipo de eletrônico",
                        "Porque exige desmontagem controlada e separação especializada de materiais",
                        "Porque não tem nenhum valor de reaproveitamento",
                        "Porque a reciclagem convencional é mais eficiente para eletrônicos"
                }, 1
        });
        perguntas.add(new Object[]{
                "O que impulsiona o crescimento acelerado do volume de lixo eletrônico no mundo?",
                new String[]{
                        "A queda no uso de dispositivos eletrônicos",
                        "A popularização de dispositivos e o ciclo de troca cada vez mais curto",
                        "O aumento da reciclagem em todos os países",
                        "A proibição de novos lançamentos tecnológicos"
                }, 1
        });

        salvarPerguntas(quiz, perguntas);
    }

    // ==================== EDU-06 · Componentes tóxicos ====================

    private ConteudoEducativo criarEdu06ComponentesToxicos() {
        String corpo = """
                ## O que existe dentro de um aparelho "comum"

                Aparelhos eletrônicos do dia a dia carregam substâncias que, quando descartadas incorretamente, contaminam solo e água e podem afetar seriamente a saúde. Três das mais conhecidas são chumbo, mercúrio e cádmio — mas não são as únicas.

                ## Chumbo

                - Onde está: soldas de placas de circuito, algumas baterias antigas
                - Risco à saúde: danos ao sistema nervoso central, especialmente perigoso para crianças
                - Persistência: não se degrada no ambiente, acumula-se no solo por longos períodos

                ## Mercúrio

                - Onde está: telas mais antigas (monitores CRT), certas lâmpadas fluorescentes
                - Risco à saúde: tóxico mesmo em pequenas quantidades; afeta sistema nervoso e rins
                - Particularidade: pode se acumular na cadeia alimentar em forma ainda mais perigosa

                ## Cádmio

                - Onde está: baterias recarregáveis mais antigas (níquel-cádmio)
                - Risco à saúde: classificado como substância cancerígena, acumula-se no organismo

                ## Outros elementos de atenção

                - Retardantes de chama bromados: usados em plásticos de carcaças, associados a distúrbios hormonais
                - Berílio: presente em alguns conectores e placas, tóxico por inalação de poeira
                - Ácidos e eletrólitos de bateria: corrosivos, podem causar queimaduras

                ## Como a contaminação acontece na prática

                - Aparelho descartado incorretamente vai parar em aterro comum ou é abandonado a céu aberto
                - Chuva e variação de temperatura degradam aos poucos a estrutura do aparelho
                - Substâncias tóxicas começam a vazar para o solo ao redor
                - Com o tempo, podem atingir o lençol freático — água usada para abastecimento humano
                - A contaminação vira um problema de saúde pública, muitas vezes silencioso

                ## Por que a reciclagem especializada é a solução

                Empresas certificadas usam processos controlados para separar os materiais tóxicos com segurança, tratar cada substância adequadamente e garantir rastreabilidade do material do início ao fim da cadeia de reciclagem.
                """;

        ConteudoEducativo conteudo = ConteudoEducativo.builder()
                .codigo("EDU-06")
                .trilha(Trilha.LIXO_ELETRONICO)
                .titulo("Componentes tóxicos")
                .corpo(corpo)
                .ordem(2)
                .imagemSugerida("Diagrama tipo raio-x de um celular ou placa de circuito, com setas numeradas indicando onde ficam chumbo, mercúrio, cádmio e outros elementos de risco.")
                .build();
        return conteudoRepository.save(conteudo);
    }

    private void criarQuizEdu06(ConteudoEducativo conteudo) {
        Quiz quiz = Quiz.builder().conteudoEducativo(conteudo).xpRecompensa(10).build();
        quiz = quizRepository.save(quiz);

        List<Object[]> perguntas = new ArrayList<>();
        perguntas.add(new Object[]{
                "Onde geralmente é encontrado o chumbo em um eletrônico?",
                new String[]{"Na tela sensível ao toque", "Em soldas de placas de circuito", "No microfone", "Na capa protetora do aparelho"}, 1
        });
        perguntas.add(new Object[]{
                "Em quais componentes o cádmio costuma estar presente?",
                new String[]{"Cabos USB", "Baterias recarregáveis mais antigas", "Fones de ouvido", "Capas protetoras"}, 1
        });
        perguntas.add(new Object[]{
                "O que pode acontecer quando materiais tóxicos vão parar em um aterro comum?",
                new String[]{
                        "Eles evaporam sem causar nenhum dano",
                        "Podem vazar para o solo e contaminar lençóis freáticos",
                        "Se transformam naturalmente em adubo",
                        "Viram plástico reciclável automaticamente"
                }, 1
        });
        perguntas.add(new Object[]{
                "Por que a contaminação de lençóis freáticos é especialmente preocupante?",
                new String[]{
                        "Porque afeta apenas espécies marinhas",
                        "Porque essa água é usada para consumo humano e irrigação",
                        "Porque deixa a água mais gelada",
                        "Na verdade, não é considerada preocupante"
                }, 1
        });
        perguntas.add(new Object[]{
                "Qual é a função da reciclagem especializada de componentes tóxicos?",
                new String[]{
                        "Vender os materiais tóxicos separadamente",
                        "Separar e tratar os materiais tóxicos com segurança, evitando contaminação",
                        "Misturar tudo novamente no lixo comum",
                        "Aumentar a produção de novos eletrônicos"
                }, 1
        });

        salvarPerguntas(quiz, perguntas);
    }

    // ==================== EDU-07 · Onde e como descartar ====================

    private ConteudoEducativo criarEdu07OndeComoDescartar() {
        String corpo = """
                ## É mais simples do que a maioria imagina

                Existem PEVs (Pontos de Entrega Voluntária) espalhados por diversas cidades brasileiras, geralmente localizados em:

                - Lojas de eletrônicos e redes de varejo parceiras
                - Supermercados e shoppings
                - Escolas e universidades com programas de coleta
                - Pontos parceiros de organizações gestoras, como a Green Eletron

                ## O processo completo, passo a passo

                - Separe o aparelho — não é necessário desmontar nada em casa
                - Encontre o ponto mais próximo por geolocalização (função de mapa deste app)
                - Leve até o ponto de coleta — não há custo na maioria dos pontos
                - A partir daí, é com os especialistas: desmontagem, separação e reciclagem seguras

                ## Cuidados antes de descartar celular, tablet ou computador

                - Faça backup completo dos seus arquivos, fotos e contatos
                - Restaure o aparelho para a configuração de fábrica
                - Remova cartões SIM e cartões de memória externos
                - Isso evita vazamento de dados pessoais, mesmo em aparelho quebrado

                ## Atenção redobrada com pilhas e baterias

                - Nunca vão no lixo comum
                - Nunca vão na reciclagem convencional
                - Risco real de vazamento químico e até incêndio se descartadas incorretamente
                - A maioria dos pontos de coleta tem recipientes específicos só para pilhas e baterias

                ## Se não houver ponto de coleta por perto

                Em cidades menores, com rede de PEVs ainda limitada, vale guardar o aparelho em local seguro até a próxima oportunidade de descarte, em vez de jogar no lixo comum como solução imediata.

                ## O hábito que resolve praticamente tudo

                Antes de jogar qualquer coisa fora, pare e se pergunte: "existe um ponto de coleta perto de mim?" Na grande maioria das vezes, principalmente em áreas urbanas, a resposta é sim.
                """;

        ConteudoEducativo conteudo = ConteudoEducativo.builder()
                .codigo("EDU-07")
                .trilha(Trilha.LIXO_ELETRONICO)
                .titulo("Onde e como descartar")
                .corpo(corpo)
                .ordem(3)
                .imagemSugerida("Mapa estilizado com pin de localização destacando um ponto de coleta próximo — visual consistente com a tela de mapa do app.")
                .build();
        return conteudoRepository.save(conteudo);
    }

    private void criarQuizEdu07(ConteudoEducativo conteudo) {
        Quiz quiz = Quiz.builder().conteudoEducativo(conteudo).xpRecompensa(10).build();
        quiz = quizRepository.save(quiz);

        List<Object[]> perguntas = new ArrayList<>();
        perguntas.add(new Object[]{
                "O que é um PEV?",
                new String[]{
                        "Um tipo específico de bateria",
                        "Um Ponto de Entrega Voluntária para descarte de eletrônicos",
                        "Um aplicativo de reciclagem",
                        "Uma peça interna de celular"
                }, 1
        });
        perguntas.add(new Object[]{
                "É necessário desmontar o aparelho antes de levá-lo a um PEV?",
                new String[]{
                        "Sim, sempre é necessário desmontar",
                        "Não, não é necessário desmontar nada",
                        "Somente computadores precisam ser desmontados",
                        "Somente celulares precisam ser desmontados"
                }, 1
        });
        perguntas.add(new Object[]{
                "O que é recomendado fazer antes de descartar um celular ou computador?",
                new String[]{
                        "Deixar a bateria carregando por segurança",
                        "Fazer backup dos dados e restaurar a configuração de fábrica",
                        "Quebrar a tela intencionalmente",
                        "Não é necessário nenhum cuidado prévio"
                }, 1
        });
        perguntas.add(new Object[]{
                "Pilhas e baterias soltas podem ser descartadas no lixo comum?",
                new String[]{
                        "Sim, sem nenhum problema",
                        "Não — elas exigem pontos de coleta específicos",
                        "Somente pilhas grandes não podem",
                        "Depende exclusivamente da marca"
                }, 1
        });
        perguntas.add(new Object[]{
                "Quem é responsável pela separação e reciclagem correta após a coleta?",
                new String[]{
                        "O próprio usuário, em casa",
                        "Empresas especializadas e certificadas",
                        "Ninguém — o material fica apenas estocado",
                        "A prefeitura, que incinera todo o material"
                }, 1
        });

        salvarPerguntas(quiz, perguntas);
    }

    // ==================== Helpers ====================

    private void salvarPerguntas(Quiz quiz, List<Object[]> perguntasData) {
        SeedHelper.salvarPerguntas(quiz, perguntasData, LETRAS);
        // Persiste em cascata as Perguntas + Alternativas adicionadas a colecao do Quiz
        // (o Quiz ja foi salvo antes, sem filhos; este save propaga o cascade = ALL).
        quizRepository.save(quiz);
    }

    private void criarFigurinhaParaConteudo(String codigo, String nome, ConteudoEducativo conteudo, int ordem, String descricao) {
        Figurinha figurinha = Figurinha.builder()
                .codigo(codigo)
                .nome(nome)
                .tipo(TipoFigurinha.QUIZ)
                .conteudoEducativo(conteudo)
                .descricao(descricao)
                .ordem(ordem)
                .build();
        figurinhaRepository.save(figurinha);
    }

    private void criarFigurinhasScanPlaceholder() {
        // Identificadores alinhados as 3 classes reais do TFLite
        // (ver documento de decisoes tecnicas, secao 5).
        String[][] dados = {
                {"FIG-08", "Bateria Segura", "BATERIA_LITIO", "Reconhecida via scanner: Bateria de Lítio."},
                {"FIG-09", "Placa-mãe Desvendada", "PLACA_MAE", "Reconhecida via scanner: Placa-mãe."},
                {"FIG-10", "Cabo Recolhido", "CABO_USB", "Reconhecida via scanner: Cabo USB."}
        };
        int ordem = 8;
        for (String[] d : dados) {
            Figurinha figurinha = Figurinha.builder()
                    .codigo(d[0])
                    .nome(d[1])
                    .tipo(TipoFigurinha.SCAN)
                    .identificadorScan(d[2])
                    .descricao(d[3])
                    .ordem(ordem++)
                    .build();
            figurinhaRepository.save(figurinha);
        }
    }

    // ==================== Semana 3 - Pontos de Coleta ====================

    private void seedPontosColetaSeNecessario() {
        if (pontoColetaRepository.count() > 0) {
            log.info("Seed de pontos de coleta ja existente - pulando (idempotente).");
            return;
        }

        log.info("Executando seed de pontos de coleta (dados placeholder - Sao Paulo)...");

        criarPontoColeta(
                "Eco Ponto Pinheiros",
                "Rua dos Pinheiros, 500 - Pinheiros, Sao Paulo - SP",
                -23.5629, -46.6822,
                List.of("Pilhas", "Baterias", "Celulares", "Cabos"),
                "Seg a Sex, 9h-18h"
        );
        criarPontoColeta(
                "PEV Shopping Higienopolis",
                "Av. Higienopolis, 618 - Higienopolis, Sao Paulo - SP",
                -23.5411, -46.6558,
                List.of("Celulares", "Notebooks", "Pilhas", "Baterias"),
                "Todos os dias, 10h-22h"
        );
        criarPontoColeta(
                "Ecoponto Vila Madalena",
                "Rua Harmonia, 950 - Vila Madalena, Sao Paulo - SP",
                -23.5505, -46.6889,
                List.of("Eletrodomesticos pequenos", "Pilhas", "Baterias"),
                "Ter a Sab, 8h-17h"
        );
        criarPontoColeta(
                "Ponto de Coleta Mackenzie - Higienopolis",
                "Rua da Consolacao, 930 - Consolacao, Sao Paulo - SP",
                -23.5433, -46.6528,
                List.of("Celulares", "Computadores", "Pilhas", "Baterias", "Cabos"),
                "Seg a Sex, 8h-20h (durante o periodo letivo)"
        );
        criarPontoColeta(
                "Eco Coleta Paraiso",
                "Rua Bahia, 120 - Paraiso, Sao Paulo - SP",
                -23.5735, -46.6389,
                List.of("Pilhas", "Baterias", "Lampadas fluorescentes"),
                "Seg a Sex, 9h-17h"
        );
        criarPontoColeta(
                "PEV Moema",
                "Av. Ibirapuera, 2332 - Moema, Sao Paulo - SP",
                -23.6003, -46.6633,
                List.of("Celulares", "Notebooks", "Tablets", "Pilhas", "Baterias"),
                "Seg a Sab, 9h-19h"
        );

        log.info("Seed de pontos de coleta concluido: 6 pontos cadastrados.");
    }

    private void criarPontoColeta(
            String nome, String endereco, double lat, double lng,
            List<String> tiposResiduo, String horario
    ) {
        PontoColeta ponto = PontoColeta.builder()
                .nome(nome)
                .endereco(endereco)
                .latitude(lat)
                .longitude(lng)
                .tiposResiduoAceitos(new ArrayList<>(tiposResiduo))
                .horarioFuncionamento(horario)
                .ativo(true)
                .build();
        pontoColetaRepository.save(ponto);
    }
}

