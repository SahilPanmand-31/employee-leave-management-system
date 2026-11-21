package com.elms.elms;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

// Starts the whole application against the in-memory H2 database (test profile).
// Fails if any bean, entity mapping or JPQL query is broken.
@SpringBootTest
@ActiveProfiles("test")
class EmployeeLeaveManagementSystemApplicationTests {

	@Test
	void contextLoads() {
	}

}
