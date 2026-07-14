# Security Policy

## Scope

This repository implements an air traffic control **administrative support** actor for ground/back-office workflow. **It does not issue clearances, provide separation instructions, make real-time control decisions, or exercise operational authority.**

All safety invariants are enforced in `src/atc_support/governor.cljc`:
- Hard blocks on any operation touching clearance issuance, separation instructions, real-time control, or any safety-critical operational decision.
- Equipment anomalies always escalate to human review.
- No proposal can bypass the Governor.

## Reporting Security Issues

If you discover a security vulnerability, **please do not open a public issue**. Instead:

1. Contact the cloud-itonami maintainers privately via email or GitHub security advisory.
2. Include:
   - A clear description of the vulnerability.
   - Steps to reproduce (if applicable).
   - Potential impact (especially any violation of scope exclusions or hard invariants).
3. Allow time for the maintainers to respond and develop a fix before public disclosure.

## Safety-Critical Constraints

- **Scope Exclusion is Permanent**: Any proposal or code change that touches clearance issuance, separation instructions, real-time control, or any safety-critical operational decision is fundamentally out of scope and will be rejected, regardless of how it is framed.
- **Governor is Unreducible**: The Governor (`src/atc_support/governor.cljc`) gates every operation. Do not attempt to bypass or downgrade safety rules without explicit maintainer consensus.
- **Audit Ledger is Append-Only**: All operations are logged. No retroactive deletion or modification of audit records is permitted.

## Expectations

This is reference-implementation code for a blueprint. Operators deploying this code are responsible for:
- Understanding the actor's strict scope limitations.
- Integrating it safely into their operational workflows.
- Ensuring human sign-off is actually performed for escalated operations.
- Maintaining their own audit and compliance procedures.

## Acknowledgments

Security issues and vulnerability reports help keep this project safe. Thank you for responsible disclosure.
