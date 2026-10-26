package gestion_comercial;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@org.springframework.test.context.ActiveProfiles("test")
@org.springframework.test.context.ContextConfiguration(initializers = gestion_comercial.support.TestDatabaseGuard.class)
@SpringBootTest
class GestionComercialApplicationTests {

	@Test
	void contextLoads() {
	}

}
