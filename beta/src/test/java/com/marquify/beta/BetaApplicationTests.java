package com.marquify.beta;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.flywaydb.core.Flyway;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class BetaApplicationTests {

	@Autowired
	JdbcTemplate jdbc;

	@Autowired
	Flyway flyway;

	@Test
	void contextLoads() {
		assertThat(flyway.info().current().getVersion().toString()).isEqualTo("19");
		assertThat(flyway.migrate().migrationsExecuted).isZero();
		assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM agendamentos", Integer.class)).isGreaterThanOrEqualTo(0);
	}

}
