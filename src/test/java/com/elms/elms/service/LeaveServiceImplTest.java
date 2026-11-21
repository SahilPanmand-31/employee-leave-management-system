package com.elms.elms.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.elms.elms.dto.LeaveApplyRequest;
import com.elms.elms.dto.LeaveResponse;
import com.elms.elms.entity.Employee;
import com.elms.elms.entity.LeaveBalance;
import com.elms.elms.entity.LeaveRequest;
import com.elms.elms.entity.LeaveStatus;
import com.elms.elms.exception.InvalidLeaveRequestException;
import com.elms.elms.exception.ResourceNotFoundException;
import com.elms.elms.repository.EmployeeRepository;
import com.elms.elms.repository.LeaveBalanceRepository;
import com.elms.elms.repository.LeaveRequestRepository;

@ExtendWith(MockitoExtension.class)
class LeaveServiceImplTest {

    @Mock
    private LeaveRequestRepository leaveRequestRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private LeaveBalanceRepository leaveBalanceRepository;

    @InjectMocks
    private LeaveServiceImpl leaveService;

    private Employee employee;
    private LeaveBalance balance;
    private LocalDate start;
    private LocalDate end;

    @BeforeEach
    void setUp() {
        employee = new Employee("Sahil", "sahil@example.com", "IT", "Java Developer");
        employee.setId(1L);
        balance = new LeaveBalance(20, 20, employee);

        // Always in the future, so the tests never break because of today's date
        start = LocalDate.now().plusDays(10);
        end = start.plusDays(2); // 3 days in total
    }

    // ---------- applyLeave ----------

    @Test
    void applyLeave_withValidRequest_savesPendingLeave() {
        LeaveApplyRequest request = new LeaveApplyRequest("CASUAL", start, end, "Family function");
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(leaveRequestRepository.existsOverlappingLeave(1L, start, end)).thenReturn(false);
        when(leaveBalanceRepository.findByEmployee(employee)).thenReturn(Optional.of(balance));
        when(leaveRequestRepository.save(any(LeaveRequest.class))).thenAnswer(inv -> inv.getArgument(0));

        LeaveResponse response = leaveService.applyLeave(1L, request);

        assertThat(response.status()).isEqualTo(LeaveStatus.PENDING);
        assertThat(response.numberOfDays()).isEqualTo(3);
        assertThat(response.employeeId()).isEqualTo(1L);
    }

