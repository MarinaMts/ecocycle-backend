-- ================================================================
-- EcoCycle Backend - V2__scanner_novos_identificadores.sql
-- O reconhecimento via TFLite passa a rodar com o modelo YOLO11n-cls,
-- que reconhece pilha / mouse / ferro de passar em vez dos objetos
-- placeholder da fase anterior (ver mudancas no back-end para o
-- scanner com IA, 07/10/2026).
--
-- Reaproveita as figurinhas FIG-08/FIG-09/FIG-10 (mesmo codigo e
-- ordem), apenas trocando identificador_scan, nome e descricao.
-- Contas que ja tinham desbloqueado essas figurinhas pelos objetos
-- antigos tem esses desbloqueios apagados, para o teste comecar do
-- zero com os novos identificadores.
-- ================================================================

DELETE FROM figurinhas_usuario
WHERE figurinha_id IN (
    SELECT id FROM figurinhas WHERE codigo IN ('FIG-08', 'FIG-09', 'FIG-10')
);

UPDATE figurinhas
SET identificador_scan = 'PILHA',
    nome = 'Pilha',
    descricao = 'Pilhas têm metais como zinco e manganês, e alguns tipos têm níquel ou cádmio. Nunca vão no lixo comum: leve a um ponto de coleta de pilhas.'
WHERE codigo = 'FIG-08';

UPDATE figurinhas
SET identificador_scan = 'MOUSE',
    nome = 'Mouse',
    descricao = 'O mouse tem placa eletrônica, plástico e cobre no cabo. Descarte em um ponto de coleta de lixo eletrônico.'
WHERE codigo = 'FIG-09';

UPDATE figurinhas
SET identificador_scan = 'FERRO_PASSAR',
    nome = 'Ferro de passar',
    descricao = 'O ferro tem resistência metálica, fios de cobre e plástico. Leve a um ponto de coleta de eletroeletrônicos.'
WHERE codigo = 'FIG-10';
