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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import org.apache.fineract.accounting.producttoaccountmapping.service.ProductToGLAccountMappingWritePlatformService;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.event.business.domain.loan.product.LoanProductPricingChangeBusinessEvent;
import org.apache.fineract.infrastructure.event.business.service.BusinessEventNotifierService;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.charge.domain.ChargeRepositoryWrapper;
import org.apache.fineract.portfolio.delinquency.domain.DelinquencyBucketRepository;
import org.apache.fineract.portfolio.floatingrates.domain.FloatingRateRepositoryWrapper;
import org.apache.fineract.portfolio.fund.domain.FundRepository;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepaymentScheduleTransactionProcessorFactory;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepositoryWrapper;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.AprCalculator;
import org.apache.fineract.portfolio.loanaccount.service.LoanProductAssembler;
import org.apache.fineract.portfolio.loanaccount.service.LoanProductUpdateUtil;
import org.apache.fineract.portfolio.loanproduct.LoanProductConstants;
import org.apache.fineract.portfolio.loanproduct.domain.AdvancedPaymentAllocationsJsonParser;
import org.apache.fineract.portfolio.loanproduct.domain.CreditAllocationsJsonParser;
import org.apache.fineract.portfolio.loanproduct.domain.LoanProduct;
import org.apache.fineract.portfolio.loanproduct.domain.LoanProductRepository;
import org.apache.fineract.portfolio.loanproduct.serialization.LoanProductDataValidator;
import org.apache.fineract.portfolio.rate.domain.RateRepositoryWrapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

/**
 * Unit tests for the pricing change detection logic in {@link LoanProductWritePlatformServiceJpaRepositoryImpl}.
 *
 * <p>
 * A "pricing change" is defined as any modification to: {@code interestRatePerPeriod}, {@code penaltyRate}, or
 * {@code interestCalculationPeriodType}. When any of these fields changes, a
 * {@link LoanProductPricingChangeBusinessEvent} must be emitted so that downstream logic can update affected loans.
 */
@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
class LoanProductWritePlatformServicePricingChangeDetectionTest {

    @Mock
    private PlatformSecurityContext context;
    @Mock
    private LoanProductDataValidator fromApiJsonDeserializer;
    @Mock
    private LoanProductRepository loanProductRepository;
    @Mock
    private AprCalculator aprCalculator;
    @Mock
    private FundRepository fundRepository;
    @Mock
    private ChargeRepositoryWrapper chargeRepository;
    @Mock
    private RateRepositoryWrapper rateRepository;
    @Mock
    private ProductToGLAccountMappingWritePlatformService accountMappingWritePlatformService;
    @Mock
    private org.apache.fineract.infrastructure.entityaccess.service.FineractEntityAccessUtil fineractEntityAccessUtil;
    @Mock
    private FloatingRateRepositoryWrapper floatingRateRepository;
    @Mock
    private LoanRepositoryWrapper loanRepositoryWrapper;
    @Mock
    private BusinessEventNotifierService businessEventNotifierService;
    @Mock
    private DelinquencyBucketRepository delinquencyBucketRepository;
    @Mock
    private LoanRepaymentScheduleTransactionProcessorFactory loanRepaymentScheduleTransactionProcessorFactory;
    @Mock
    private AdvancedPaymentAllocationsJsonParser advancedPaymentJsonParser;
    @Mock
    private CreditAllocationsJsonParser creditAllocationsJsonParser;
    @Mock
    private LoanProductAssembler loanProductAssembler;
    @Mock
    private LoanProductUpdateUtil loanProductUpdateUtil;

    private LoanProductWritePlatformServiceJpaRepositoryImpl service;

    @BeforeEach
    void setUp() {
        service = new LoanProductWritePlatformServiceJpaRepositoryImpl(context, fromApiJsonDeserializer, loanProductRepository,
                aprCalculator, fundRepository, chargeRepository, rateRepository, accountMappingWritePlatformService,
                fineractEntityAccessUtil, floatingRateRepository, loanRepositoryWrapper, businessEventNotifierService,
                delinquencyBucketRepository, loanRepaymentScheduleTransactionProcessorFactory, advancedPaymentJsonParser,
                creditAllocationsJsonParser, loanProductAssembler, loanProductUpdateUtil);

        // Common stubs
        when(accountMappingWritePlatformService.updateLoanProductToGLAccountMapping(any(), any(), any(Boolean.class),
                any(), any(Boolean.class), any(Boolean.class), any(Boolean.class))).thenReturn(new HashMap<>());
        when(loanRepositoryWrapper.doNonClosedLoanAccountsExistForProduct(any())).thenReturn(false);
    }

    private LoanProduct buildMockProduct() {
        LoanProduct product = mock(LoanProduct.class);
        when(product.getId()).thenReturn(1L);
        when(product.isLinkedToFloatingInterestRate()).thenReturn(false);
        when(product.getFloatingRates()).thenReturn(null);

        org.apache.fineract.portfolio.loanproduct.domain.LoanProductRelatedDetail detail = mock(
                org.apache.fineract.portfolio.loanproduct.domain.LoanProductRelatedDetail.class);
        when(detail.isEnableIncomeCapitalization()).thenReturn(false);
        when(detail.isEnableBuyDownFee()).thenReturn(false);
        when(detail.isMerchantBuyDownFee()).thenReturn(false);
        when(product.getLoanProductRelatedDetail()).thenReturn(detail);
        when(product.getAccountingRule()).thenReturn(null);
        return product;
    }

