package com.mindex.challenge.service.impl;

import com.mindex.challenge.dao.CompensationRepository;
import com.mindex.challenge.dao.EmployeeRepository;
import com.mindex.challenge.data.Compensation;
import com.mindex.challenge.data.Employee;
import com.mindex.challenge.exception.CompensationValidationException;
import com.mindex.challenge.exception.EmployeeNotFoundException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CompensationServiceImplTest {

    @Autowired
    private CompensationServiceImpl compensationService;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private CompensationRepository compensationRepository;

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    private String compensationUrl;
    private String compensationIdUrl;

    @Before
    public void setup() {
        compensationUrl = "http://localhost:" + port + "/compensation";
        compensationIdUrl = "http://localhost:" + port + "/compensation/{id}";
    }

    private CompensationBuilder compensationBuilder() {
        return new CompensationBuilder();
    }

    private class CompensationBuilder {
        private final Compensation compensation = new Compensation();

        public CompensationBuilder forEmployee(String employeeId) {
            compensation.setEmployeeId(employeeId);
            return this;
        }

        public CompensationBuilder withSalary(String salary) {
            compensation.setSalary(new BigDecimal(salary));
            return this;
        }

        public CompensationBuilder withSalary(BigDecimal salary) {
            compensation.setSalary(salary);
            return this;
        }

        public CompensationBuilder effectiveFrom(LocalDate date) {
            compensation.setEffectiveDate(date);
            return this;
        }

        public CompensationBuilder inCurrency(String currency) {
            compensation.setCurrency(currency);
            return this;
        }

        public CompensationBuilder withDefaults() {
            return this
                    .withSalary("75000.00")
                    .effectiveFrom(LocalDate.of(2024, 1, 1))
                    .inCurrency("USD");
        }

        public Compensation build() {
            return compensation;
        }
    }

    private Employee createTestEmployee(String employeeId, String firstName, String lastName) {
        Employee employee = new Employee();
        employee.setEmployeeId(employeeId);
        employee.setFirstName(firstName);
        employee.setLastName(lastName);
        employee.setPosition("Software Engineer");
        employee.setDepartment("Engineering");
        return employeeRepository.save(employee);
    }

    private void assertCompensationCreatedProperly(Compensation compensation) {
        assertNotNull(compensation);
        assertNotNull(compensation.getCompensationId());
        assertTrue(compensation.isActive());
        assertNull(compensation.getEndDate());
    }

    private void assertCompensationMatches(Compensation expected, Compensation actual) {
        assertEquals(expected.getEmployeeId(), actual.getEmployeeId());
        assertEquals(expected.getSalary(), actual.getSalary());
        assertEquals(expected.getEffectiveDate(), actual.getEffectiveDate());
    }

    private void assertCompensationIsDeactivated(String compensationId) {
        Compensation deactivated = compensationRepository.findById(compensationId).orElse(null);
        assertNotNull(deactivated);
        assertFalse(deactivated.isActive());
        assertNotNull(deactivated.getEndDate());
    }

    // === Tests ===

    @Test
    public void testCreateRead() {
        createTestEmployee("test-emp-123", "John", "Doe");
        Compensation testCompensation = compensationBuilder()
                .forEmployee("test-emp-123")
                .withSalary("75000.00")
                .effectiveFrom(LocalDate.of(2024, 1, 1))
                .build();

        Compensation created = compensationService.create(testCompensation);
        Optional<Compensation> read = compensationService.read("test-emp-123");

        assertCompensationCreatedProperly(created);
        assertCompensationMatches(testCompensation, created);
        assertTrue(read.isPresent());
        assertEquals(created.getCompensationId(), read.get().getCompensationId());
    }

    @Test
    public void testCreateWithDefaultValues() {
        createTestEmployee("test-emp-456", "Jane", "Smith");
        Compensation testCompensation = compensationBuilder()
                .forEmployee("test-emp-456")
                .withSalary("85000.00")
                .build(); // No effectiveDate or currency

        Compensation created = compensationService.create(testCompensation);

        assertCompensationCreatedProperly(created);
        assertEquals(LocalDate.now(), created.getEffectiveDate());
        assertEquals("USD", created.getCurrency());
    }

    @Test
    public void testCreateCompensationDeactivatesPrevious() {
        createTestEmployee("test-emp-789", "Bob", "Johnson");

        Compensation firstCompensation = compensationBuilder()
                .forEmployee("test-emp-789")
                .withSalary("70000.00")
                .effectiveFrom(LocalDate.of(2023, 1, 1))
                .build();

        Compensation secondCompensation = compensationBuilder()
                .forEmployee("test-emp-789")
                .withSalary("80000.00")
                .effectiveFrom(LocalDate.of(2024, 1, 1))
                .build();

        Compensation savedFirst = compensationService.create(firstCompensation);
        Compensation savedSecond = compensationService.create(secondCompensation);

        assertTrue(savedSecond.isActive());
        assertCompensationIsDeactivated(savedFirst.getCompensationId());

        Optional<Compensation> activeCompensation = compensationService.read("test-emp-789");
        assertTrue(activeCompensation.isPresent());
        assertEquals(savedSecond.getCompensationId(), activeCompensation.get().getCompensationId());
    }

    // === Exception Tests with assertThrows ===

    @Test
    public void testCreateCompensationInvalidEmployee() {
        Compensation testCompensation = compensationBuilder()
                .forEmployee("non-existent-employee")
                .withDefaults()
                .build();

        EmployeeNotFoundException exception = assertThrows(
                EmployeeNotFoundException.class,
                () -> compensationService.create(testCompensation)
        );

        assertTrue(exception.getMessage().contains("non-existent-employee"));
    }

    @Test
    public void testCreateCompensationNullData() {
        CompensationValidationException exception = assertThrows(
                CompensationValidationException.class,
                () -> compensationService.create(null)
        );

        assertTrue(exception.getMessage().contains("Compensation cannot be null"));
    }

    @Test
    public void testCreateCompensationInvalidEmployeeIds() {
        // Given
        String[] invalidIds = {"", "   "};

        for (String invalidId : invalidIds) {
            Compensation compensation = compensationBuilder()
                    .forEmployee(invalidId)
                    .withDefaults()
                    .build();

            assertThrows(CompensationValidationException.class, () -> compensationService.create(compensation));
        }
    }

    @Test
    public void testCreateCompensationNullEmployeeId() {
        Compensation compensation = compensationBuilder()
                .forEmployee(null)
                .withDefaults()
                .build();

        CompensationValidationException exception = assertThrows(
                CompensationValidationException.class,
                () -> compensationService.create(compensation)
        );

        assertTrue(exception.getMessage().contains("Employee ID"));
    }

    @Test
    public void testCreateCompensationInvalidSalaries() {
        createTestEmployee("test-emp-salary", "Test", "User");
        BigDecimal[] invalidSalaries = {null, new BigDecimal("-1000.00")};

        for (BigDecimal invalidSalary : invalidSalaries) {
            Compensation compensation = compensationBuilder()
                    .forEmployee("test-emp-salary")
                    .withSalary(invalidSalary)
                    .effectiveFrom(LocalDate.now())
                    .build();

            assertThrows(CompensationValidationException.class, () -> compensationService.create(compensation));
        }
    }


    @Test
    public void testReadNonExistentEmployee() {
        Optional<Compensation> result = compensationService.read("non-existent-employee");
        assertFalse(result.isPresent());
    }

    @Test
    public void testReadEmployeeWithoutCompensation() {
        createTestEmployee("test-emp-no-comp", "Alice", "Wonder");

        Optional<Compensation> result = compensationService.read("test-emp-no-comp");
        assertFalse(result.isPresent());
    }

    @Test
    public void testReadInvalidEmployeeIdFormats() {
        String[] invalidIds = {null, "", "   "};

        for (String invalidId : invalidIds) {
            Optional<Compensation> result = compensationService.read(invalidId);
            assertFalse("Should return empty for invalid ID: '" + invalidId + "'", result.isPresent());
        }
    }
}