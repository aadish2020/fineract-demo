/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.fineract.portfolio.loanaccount.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import org.apache.fineract.infrastructure.core.config.FineractProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Tests {@link LoanRepositoryWrapper#findActiveLoansByProductId(Long)}.
 */
@ExtendWith(MockitoExtension.class)
class LoanRepositoryWrapperTest {

    private static final Long PRODUCT_ID = 42L;

    // Statuses included in NON_CLOSED_AND_OVERPAID_LOAN_STATUSES
    private static final List<LoanStatus> EXPECTED_ALLOWED_STATUSES = Arrays.asList(
            LoanStatus.SUBMITTED_AND_PENDING_APPROVAL,
            LoanStatus.APPROVED,
            LoanStatus.ACTIVE,
            LoanStatus.TRANSFER_IN_PROGRESS,
            LoanStatus.TRANSFER_ON_HOLD,
            LoanStatus.OVERPAID);

    // Statuses that should be excluded (closed / written-off)
    private static final List<LoanStatus> EXCLUDED_STATUSES = Arrays.asList(
            LoanStatus.CLOSED_OBLIGATIONS_MET,
            LoanStatus.CLOSED_WRITTEN_OFF,
            LoanStatus.CLOSED_RESCHEDULE_OUTSTANDING_AMOUNT,
            LoanStatus.WITHDRAWN_BY_CLIENT,
            LoanStatus.REJECTED);

    @Mock
    private LoanRepository loanRepository;

    @Mock
    private FineractProperties fineractProperties;

    @InjectMocks
    private LoanRepositoryWrapper underTest;

    @BeforeEach
    void setUp() {
        // No additional setup needed; Mockito wires mocks via @InjectMocks
    }

    @Test
    void findActiveLoansByProductId_returnsLoansFromRepository() {
        Loan loan1 = mock(Loan.class);
        Loan loan2 = mock(Loan.class);
        List<Loan> expected = Arrays.asList(loan1, loan2);

        when(loanRepository.findByLoanProductAndStatusIn(eq(PRODUCT_ID), argThat(statuses ->
                statuses.containsAll(EXPECTED_ALLOWED_STATUSES)))).thenReturn(expected);

        List<Loan> result = underTest.findActiveLoansByProductId(PRODUCT_ID);

        assertThat(result).isEqualTo(expected);
        verify(loanRepository).findByLoanProductAndStatusIn(eq(PRODUCT_ID), argThat(statuses ->
                statuses.containsAll(EXPECTED_ALLOWED_STATUSES)));
    }

    @Test
    void findActiveLoansByProductId_usesOnlyAllowedStatuses() {
        when(loanRepository.findByLoanProductAndStatusIn(eq(PRODUCT_ID), argThat(statuses ->
                statuses.containsAll(EXPECTED_ALLOWED_STATUSES)))).thenReturn(List.of());

        underTest.findActiveLoansByProductId(PRODUCT_ID);

        // Verify the statuses passed to the repository do not include any closed/irrelevant statuses
        verify(loanRepository).findByLoanProductAndStatusIn(eq(PRODUCT_ID), argThat((Collection<LoanStatus> statuses) ->
                EXCLUDED_STATUSES.stream().noneMatch(statuses::contains)));
    }

    @Test
    void findActiveLoansByProductId_returnsEmptyListWhenNoLoansExist() {
        when(loanRepository.findByLoanProductAndStatusIn(eq(PRODUCT_ID), argThat(statuses ->
                statuses.containsAll(EXPECTED_ALLOWED_STATUSES)))).thenReturn(List.of());

        List<Loan> result = underTest.findActiveLoansByProductId(PRODUCT_ID);

        assertThat(result).isEmpty();
    }

    @ParameterizedTest
    @EnumSource(value = LoanStatus.class,
            names = { "APPROVED", "ACTIVE", "OVERPAID" })
    void findActiveLoansByProductId_includesEligibleStatus(LoanStatus eligibleStatus) {
        Loan eligibleLoan = mock(Loan.class);
        when(loanRepository.findByLoanProductAndStatusIn(eq(PRODUCT_ID), argThat(statuses ->
                statuses.contains(eligibleStatus)))).thenReturn(List.of(eligibleLoan));

        List<Loan> result = underTest.findActiveLoansByProductId(PRODUCT_ID);

        assertThat(result).containsExactly(eligibleLoan);
    }
}
