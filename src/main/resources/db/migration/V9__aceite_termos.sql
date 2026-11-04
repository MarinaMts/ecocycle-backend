-- Registro do aceite dos Termos de Uso e da Politica de Privacidade no
-- cadastro (comprovacao do consentimento, LGPD). Nulo para contas criadas
-- antes desta mudanca ou por versoes do app que ainda nao enviam o aceite.
ALTER TABLE users ADD COLUMN termos_aceitos_em TIMESTAMP;
ALTER TABLE users ADD COLUMN termos_versao VARCHAR(20);
