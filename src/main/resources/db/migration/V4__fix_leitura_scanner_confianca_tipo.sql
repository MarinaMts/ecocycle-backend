-- ================================================================
-- EcoCycle Backend - V4__fix_leitura_scanner_confianca_tipo.sql
-- A V3 criou leitura_scanner.confianca como DECIMAL(5,2), mas a
-- entidade LeituraScanner.confianca e Double (mapeado para
-- DOUBLE PRECISION) - o mesmo padrao usado em PontoColeta.latitude/
-- longitude (V1). Com ddl-auto=validate em producao, essa divergencia
-- de tipo impedia o entityManagerFactory de subir.
-- ================================================================

ALTER TABLE leitura_scanner
    ALTER COLUMN confianca TYPE DOUBLE PRECISION USING confianca::double precision;
