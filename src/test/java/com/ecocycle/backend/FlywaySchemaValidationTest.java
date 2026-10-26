package com.ecocycle.backend;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Os demais testes usam H2 com ddl-auto=update e Flyway desligado, entao nunca
 * comparam as migrations com as entidades. Este teste reproduz o que o perfil
 * prod faz no startup: aplica as migrations via Flyway e roda o Hibernate com
 * ddl-auto=validate. Se uma entidade divergir do schema das migrations, o
 * contexto nao sobe e o teste falha.
 *
 * H2 em modo PostgreSQL e uma aproximacao (nao substitui o Postgres real),
 * mas pega desencontros de tipo como DOUBLE x NUMERIC.
 */
@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:ecocycle-flyway;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH;DB_CLOSE_DELAY=-1",
        "spring.flyway.enabled=true",
        "spring.flyway.locations=classpath:db/migration",
        "spring.jpa.hibernate.ddl-auto=validate",
        "spring.jpa.defer-datasource-initialization=false"
})
@Transactional
class FlywaySchemaValidationTest {

    @Autowired
    private Flyway flyway;

    @Test
    void entidadesDevemBaterComOSchemaDasMigrations() {
        // Chegar aqui ja significa que o ddl-auto=validate passou.
        assertThat(flyway.info().applied()).isNotEmpty();
        assertThat(flyway.info().pending()).isEmpty();
    }
}
