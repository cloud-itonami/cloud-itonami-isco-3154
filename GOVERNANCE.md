# Governance

This is a cloud-itonami blueprint project — an open occupation pattern for air traffic control administrative support (ISCO-08 3154). Governance follows the cloud-itonami fleet standards.

## Scope

This repository designs and maintains the ATC Support Actor reference implementation: a `langgraph.graph/state-graph` with an Advisor, independent Governor, and append-only audit ledger, gated by strict safety rules that structurally exclude any operation touching clearance issuance, separation instructions, real-time control, or any safety-critical operational authority.

## Decision-Making

- **Architecture & Safety Rules**: Decisions are made transparently via the codebase (src/atc_support/governor.cljc) and the `blueprint.edn` schema.
- **Changes to Safety Rules**: Any change to hard invariants or scope exclusions requires review and consensus from the cloud-itonami maintainers.
- **Code Contributions**: Follow standard open-source practices: fork → branch → PR → review → merge.

## Maintenance

- **Active Maintainers**: cloud-itonami core team.
- **Issue Triage**: Community reports are triaged and addressed in order.
- **Release Cycle**: Following cloud-itonami fleet versioning.

## Code of Conduct

All participants must adhere to the [Contributor Covenant](CODE_OF_CONDUCT.md).