    private JsonCommand buildCommand() {
        JsonCommand command = mock(JsonCommand.class);
        when(command.commandId()).thenReturn(null);
        when(command.parameterExists("floatingRatesId")).thenReturn(false);
        when(command.parameterExists(LoanProductConstants.SUPPORTED_INTEREST_REFUND_TYPES)).thenReturn(false);
        when(command.parameterExists(LoanProductConstants.CHARGE_OFF_BEHAVIOUR)).thenReturn(false);
        when(command.parameterExists(LoanProductConstants.RATES_PARAM_NAME)).thenReturn(false);
        when(command.hasParameter("paymentAllocation")).thenReturn(false);
        when(command.hasParameter("creditAllocation")).thenReturn(false);
        when(command.hasParameter("charges")).thenReturn(false);
        return command;
    }

    @Test
    void shouldEmitPricingChangeEvent_whenInterestRatePerPeriodChanges() {
        // given
        LoanProduct product = buildMockProduct();
        JsonCommand command = buildCommand();
        when(loanProductRepository.findById(1L)).thenReturn(java.util.Optional.of(product));

        Map<String, Object> changes = new HashMap<>();
        // A change in interestRatePerPeriod constitutes a pricing change
        changes.put(LoanProductConstants.INTEREST_RATE_PER_PERIOD, new BigDecimal("12.00"));
        when(loanProductUpdateUtil.update(product, command, aprCalculator, null)).thenReturn(changes);

        // when
        CommandProcessingResult result = service.updateLoanProduct(1L, command);

        // then
        verify(businessEventNotifierService).notifyPostBusinessEvent(any(LoanProductPricingChangeBusinessEvent.class));
    }

    @Test
    void shouldEmitPricingChangeEvent_whenPenaltyRateChanges() {
        // given
        LoanProduct product = buildMockProduct();
        JsonCommand command = buildCommand();
        when(loanProductRepository.findById(1L)).thenReturn(java.util.Optional.of(product));

        Map<String, Object> changes = new HashMap<>();
        // A change in penaltyRate constitutes a pricing change
        changes.put(LoanProductConstants.PENALTY_RATE, new BigDecimal("5.00"));
        when(loanProductUpdateUtil.update(product, command, aprCalculator, null)).thenReturn(changes);

        // when
        service.updateLoanProduct(1L, command);

        // then
        verify(businessEventNotifierService).notifyPostBusinessEvent(any(LoanProductPricingChangeBusinessEvent.class));
    }

    @Test
    void shouldEmitPricingChangeEvent_whenInterestCalculationPeriodTypeChanges() {
        // given
        LoanProduct product = buildMockProduct();
        JsonCommand command = buildCommand();
        when(loanProductRepository.findById(1L)).thenReturn(java.util.Optional.of(product));

        Map<String, Object> changes = new HashMap<>();
        // A change in interestCalculationPeriodType constitutes a pricing change
        changes.put("interestCalculationPeriodType", 1);
        when(loanProductUpdateUtil.update(product, command, aprCalculator, null)).thenReturn(changes);

        // when
        service.updateLoanProduct(1L, command);

        // then
        verify(businessEventNotifierService).notifyPostBusinessEvent(any(LoanProductPricingChangeBusinessEvent.class));
    }

    @Test
    void shouldNotEmitPricingChangeEvent_whenNoPricingFieldChanges() {
        // given
        LoanProduct product = buildMockProduct();
        JsonCommand command = buildCommand();
        when(loanProductRepository.findById(1L)).thenReturn(java.util.Optional.of(product));

        Map<String, Object> changes = new HashMap<>();
        // Only a non-pricing field changed
        changes.put("name", "New Name");
        when(loanProductUpdateUtil.update(product, command, aprCalculator, null)).thenReturn(changes);

        // when
        service.updateLoanProduct(1L, command);

        // then: no pricing change event should be emitted
        verify(businessEventNotifierService, never()).notifyPostBusinessEvent(any(LoanProductPricingChangeBusinessEvent.class));
    }

    @Test
    void shouldNotEmitPricingChangeEvent_whenChangesMapIsEmpty() {
        // given
        LoanProduct product = buildMockProduct();
        JsonCommand command = buildCommand();
        when(loanProductRepository.findById(1L)).thenReturn(java.util.Optional.of(product));

        // No changes at all
        when(loanProductUpdateUtil.update(product, command, aprCalculator, null)).thenReturn(new HashMap<>());

        // when
        service.updateLoanProduct(1L, command);

        // then
        verify(businessEventNotifierService, never()).notifyPostBusinessEvent(any(LoanProductPricingChangeBusinessEvent.class));
    }

    @Test
    void shouldEmitPricingChangeEvent_whenMultiplePricingFieldsChange() {
        // given
        LoanProduct product = buildMockProduct();
        JsonCommand command = buildCommand();
        when(loanProductRepository.findById(1L)).thenReturn(java.util.Optional.of(product));

        Map<String, Object> changes = new HashMap<>();
        // Multiple pricing fields changed at once
        changes.put(LoanProductConstants.INTEREST_RATE_PER_PERIOD, new BigDecimal("15.00"));
        changes.put(LoanProductConstants.PENALTY_RATE, new BigDecimal("3.00"));
        changes.put("interestCalculationPeriodType", 0);
        when(loanProductUpdateUtil.update(product, command, aprCalculator, null)).thenReturn(changes);

        // when
        service.updateLoanProduct(1L, command);

        // then: exactly one event is emitted even when multiple pricing fields change
        verify(businessEventNotifierService).notifyPostBusinessEvent(any(LoanProductPricingChangeBusinessEvent.class));
    }
}
