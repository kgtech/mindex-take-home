package com.mindex.challenge.service.impl;

import com.mindex.challenge.data.Employee;
import com.mindex.challenge.data.dto.ReportingStructure;
import com.mindex.challenge.service.EmployeeService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Optional;

import static org.junit.Assert.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class EmployeeServiceImplTest {

    private String employeeUrl;
    private String employeeIdUrl;
    private String employeeStructureUrl;
    private Employee johnLennonTestEmployee = new Employee();

    // Based on data from employee_database.json
    private static final String JOHN_LENNON_ID = "16a596ae-edd3-4847-99fe-c4518e82c86f";
    private static final String PAUL_MCCARTNEY_ID = "b7839309-3348-463b-a7e3-5de1c168beb3";
    private static final String RINGO_STARR_ID = "03aa1462-ffa9-4978-901b-7c001562cf6f";
    private static final String PETE_BEST_ID = "62c1084e-6e34-4630-93fd-9153afb65309";
    private static final String GEORGE_HARRISON_ID = "c0c2293d-16bd-4603-8e08-638a9d18b22c";


    @Autowired
    private EmployeeService employeeService;

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Before
    public void setup() {
        employeeUrl = "http://localhost:" + port + "/employee";
        employeeIdUrl = "http://localhost:" + port + "/employee/{id}";
        employeeStructureUrl = "http://localhost:" + port + "/employee/{id}/reportingStructure";

        johnLennonTestEmployee.setFirstName("John");
        johnLennonTestEmployee.setLastName("Doe");
        johnLennonTestEmployee.setDepartment("Engineering");
        johnLennonTestEmployee.setPosition("Developer");
        johnLennonTestEmployee.setEmployeeId("1");
    }

    private static void assertEmployeeEquivalence(Employee expected, Employee actual) {
        assertEquals(expected.getFirstName(), actual.getFirstName());
        assertEquals(expected.getLastName(), actual.getLastName());
        assertEquals(expected.getDepartment(), actual.getDepartment());
        assertEquals(expected.getPosition(), actual.getPosition());
    }

    @Test
    public void testCreateReadUpdate() {

        // Create checks
        Employee createdEmployee = restTemplate.postForEntity(employeeUrl, johnLennonTestEmployee, Employee.class).getBody();

        assertNotNull(createdEmployee.getEmployeeId());
        assertEmployeeEquivalence(johnLennonTestEmployee, createdEmployee);


        // Read checks
        Employee readEmployee = restTemplate.getForEntity(employeeIdUrl, Employee.class, createdEmployee.getEmployeeId()).getBody();
        assertEquals(createdEmployee.getEmployeeId(), readEmployee.getEmployeeId());
        assertEmployeeEquivalence(createdEmployee, readEmployee);


        // Update checks
        readEmployee.setPosition("Development Manager");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Employee updatedEmployee =
                restTemplate.exchange(employeeIdUrl,
                        HttpMethod.PUT,
                        new HttpEntity<Employee>(readEmployee, headers),
                        Employee.class,
                        readEmployee.getEmployeeId()).getBody();

        assertEmployeeEquivalence(readEmployee, updatedEmployee);
    }

    @Test
    public void testGetReportingStructure() {
        // Employee Structure Tests
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<ReportingStructure> reportingStructureResponseEntity = restTemplate.getForEntity(employeeStructureUrl, ReportingStructure.class, "1");
        assertEquals(HttpStatus.NOT_FOUND, reportingStructureResponseEntity.getStatusCode());
    }


    @Test
    public void testGetReportingStructure_JohnLennon_ShouldReturn4Reports() {
        // When - Service Layer Test
        Optional<ReportingStructure> serviceResult = employeeService.getReportingStructure(JOHN_LENNON_ID);

        // Then - Service Layer Assertions
        assertTrue("ReportingStructure should be present for John Lennon", serviceResult.isPresent());
        ReportingStructure reportingStructure = serviceResult.get();
        assertEquals("John Lennon should have 4 total reports", 4, reportingStructure.getNumberOfReports());
        assertEquals("Employee ID should match", JOHN_LENNON_ID, reportingStructure.getEmployee().getEmployeeId());

        // When - REST Endpoint Test
        ResponseEntity<ReportingStructure> response = restTemplate.getForEntity(
                employeeStructureUrl, ReportingStructure.class, JOHN_LENNON_ID);

        // Then - REST Endpoint Assertions
        assertEquals("Should return HTTP 200 OK", HttpStatus.OK, response.getStatusCode());
        assertNotNull("Response body should not be null", response.getBody());
        assertEquals("REST endpoint should return 4 reports", 4, response.getBody().getNumberOfReports());
        assertEquals("Employee details should be complete", "John", response.getBody().getEmployee().getFirstName());
        assertEquals("Employee details should be complete", "Lennon", response.getBody().getEmployee().getLastName());
    }

    /**
     * Test intermediate node in hierarchy: Ringo Starr has 2 direct reports
     */
    @Test
    public void testGetReportingStructure_RingoStarr_ShouldReturn2Reports() {
        // Given: Ringo Starr has 2 direct reports (Pete Best, George Harrison)

        // When - Service Layer Test
        Optional<ReportingStructure> serviceResult = employeeService.getReportingStructure(RINGO_STARR_ID);

        // Then - Service Layer Assertions
        assertTrue("ReportingStructure should be present for Ringo Starr", serviceResult.isPresent());
        assertEquals("Ringo Starr should have 2 total reports", 2, serviceResult.get().getNumberOfReports());

        // When - REST Endpoint Test
        ResponseEntity<ReportingStructure> response = restTemplate.getForEntity(
                employeeStructureUrl, ReportingStructure.class, RINGO_STARR_ID);

        // Then - REST Endpoint Assertions
        assertEquals("Should return HTTP 200 OK", HttpStatus.OK, response.getStatusCode());
        assertEquals("REST endpoint should return 2 reports", 2, response.getBody().getNumberOfReports());
        assertEquals("Employee details should be complete", "Ringo", response.getBody().getEmployee().getFirstName());
    }

    @Test
    public void testGetReportingStructure_PaulMcCartney_ShouldReturn0Reports() {
        // Given: Paul McCartney is a leaf node (no direct reports)

        // When - Service Layer Test
        Optional<ReportingStructure> serviceResult = employeeService.getReportingStructure(PAUL_MCCARTNEY_ID);

        // Then - Service Layer Assertions
        assertTrue("ReportingStructure should be present for Paul McCartney", serviceResult.isPresent());
        assertEquals("Paul McCartney should have 0 reports", 0, serviceResult.get().getNumberOfReports());

        // When - REST Endpoint Test
        ResponseEntity<ReportingStructure> response = restTemplate.getForEntity(
                employeeStructureUrl, ReportingStructure.class, PAUL_MCCARTNEY_ID);

        // Then - REST Endpoint Assertions
        assertEquals("Should return HTTP 200 OK", HttpStatus.OK, response.getStatusCode());
        assertEquals("REST endpoint should return 0 reports", 0, response.getBody().getNumberOfReports());
    }


    @Test
    public void testGetReportingStructure_testEmployeesWithoutReports_ShouldReturn0Reports() {
        // Test Pete Best
        Optional<ReportingStructure> peteBestResult = employeeService.getReportingStructure(PETE_BEST_ID);
        assertTrue("Pete Best should be found", peteBestResult.isPresent());
        assertEquals("Pete Best should have 0 reports", 0, peteBestResult.get().getNumberOfReports());

        Optional<ReportingStructure> georgeHarrisonResult = employeeService.getReportingStructure(GEORGE_HARRISON_ID);
        assertTrue("George Harrison should be found", georgeHarrisonResult.isPresent());
        assertEquals("George Harrison should have 0 reports", 0, georgeHarrisonResult.get().getNumberOfReports());
    }


    @Test
    public void testGetReportingStructure_InvalidEmployeeId_ShouldReturnNotFound() {
        // Given: Non-existent employee ID
        String invalidId = "non-existent-employee-id";

        // When - Service Layer Test
        Optional<ReportingStructure> serviceResult = employeeService.getReportingStructure(invalidId);

        // Then - Service Layer Assertions
        assertFalse("ReportingStructure should be empty for invalid employee ID", serviceResult.isPresent());

        // When - REST Endpoint Test
        ResponseEntity<ReportingStructure> response = restTemplate.getForEntity(
                employeeStructureUrl, ReportingStructure.class, invalidId);

        // Then - REST Endpoint Assertions
        assertEquals("Should return HTTP 404 Not Found", HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull("Response body should be null for 404", response.getBody());
    }


    @Test
    public void testGetReportingStructure_nullEmployeeId_ShouldReturnEmptyOptional() {
        // Test null employee ID
        Optional<ReportingStructure> nullResult = employeeService.getReportingStructure(null);
        assertFalse("Should return empty for null employee ID", nullResult.isPresent());
    }

    @Test
    public void testGetReportingStructure_emptyEmployeeId_ShouldReturnEmptyOptional() {
        Optional<ReportingStructure> emptyResult = employeeService.getReportingStructure("");
        assertFalse("Should return empty for empty employee ID", emptyResult.isPresent());
    }

    @Test
    public void testGetReportingStructure_Consistency() {
        // Given: Multiple calls to the same employee

        // When - Multiple service calls
        Optional<ReportingStructure> result1 = employeeService.getReportingStructure(JOHN_LENNON_ID);
        Optional<ReportingStructure> result2 = employeeService.getReportingStructure(JOHN_LENNON_ID);
        Optional<ReportingStructure> result3 = employeeService.getReportingStructure(JOHN_LENNON_ID);

        // Then - Results should be consistent
        assertTrue("All results should be present", result1.isPresent() && result2.isPresent() && result3.isPresent());
        assertEquals("Results should be consistent", result1.get().getNumberOfReports(), result2.get().getNumberOfReports());
        assertEquals("Results should be consistent", result2.get().getNumberOfReports(), result3.get().getNumberOfReports());

        // When - Multiple REST calls
        ResponseEntity<ReportingStructure> response1 = restTemplate.getForEntity(employeeStructureUrl, ReportingStructure.class, JOHN_LENNON_ID);
        ResponseEntity<ReportingStructure> response2 = restTemplate.getForEntity(employeeStructureUrl, ReportingStructure.class, JOHN_LENNON_ID);

        // Then - REST results should be consistent
        assertEquals("HTTP responses should be consistent", response1.getStatusCode(), response2.getStatusCode());
        assertEquals("Report counts should be consistent",
                response1.getBody().getNumberOfReports(), response2.getBody().getNumberOfReports());
    }

    @Test
    public void testGetReportingStructure_CompleteEmployeeData() {
        // When
        ResponseEntity<ReportingStructure> response = restTemplate.getForEntity(
                employeeStructureUrl, ReportingStructure.class, JOHN_LENNON_ID);

        // Then - Verify complete employee object
        assertEquals("Should return HTTP 200 OK", HttpStatus.OK, response.getStatusCode());
        ReportingStructure reportingStructure = response.getBody();
        Employee employee = reportingStructure.getEmployee();

        assertNotNull("Employee should not be null", employee);
        assertEquals("Employee ID should match", JOHN_LENNON_ID, employee.getEmployeeId());
        assertEquals("First name should be complete", "John", employee.getFirstName());
        assertEquals("Last name should be complete", "Lennon", employee.getLastName());
        assertEquals("Position should be complete", "Development Manager", employee.getPosition());
        assertEquals("Department should be complete", "Engineering", employee.getDepartment());
        assertNotNull("Direct reports should not be null", employee.getDirectReports());
        assertEquals("Should have 2 direct reports", 2, employee.getDirectReports().size());
    }


}
