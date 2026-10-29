INSERT INTO figurinhas (codigo, nome, tipo, identificador_scan, descricao, ordem)
SELECT novas.codigo, novas.nome, 'SCAN', novas.identificador_scan, novas.descricao, novas.ordem
FROM (VALUES
    ('FIG-11', 'Celular', 'CELULAR',
     'O celular tem bateria de íon-lítio e uma placa com cobre, ouro e chumbo. Antes de descartar, apague seus dados e retire o chip. Leve a um ponto de coleta de eletrônicos.',
     11),
    ('FIG-12', 'Monitor', 'MONITOR',
     'Monitores antigos de tubo têm chumbo no vidro e alguns LCD antigos têm mercúrio nas lâmpadas. Não quebre: leve o monitor inteiro a um ponto de coleta de eletroeletrônicos.',
     12),
    ('FIG-13', 'Teclado', 'TECLADO',
     'O teclado tem plástico, membrana de silicone, fios de cobre e uma placa eletrônica. Se ainda funciona, doe; se não, leve a um ponto de coleta de lixo eletrônico.',
     13),
    ('FIG-14', 'Notebook', 'NOTEBOOK',
     'O notebook tem bateria de íon-lítio, placa-mãe com metais e um disco com seus dados. Apague o disco com segurança antes de descartar e leve a um ponto de coleta.',
     14),
    ('FIG-15', 'Placa de circuito', 'PLACA_CIRCUITO',
     'A placa de circuito tem cobre, ouro e prata, mas também solda de chumbo. Nunca queime nem jogue no lixo comum: na reciclagem, os metais são recuperados.',
     15),
    ('FIG-16', 'HD', 'HD',
     'O HD tem discos metálicos, ímãs de neodímio e uma placa com chumbo. Ele guarda seus dados: apague com limpeza segura antes de descartar e leve a um ponto de coleta.',
     16)
) AS novas (codigo, nome, identificador_scan, descricao, ordem)
WHERE EXISTS (SELECT 1 FROM figurinhas WHERE codigo = 'FIG-10')
  AND NOT EXISTS (SELECT 1 FROM figurinhas f WHERE f.codigo = novas.codigo);
