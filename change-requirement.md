# Change Request: Automatic Update of Active Loans When Product Interest Rate Changes

## Request Raised By
Credit Operations Department

## Date
[Insert Date]

---

## 1. Background

Currently, when the interest rate or penalty configuration of a Loan Product is modified, the change does not automatically apply to loans that have already been disbursed under that product.

This creates operational challenges, as the team must manually identify and update each affected loan individually.

---

## 2. Business Problem

When there is:

- A change in benchmark rate (e.g., repo-linked pricing)
- A credit committee decision to revise product pricing
- A regulatory or policy-driven pricing revision
- A risk-based repricing decision

The revised rate needs to be applied to all relevant active loans.

However, at present:

- Product-level changes do not automatically flow to active loans.
- Operations teams must manually update loans one by one.
- There is a risk of:
  - Missing some accounts
  - Incorrect effective date handling
  - Audit observations
  - Inconsistent customer treatment

This increases operational workload and compliance risk.

---

## 3. Objective

To enable automatic identification and update of active loans whenever a Loan Product’s interest rate or penalty settings are revised.

The system should ensure consistent and controlled propagation of pricing changes.

---

## 4. Scope of Change

### In Scope

- Changes to:
  - Nominal interest rate
  - Penalty interest rate
  - Interest recalculation configuration
- Loans that are:
  - Active
  - Disbursed
  - Not closed or written off

### Out of Scope (Phase 1)

- Closed loans
- Written-off loans
- Fully repaid loans
- Special restructured loans (to be reviewed separately)

---

## 5. Functional Requirements

### FR-1: Detection of Pricing Change

When a Loan Product is modified, the system must detect whether the following have changed:

- Interest rate
- Penalty rate
- Interest calculation method

If none of the above changed, no further action is required.

---

### FR-2: Identification of Affected Loans

The system must:

- Identify all active loans linked to the modified Loan Product
- Exclude:
  - Closed loans
  - Written-off loans
  - Loans with approved custom interest override (if applicable)

---

### FR-3: Effective Date Handling

User must provide an effective date for the pricing change.

If the effective date is:

- Today or future → apply prospectively
- Backdated → recalculate interest from the effective date and adjust accordingly

---

### FR-4: Preservation of Overrides

If a loan has a manually approved custom interest rate or special concession:

- That loan should not be automatically modified
- It should be flagged for manual review

---

### FR-5: Audit Trail

The system must log:

- Product ID
- Fields changed
- Effective date
- Number of loans impacted
- Number of loans excluded
- User initiating the change
- Date and time of execution

---

## 6. Expected Outcome

After implementation:

- Product interest rate changes will automatically apply to relevant active loans.
- Manual bulk updates will no longer be required.
- Risk of inconsistency will reduce.
- Audit traceability will improve.
- Operational efficiency will increase.

---

## 7. Business Justification

This change will:

- Reduce operational workload
- Minimize pricing inconsistency
- Improve compliance posture
- Reduce manual errors
- Ensure faster execution of pricing decisions

---

## 8. Priority

High

Given the regulatory and financial impact of pricing errors, this change is considered important for operational stability and compliance.

---

## 9. Requested Timeline

To be evaluated by Product and Engineering teams.

---

## 10. Additional Notes

This request does not require changes to how interest is calculated.  
It only requires automated propagation of product-level pricing changes to eligible active loans under controlled conditions.