    @Test
    void applyLeave_whenEndDateBeforeStartDate_throwsException() {
        LeaveApplyRequest request = new LeaveApplyRequest("CASUAL", end, start, "Wrong order");
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));

        assertThatThrownBy(() -> leaveService.applyLeave(1L, request))
                .isInstanceOf(InvalidLeaveRequestException.class)
                .hasMessage("End Date cannot be before Start Date");
    }

    @Test
    void applyLeave_forPastDate_throwsException() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        LeaveApplyRequest request = new LeaveApplyRequest("SICK", yesterday, LocalDate.now(), "Fever");
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));

        assertThatThrownBy(() -> leaveService.applyLeave(1L, request))
                .isInstanceOf(InvalidLeaveRequestException.class)
                .hasMessage("Cannot apply leave for past dates");
    }

    @Test
    void applyLeave_whenDatesOverlap_throwsException() {
        LeaveApplyRequest request = new LeaveApplyRequest("CASUAL", start, end, "Trip");
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(leaveRequestRepository.existsOverlappingLeave(1L, start, end)).thenReturn(true);

        assertThatThrownBy(() -> leaveService.applyLeave(1L, request))
                .isInstanceOf(InvalidLeaveRequestException.class)
                .hasMessage("Leave already exists for selected dates");

        verify(leaveRequestRepository, never()).save(any());
    }

    @Test
    void applyLeave_whenNotEnoughBalance_throwsException() {
        LeaveApplyRequest request = new LeaveApplyRequest("CASUAL", start, end, "Trip");
        balance.setRemainingLeaves(2); // asks for 3 days, has 2
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(leaveRequestRepository.existsOverlappingLeave(1L, start, end)).thenReturn(false);
        when(leaveBalanceRepository.findByEmployee(employee)).thenReturn(Optional.of(balance));

        assertThatThrownBy(() -> leaveService.applyLeave(1L, request))
                .isInstanceOf(InvalidLeaveRequestException.class)
                .hasMessage("Insufficient Leave Balance");
    }

    @Test
    void applyLeave_forUnknownEmployee_throwsResourceNotFoundException() {
        LeaveApplyRequest request = new LeaveApplyRequest("CASUAL", start, end, "Trip");
        when(employeeRepository.findById(42L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> leaveService.applyLeave(42L, request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ---------- approve / reject / cancel ----------

    @Test
    void approveLeave_deductsDaysFromBalance() {
        LeaveRequest leave = pendingLeave();
        when(leaveRequestRepository.findById(5L)).thenReturn(Optional.of(leave));
        when(leaveBalanceRepository.findByEmployee(employee)).thenReturn(Optional.of(balance));
        when(leaveRequestRepository.save(leave)).thenReturn(leave);

        LeaveResponse response = leaveService.approveLeave(5L);

        assertThat(response.status()).isEqualTo(LeaveStatus.APPROVED);
        assertThat(balance.getRemainingLeaves()).isEqualTo(17); // 20 - 3
        verify(leaveBalanceRepository).save(balance);
    }

    @Test
    void approveLeave_whenAlreadyApproved_throwsException() {
        LeaveRequest leave = pendingLeave();
        leave.setStatus(LeaveStatus.APPROVED);
        when(leaveRequestRepository.findById(5L)).thenReturn(Optional.of(leave));

        assertThatThrownBy(() -> leaveService.approveLeave(5L))
                .isInstanceOf(InvalidLeaveRequestException.class)
                .hasMessageContaining("already APPROVED");

        // Balance must not be deducted twice
        verify(leaveBalanceRepository, never()).save(any());
    }

    @Test
    void approveLeave_whenNotEnoughBalance_throwsException() {
        LeaveRequest leave = pendingLeave();
        balance.setRemainingLeaves(1);
        when(leaveRequestRepository.findById(5L)).thenReturn(Optional.of(leave));
        when(leaveBalanceRepository.findByEmployee(employee)).thenReturn(Optional.of(balance));

        assertThatThrownBy(() -> leaveService.approveLeave(5L))
                .isInstanceOf(InvalidLeaveRequestException.class)
                .hasMessage("Insufficient Leave Balance");
    }

    @Test
    void rejectLeave_setsStatusRejected_andKeepsBalance() {
        LeaveRequest leave = pendingLeave();
        when(leaveRequestRepository.findById(5L)).thenReturn(Optional.of(leave));
        when(leaveRequestRepository.save(leave)).thenReturn(leave);

        LeaveResponse response = leaveService.rejectLeave(5L);

        assertThat(response.status()).isEqualTo(LeaveStatus.REJECTED);
        verify(leaveBalanceRepository, never()).save(any());
    }

    @Test
    void cancelLeave_whenNotPending_throwsException() {
        LeaveRequest leave = pendingLeave();
        leave.setStatus(LeaveStatus.REJECTED);
        when(leaveRequestRepository.findById(5L)).thenReturn(Optional.of(leave));

        assertThatThrownBy(() -> leaveService.cancelLeave(5L))
                .isInstanceOf(InvalidLeaveRequestException.class)
                .hasMessage("Only Pending Leave can be cancelled");
    }

    private LeaveRequest pendingLeave() {
        LeaveRequest leave = new LeaveRequest("CASUAL", start, end, "Trip", LeaveStatus.PENDING, employee);
        leave.setId(5L);
        return leave;
    }
}
