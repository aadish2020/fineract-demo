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
package org.apache.fineract.infrastructure.event.business.domain.loan.product;

import java.util.Map;
import lombok.Getter;
import org.apache.fineract.portfolio.loanproduct.domain.LoanProduct;

/**
 * Business event raised when pricing parameters (interest rate, penalty rate, or interest calculation method) of a
 * Loan Product are changed. This event is used to trigger workflow that updates active loans associated with the
 * product.
 */
@Getter
public class LoanProductPricingParameterChangeBusinessEvent extends LoanProductBusinessEvent {

    private static final String TYPE = "LoanProductPricingParameterChangeBusinessEvent";

    private final Map<String, Object> changes;

    public LoanProductPricingParameterChangeBusinessEvent(LoanProduct value, Map<String, Object> changes) {
        super(value);
        this.changes = changes;
    }

    @Override
    public String getType() {
        return TYPE;
    }
}
