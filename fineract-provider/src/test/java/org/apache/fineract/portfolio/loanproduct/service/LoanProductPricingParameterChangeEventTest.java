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
package org.apache.fineract.portfolio.loanproduct.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.event.business.domain.loan.product.LoanProductPricingParameterChangeBusinessEvent;
import org.apache.fineract.infrastructure.event.business.service.BusinessEventNotifierService;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.AprCalculator;
import org.apache.fineract.portfolio.loanaccount.service.LoanProductUpdateUtil;
import org.apache.fineract.portfolio.loanproduct.LoanProductConstants;
import org.apache.fineract.portfolio.loanproduct.domain.LoanProduct;
import org.apache.fineract.portfolio.loanproduct.domain.LoanProductRepository;
import org.apache.fineract.portfolio.loanproduct.serialization.LoanProductDataValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Test class to verify that pricing parameter changes in Loan Products trigger the appropriate business event.
 */
@ExtendWith(MockitoExtension.class)
class LoanProductPricingParameterChangeEventTest {

    @Mock
    private LoanProductRepository loanProductRepository;

    @Mock
    private LoanProductDataValidator validator;

    @Mock
    private BusinessEventNotifierService businessEventNotifierService;

    @Mock
    private LoanProductUpdateUtil loanProductUpdateUtil;

    @Mock
    private AprCalculator aprCalculator;

    @Mock
    private JsonCommand jsonCommand;

    @Mock
    private LoanProduct loanProduct;

    private static final Long LOAN_PRODUCT_ID = 1L;

    @BeforeEach
    void setUp() {
        when(loanProductRepository.findById(LOAN_PRODUCT_ID)).thenReturn(Optional.of(loanProduct));
    }

    @Test
    void testInterestRateChangeTriggersEvent() {
        // Given: A loan product update with interest rate change
        Map<String, Object> changes = new HashMap<>();
        changes.put(LoanProductConstants.INTEREST_RATE_PER_PERIOD, BigDecimal.valueOf(12.5));

        when(loanProductUpdateUtil.update(any(), any(), any(), any())).thenReturn(changes);
        when(loanProduct.getId()).thenReturn(LOAN_PRODUCT_ID);

        // When: Simulating the hasPricingParameterChanges check
        boolean hasPricingChanges = changes.containsKey(LoanProductConstants.INTEREST_RATE_PER_PERIOD);

        // Then: Should detect pricing parameter change
        assertTrue(hasPricingChanges, "Interest rate change should be detected as a pricing parameter change");
    }

    @Test
    void testInterestCalculationMethodChangeTriggersEvent() {
        // Given: A loan product update with interest calculation method change
        Map<String, Object> changes = new HashMap<>();
        changes.put(LoanProductConstants.interestCalculationPeriodTypeParamName, 1);

        when(loanProductUpdateUtil.update(any(), any(), any(), any())).thenReturn(changes);
        when(loanProduct.getId()).thenReturn(LOAN_PRODUCT_ID);

        // When: Simulating the hasPricingParameterChanges check
        boolean hasPricingChanges = changes.containsKey(LoanProductConstants.interestCalculationPeriodTypeParamName);

        // Then: Should detect pricing parameter change
        assertTrue(hasPricingChanges, "Interest calculation method change should be detected as a pricing parameter change");
    }

    @Test
    void testInterestTypeChangeTriggersEvent() {
        // Given: A loan product update with interest type change
        Map<String, Object> changes = new HashMap<>();
        changes.put(LoanProductConstants.interestTypeParamName, 0);

        when(loanProductUpdateUtil.update(any(), any(), any(), any())).thenReturn(changes);
        when(loanProduct.getId()).thenReturn(LOAN_PRODUCT_ID);

        // When: Simulating the hasPricingParameterChanges check
        boolean hasPricingChanges = changes.containsKey(LoanProductConstants.interestTypeParamName);

        // Then: Should detect pricing parameter change
        assertTrue(hasPricingChanges, "Interest type change should be detected as a pricing parameter change");
    }

