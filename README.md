# cloud-itonami-isco-3154

Open Occupation Blueprint for **ISCO-08 3154**: Air Traffic Controllers.

This repository designs a forkable OSS blueprint for air traffic control **administrative support** as a governor-gated actor: supporting shift handover documentation, controller training records, equipment anomaly flagging, and facility maintenance coordination under an independent **ATC Support Governor** that ensures no proposal touches clearance issuance, separation instructions, real-time control, or any safety-critical operational authority.

## CRITICAL: Scope Exclusion — What This Actor Does NOT Do

**This is an ADMINISTRATIVE SUPPORT actor only, for ground/back-office ATC workflow.**

This actor NEVER:
- Issues or authorizes any air traffic clearance (takeoff, landing, altitude, heading, speed)
- Provides or coordinates any separation instruction or separation assurance
- Makes any real-time go/no-go or safety-critical control decision
- Processes, monitors, or analyzes live radar/flight-data for operational purposes
- Coordinates real-time in-flight operations or live radio communication
- Makes or influences any tactical airspace management decision
- Authorizes facility equipment operational status or safety determinations
- Delegates or assumes any operational control or pilot-in-command responsibility
- Performs any time-critical decision required during active traffic handling

**Any proposal attempting any of the above is a hard, permanent block** — even with `:propose` effect. Scope violations are never escalable to human sign-off; they are structurally excluded from this actor's vocabulary entirely.

## ATC Support Premise

All cloud-itonami verticals are designed on the premise that decision-making is gated by an independent governor. Here, an **ATC Support Governor** gates all proposals under strict safety rules: the advisor (mock or LLM) can only propose ground/administrative actions (shift handover notes, training records, equipment anomaly flags, facility maintenance scheduling). The governor rejects any proposal that strays into clearance issuance, separation instructions, real-time operations, or any safety-critical authority. The governor never dispatches actions itself; equipment anomalies always escalate to human review.

## Core Contract

```text
atc support request (administrative/ground only)
        |
        v
ATC Support Advisor -> ATC Support Governor -> support action or human escalation
        |
        v
committed operation record + audit ledger
```

No automated advice can issue a clearance, provide separation instructions, dispatch a safety-critical operation the governor refuses, suppress an operating record, or touch live airspace/real-time systems.

## Capability Layer

Resolves via [`kotoba-lang/occupation`](https://github.com/kotoba-lang/occupation)
(ISCO-08 `3154`). Required capabilities:

- :identity
- :forms
- :dmn
- :bpmn
- :audit-ledger

See [`docs/business-model.md`](docs/business-model.md) and
[`docs/operator-guide.md`](docs/operator-guide.md).

## Reference Implementation (`:maturity :implemented`)

Full itonami Actor pattern (per ADR-2607011000 / CLAUDE.md's Actors section): a real
[`kotoba-lang/langgraph`](https://github.com/kotoba-lang/langgraph)
`StateGraph`, with the Advisor and Governor as distinct graph nodes and
human-in-the-loop interrupt/resume via checkpointing.

```text
:intake -> :advise -> :govern -> :decide -+-> :commit            (:ok? true)
                                          +-> :request-approval   (:escalate? true, interrupt-before)
                                          +-> :hold               (:hard? true)
```

- `src/atc_support/store.kotoba` — `Store` protocol + `MemStore`:
  registered controllers and facilities, committed operations, an append-only audit ledger.
- `src/atc_support/advisor.kotoba` — `Advisor` protocol; `mock-advisor`
  (deterministic, default) proposes an ATC support action from a
  request; `llm-advisor` wraps a `langchain.model/ChatModel` — either
  way the advisor only ever produces a `:propose`-effect proposal,
  never a committed record, and LLM parse failures always yield
  `:op :unknown` with `:confidence 0.0` (held by the governor as an
  unknown op, never fabricated confidence).
- `src/atc_support/governor.kotoba` — `ATCSupportGovernor/check`: a pure
  function, wired as its own `:govern` node. Hard invariants
  (unregistered controller/facility, a proposal whose `:effect` isn't `:propose`,
  any operation touching clearance issuance / separation / real-time control / safety authority,
  and any op outside the actor's four-op vocabulary) always route to `:hold`. Escalation invariants (`:flag-equipment-anomaly`,
  or low advisor confidence) always route to `:request-approval` — an
  `interrupt-before` node that the graph checkpoints and only resumes on
  explicit human approval (`actor/approve!`).
- `src/atc_support/facts.kotoba` — `permitted-ops` (the four ops the
  advisor may propose, with their meanings) and `escalating-ops`. The
  governor holds any op outside this set as `:unknown-op`; before it did,
  an LLM reply naming e.g. `:issue-notam` at confidence 0.95 reached
  `:commit`, because `disallowed-ops` lists only what is forbidden.
- `src/atc_support/actor.kotoba` — `build-graph`, `run-request!`,
  `approve!`: the `langgraph.graph/state-graph` wiring itself.

```bash
kbb -M:test
```

23 tests / 62 assertions green. `run_tests.kotoba` refuses (exit 2) to
report a pass below that published count, on 0 sources, or on 0 test
namespaces; failures exit 1. (`cognitect.test-runner`, which `:test` named
until 2026-09-25, resolves only `.clj`/`.cljc` and could not run the
`.kotoba` suite: `kbb test: 0 test namespace(s) found`.)

This is what backs this repo's `:maturity :implemented` entry in
[`kotoba-lang/occupation`](https://github.com/kotoba-lang/occupation).

## License

AGPL-3.0-or-later.
