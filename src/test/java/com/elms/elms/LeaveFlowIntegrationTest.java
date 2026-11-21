package com.elms.elms;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.jayway.jsonpath.JsonPath;

// End-to-end flow on H2 with real JWT tokens:
// register -> login -> create employee -> apply leave -> approve -> check balance.
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class LeaveFlowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void fullLeaveFlow_withJwtRoles() throws Exception {
        String managerToken = registerAndLogin("manager1", "MANAGER");
        String employeeToken = registerAndLogin("employee1", "EMPLOYEE");

        // Manager creates an employee -> leave balance of 20 is created automatically
        String employeeJson = mockMvc.perform(post("/employees")
                        .header("Authorization", "Bearer " + managerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Sahil\",\"email\":\"sahil@example.com\",\"department\":\"IT\",\"role\":\"Java Developer\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        Integer employeeId = JsonPath.read(employeeJson, "$.id");

        // Employee cannot create employees
        mockMvc.perform(post("/employees")
                        .header("Authorization", "Bearer " + employeeToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"X\",\"email\":\"x@example.com\",\"department\":\"IT\",\"role\":\"Dev\"}"))
                .andExpect(status().isForbidden());

        // Employee applies for 3 days of leave
        LocalDate start = LocalDate.now().plusDays(7);
        String leaveBody = String.format(
                "{\"leaveType\":\"CASUAL\",\"startDate\":\"%s\",\"endDate\":\"%s\",\"reason\":\"Family function\"}",
                start, start.plusDays(2));

        String leaveJson = mockMvc.perform(post("/leaves/apply/" + employeeId)
                        .header("Authorization", "Bearer " + employeeToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(leaveBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andReturn().getResponse().getContentAsString();
        Integer leaveId = JsonPath.read(leaveJson, "$.id");

        // Overlapping leave is rejected
        mockMvc.perform(post("/leaves/apply/" + employeeId)
                        .header("Authorization", "Bearer " + employeeToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(leaveBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Leave already exists for selected dates"));

        // Manager approves
        mockMvc.perform(put("/leaves/approve/" + leaveId)
                        .header("Authorization", "Bearer " + managerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));

        // Balance went from 20 to 17
        mockMvc.perform(get("/employees/" + employeeId + "/leave-balance")
                        .header("Authorization", "Bearer " + employeeToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.remainingLeaves").value(17))
                .andExpect(jsonPath("$.usedLeaves").value(3));

        // History and search return plain lists
        mockMvc.perform(get("/leaves/history/" + employeeId)
                        .header("Authorization", "Bearer " + employeeToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        mockMvc.perform(get("/employees/search?name=sah&department=it")
                        .header("Authorization", "Bearer " + employeeToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Sahil"));

        // Wrong password -> 401
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"manager1\",\"password\":\"wrong-password\"}"))
                .andExpect(status().isUnauthorized());

        // Swagger docs are public
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk());
    }

    private String registerAndLogin(String username, String role) throws Exception {
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format(
                                "{\"username\":\"%s\",\"password\":\"secret123\",\"role\":\"%s\"}", username, role)))
                .andExpect(status().isCreated());

        String loginJson = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format("{\"username\":\"%s\",\"password\":\"secret123\"}", username)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        return JsonPath.read(loginJson, "$.token");
    }
}
