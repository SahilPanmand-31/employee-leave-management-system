package com.elms.elms.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.elms.elms.dto.EmployeeRequest;
import com.elms.elms.dto.EmployeeResponse;
import com.elms.elms.dto.LeaveBalanceResponse;
import com.elms.elms.entity.Employee;
import com.elms.elms.entity.LeaveBalance;
import com.elms.elms.exception.DuplicateResourceException;
import com.elms.elms.exception.ResourceNotFoundException;
import com.elms.elms.repository.EmployeeRepository;
import com.elms.elms.repository.LeaveBalanceRepository;

// Pure unit test: repositories are mocked with Mockito, no Spring context, no database.
@ExtendWith(MockitoExtension.class)
class EmployeeServiceImplTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private LeaveBalanceRepository leaveBalanceRepository;

    @InjectMocks
    private EmployeeServiceImpl employeeService;

    private Employee employee;
    private EmployeeRequest request;

    @BeforeEach
    void setUp() {
        employee = new Employee("Sahil", "sahil@example.com", "IT", "Java Developer");
        employee.setId(1L);
        request = new EmployeeRequest("Sahil", "sahil@example.com", "IT", "Java Developer");
    }

    @Test
    void createEmployee_savesEmployeeAndCreatesDefaultLeaveBalance() {
        when(employeeRepository.existsByEmail("sahil@example.com")).thenReturn(false);
        when(employeeRepository.save(any(Employee.class))).thenReturn(employee);

        EmployeeResponse response = employeeService.createEmployee(request);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.email()).isEqualTo("sahil@example.com");

        // The leave balance must be created with 20 days
        ArgumentCaptor<LeaveBalance> balanceCaptor = ArgumentCaptor.forClass(LeaveBalance.class);
        verify(leaveBalanceRepository).save(balanceCaptor.capture());
        assertThat(balanceCaptor.getValue().getTotalLeaves()).isEqualTo(20);
        assertThat(balanceCaptor.getValue().getRemainingLeaves()).isEqualTo(20);
    }

    @Test
    void createEmployee_withDuplicateEmail_throwsDuplicateResourceException() {
        when(employeeRepository.existsByEmail("sahil@example.com")).thenReturn(true);

        assertThatThrownBy(() -> employeeService.createEmployee(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("Email already exists");

        verify(employeeRepository, never()).save(any());
    }

    @Test
    void getEmployeeById_whenMissing_throwsResourceNotFoundException() {
        when(employeeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> employeeService.getEmployeeById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Employee not found with id: 99");
    }

    @Test
    void updateEmployee_updatesAllFields() {
        EmployeeRequest update = new EmployeeRequest("Sahil P", "new@example.com", "HR", "Lead");
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(employeeRepository.existsByEmailAndIdNot("new@example.com", 1L)).thenReturn(false);
        when(employeeRepository.save(employee)).thenReturn(employee);

        EmployeeResponse response = employeeService.updateEmployee(1L, update);

        assertThat(response.name()).isEqualTo("Sahil P");
        assertThat(response.email()).isEqualTo("new@example.com");
        assertThat(response.department()).isEqualTo("HR");
    }

    @Test
    void updateEmployee_withEmailOfAnotherEmployee_throwsDuplicateResourceException() {
        EmployeeRequest update = new EmployeeRequest("Sahil", "taken@example.com", "IT", "Dev");
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(employeeRepository.existsByEmailAndIdNot("taken@example.com", 1L)).thenReturn(true);

        assertThatThrownBy(() -> employeeService.updateEmployee(1L, update))
                .isInstanceOf(DuplicateResourceException.class);
    }

    @Test
    void deleteEmployee_deletesExistingEmployee() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));

        employeeService.deleteEmployee(1L);

        verify(employeeRepository).delete(employee);
    }

    @Test
    void searchEmployees_withNullFilters_searchesWithEmptyStrings() {
        when(employeeRepository.findByNameContainingIgnoreCaseAndDepartmentContainingIgnoreCase("", ""))
                .thenReturn(List.of(employee));

        List<EmployeeResponse> result = employeeService.searchEmployees(null, null);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).name()).isEqualTo("Sahil");
    }

    @Test
    void getLeaveBalance_returnsUsedAndRemainingDays() {
        LeaveBalance balance = new LeaveBalance(20, 15, employee);
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(leaveBalanceRepository.findByEmployee(employee)).thenReturn(Optional.of(balance));

        LeaveBalanceResponse response = employeeService.getLeaveBalance(1L);

        assertThat(response.totalLeaves()).isEqualTo(20);
        assertThat(response.usedLeaves()).isEqualTo(5);
        assertThat(response.remainingLeaves()).isEqualTo(15);
    }
}
