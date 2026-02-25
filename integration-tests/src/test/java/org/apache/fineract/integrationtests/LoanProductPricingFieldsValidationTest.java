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

package org.apache.fineract.integrationtests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import org.apache.fineract.client.models.PostLoanProductsRequest;
import org.apache.fineract.client.models.PostLoanProductsResponse;
import org.apache.fineract.client.models.PutLoanProductsProductIdRequest;
import org.apache.fineract.client.models.PutLoanProductsProductIdResponse;
import org.apache.fineract.client.util.CallFailedRuntimeException;
import org.junit.jupiter.api.Test;

public class LoanProductPricingFieldsValidationTest extends BaseLoanIntegrationTest {

    /**
     * Test that penaltyRate field is accepted and validated during loan product creation
     */
    @Test
    public void testLoanProductCreationWithPenaltyRate() {
        // Create a loan product with valid penaltyRate
        final PostLoanProductsRequest request = create4IProgressive();
        request.setPenaltyRate(new BigDecimal("5.0"));

        final PostLoanProductsResponse response = loanProductHelper.createLoanProduct(request);
        assertNotNull(response);
        assertNotNull(response.getResourceId());
    }

    /**
     * Test that penaltyRate field with zero value is accepted
     */
    @Test
    public void testLoanProductCreationWithZeroPenaltyRate() {
        final PostLoanProductsRequest request = create4IProgressive();
        request.setPenaltyRate(BigDecimal.ZERO);

        final PostLoanProductsResponse response = loanProductHelper.createLoanProduct(request);
        assertNotNull(response);
        assertNotNull(response.getResourceId());
    }

    /**
     * Test that negative penaltyRate is rejected
     */
    @Test
    public void testLoanProductCreationWithNegativePenaltyRateFails() {
        final PostLoanProductsRequest request = create4IProgressive();
        request.setPenaltyRate(new BigDecimal("-5.0"));

        // Expect validation to fail with CallFailedRuntimeException
        assertThrows(CallFailedRuntimeException.class, () -> {
            loanProductHelper.createLoanProduct(request);
        });
    }

    /**
     * Test that penaltyRate field can be updated
     */
    @Test
    public void testLoanProductUpdateWithPenaltyRate() {
        // Create a loan product first
        final PostLoanProductsRequest createRequest = create4IProgressive();
        final PostLoanProductsResponse createResponse = loanProductHelper.createLoanProduct(createRequest);
        assertNotNull(createResponse.getResourceId());

        // Update with penaltyRate
        final PutLoanProductsProductIdRequest updateRequest = new PutLoanProductsProductIdRequest();
        updateRequest.setPenaltyRate(new BigDecimal("3.5"));

        final PutLoanProductsProductIdResponse updateResponse = loanProductHelper
                .updateLoanProductById(createResponse.getResourceId(), updateRequest);
        assertNotNull(updateResponse);
        assertNotNull(updateResponse.getResourceId());
        assertEquals(createResponse.getResourceId(), updateResponse.getResourceId());
    }

    /**
     * Test that interestRatePerPeriod validation still works (existing field)
     */
    @Test
    public void testLoanProductUpdateWithInterestRatePerPeriod() {
        // Create a loan product first
        final PostLoanProductsRequest createRequest = create4IProgressive();
        final PostLoanProductsResponse createResponse = loanProductHelper.createLoanProduct(createRequest);
        assertNotNull(createResponse.getResourceId());

        // Update with new interest rate
        final PutLoanProductsProductIdRequest updateRequest = new PutLoanProductsProductIdRequest();
        updateRequest.setInterestRatePerPeriod(new BigDecimal("8.5"));

        final PutLoanProductsProductIdResponse updateResponse = loanProductHelper
                .updateLoanProductById(createResponse.getResourceId(), updateRequest);
        assertNotNull(updateResponse);
        assertEquals(createResponse.getResourceId(), updateResponse.getResourceId());
    }

    /**
     * Test that interestCalculationPeriodType validation still works (existing field)
     */
    @Test
    public void testLoanProductUpdateWithInterestCalculationMethod() {
        // Create a loan product first
        final PostLoanProductsRequest createRequest = create4IProgressive();
        final PostLoanProductsResponse createResponse = loanProductHelper.createLoanProduct(createRequest);
        assertNotNull(createResponse.getResourceId());

        // Update with interest calculation period type (0 = DAILY, 1 = SAME_AS_REPAYMENT_PERIOD)
        final PutLoanProductsProductIdRequest updateRequest = new PutLoanProductsProductIdRequest();
        updateRequest.setInterestCalculationPeriodType(0); // DAILY

        final PutLoanProductsProductIdResponse updateResponse = loanProductHelper
                .updateLoanProductById(createResponse.getResourceId(), updateRequest);
        assertNotNull(updateResponse);
        assertEquals(createResponse.getResourceId(), updateResponse.getResourceId());
    }

    /**
     * Test that all three pricing fields can be updated together
     */
    @Test
    public void testLoanProductUpdateWithAllPricingFields() {
        // Create a loan product first
        final PostLoanProductsRequest createRequest = create4IProgressive();
        final PostLoanProductsResponse createResponse = loanProductHelper.createLoanProduct(createRequest);
        assertNotNull(createResponse.getResourceId());

        // Update with all three pricing fields
        final PutLoanProductsProductIdRequest updateRequest = new PutLoanProductsProductIdRequest();
        updateRequest.setInterestRatePerPeriod(new BigDecimal("7.0"));
        updateRequest.setPenaltyRate(new BigDecimal("2.5"));
        updateRequest.setInterestCalculationPeriodType(1); // SAME_AS_REPAYMENT_PERIOD

        final PutLoanProductsProductIdResponse updateResponse = loanProductHelper
                .updateLoanProductById(createResponse.getResourceId(), updateRequest);
        assertNotNull(updateResponse);
        assertEquals(createResponse.getResourceId(), updateResponse.getResourceId());
    }
}
