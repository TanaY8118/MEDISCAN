package com.mediscan;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mediscan.support.AuthTestHelper;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Root base class for all integration tests.
 *
 * <p>
 * Loads the complete Spring application context using the "test" profile,
 * which substitutes H2 (MySQL-mode) for MySQL and Flapdoodle embedded MongoDB
 * for the real MongoDB instance. No external infrastructure is required.
 *
 * <p>
 * Every test method that interacts with the JPA/MySQL layer is wrapped in a
 * transaction that rolls back automatically, keeping tests independent.
 * MongoDB state is cleared via {@link AuthTestHelper#reset()} before each test.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public abstract class BaseIntegrationTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected AuthTestHelper authHelper;

    /**
     * Reset per-test state. Subclasses may override and call {@code super.setUp()}.
     */
    @BeforeEach
    public void setUp() {
        authHelper.reset();
    }
}
