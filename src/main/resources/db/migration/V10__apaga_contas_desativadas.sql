-- Antes, "excluir conta" so desativava o usuario (ativo = false) e os dados
-- continuavam no banco. Agora a exclusao e definitiva; esta migracao aplica
-- o mesmo as contas que ja tinham sido "excluidas" pelo app.
DELETE FROM quiz_respostas WHERE quiz_resultado_id IN (
    SELECT qr.id FROM quiz_resultados qr JOIN users u ON u.id = qr.user_id WHERE u.ativo = FALSE
);
DELETE FROM quiz_resultados WHERE user_id IN (SELECT id FROM users WHERE ativo = FALSE);
DELETE FROM conteudos_lidos WHERE user_id IN (SELECT id FROM users WHERE ativo = FALSE);
DELETE FROM figurinhas_usuario WHERE user_id IN (SELECT id FROM users WHERE ativo = FALSE);
DELETE FROM leitura_scanner WHERE usuario_id IN (SELECT id FROM users WHERE ativo = FALSE);
DELETE FROM users WHERE ativo = FALSE;