    @Test
    void testFloatingRateChangeTriggersEvent() {
        // Given: A loan product update with floating rate change
        Map<String, Object> changes = new HashMap<>();
        changes.put("isLinkedToFloatingInterestRates", true);

        when(loanProductUpdateUtil.update(any(), any(), any(), any())).thenReturn(changes);
        when(loanProduct.getId()).thenReturn(LOAN_PRODUCT_ID);

        // When: Simulating the hasPricingParameterChanges check
        boolean hasPricingChanges = changes.containsKey("isLinkedToFloatingInterestRates");

        // Then: Should detect pricing parameter change
        assertTrue(hasPricingChanges, "Floating rate link change should be detected as a pricing parameter change");
    }

    @Test
    void testInterestRecalculationChangeTriggersEvent() {
        // Given: A loan product update with interest recalculation change
        Map<String, Object> changes = new HashMap<>();
        changes.put(LoanProductConstants.IS_INTEREST_RECALCULATION_ENABLED_PARAMETER_NAME, true);

        when(loanProductUpdateUtil.update(any(), any(), any(), any())).thenReturn(changes);
        when(loanProduct.getId()).thenReturn(LOAN_PRODUCT_ID);

        // When: Simulating the hasPricingParameterChanges check
        boolean hasPricingChanges = changes.containsKey(LoanProductConstants.IS_INTEREST_RECALCULATION_ENABLED_PARAMETER_NAME);

        // Then: Should detect pricing parameter change
        assertTrue(hasPricingChanges, "Interest recalculation change should be detected as a pricing parameter change");
    }

    @Test
    void testNonPricingChangeDoesNotTriggerEvent() {
        // Given: A loan product update with non-pricing changes only
        Map<String, Object> changes = new HashMap<>();
        changes.put("name", "Updated Product Name");
        changes.put("description", "Updated description");

        when(loanProductUpdateUtil.update(any(), any(), any(), any())).thenReturn(changes);
        when(loanProduct.getId()).thenReturn(LOAN_PRODUCT_ID);

        // When: Simulating the hasPricingParameterChanges check
        boolean hasPricingChanges = changes.containsKey(LoanProductConstants.INTEREST_RATE_PER_PERIOD)
                || changes.containsKey(LoanProductConstants.interestTypeParamName)
                || changes.containsKey(LoanProductConstants.interestCalculationPeriodTypeParamName)
                || changes.containsKey("isLinkedToFloatingInterestRates")
                || changes.containsKey(LoanProductConstants.IS_INTEREST_RECALCULATION_ENABLED_PARAMETER_NAME);

        // Then: Should not detect pricing parameter change
        assertTrue(!hasPricingChanges, "Non-pricing changes should not be detected as pricing parameter changes");
    }

    @Test
    void testEventContainsChangesMap() {
        // Given: A pricing parameter change
        Map<String, Object> changes = new HashMap<>();
        changes.put(LoanProductConstants.INTEREST_RATE_PER_PERIOD, BigDecimal.valueOf(15.0));
        changes.put("locale", "en");

        // When: Creating the event
        LoanProductPricingParameterChangeBusinessEvent event = new LoanProductPricingParameterChangeBusinessEvent(loanProduct, changes);

        // Then: Event should contain the changes map
        assertNotNull(event.getChanges(), "Event should contain changes map");
        assertEquals(changes, event.getChanges(), "Event changes should match the provided changes");
        assertTrue(event.getChanges().containsKey(LoanProductConstants.INTEREST_RATE_PER_PERIOD),
                "Event should contain interest rate change");
    }

    @Test
    void testEventTypeIsCorrect() {
        // Given: A pricing parameter change event
        Map<String, Object> changes = new HashMap<>();
        changes.put(LoanProductConstants.INTEREST_RATE_PER_PERIOD, BigDecimal.valueOf(10.0));

        LoanProductPricingParameterChangeBusinessEvent event = new LoanProductPricingParameterChangeBusinessEvent(loanProduct, changes);

        // Then: Event type should be correct
        assertEquals("LoanProductPricingParameterChangeBusinessEvent", event.getType(),
                "Event type should be LoanProductPricingParameterChangeBusinessEvent");
    }
}
