INSERT INTO figurinhas (codigo, nome, tipo, identificador_scan, descricao, ordem)
SELECT novas.codigo, novas.nome, 'SCAN', novas.identificador_scan, novas.descricao, novas.ordem
FROM (VALUES
    ('FIG-17', 'Fonte de alimentação', 'FONTE',
     'A fonte tem transformador com cobre, capacitores e uma placa com chumbo. Não abra: os capacitores podem dar choque. Leve inteira a um ponto de coleta de eletrônicos.',
     17),
    ('FIG-18', 'Pendrive', 'PENDRIVE',
     'O pendrive tem chip de memória e uma pequena placa com metais. Ele guarda seus dados: apague tudo antes de descartar e leve a um ponto de coleta.',
     18)
) AS novas (codigo, nome, identificador_scan, descricao, ordem)
WHERE EXISTS (SELECT 1 FROM figurinhas WHERE codigo = 'FIG-10')
  AND NOT EXISTS (SELECT 1 FROM figurinhas f WHERE f.codigo = novas.codigo);
