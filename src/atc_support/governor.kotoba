(ns atc-support.governor
  "ATCSupportGovernor — the independent safety/traceability layer for
  the ISCO-08 3154 air traffic control administrative support actor. Wired as its
  own `:govern` node in `atc-support.actor`'s StateGraph, downstream of
  `:advise`.

  CRITICAL SCOPE EXCLUSION — What This Actor Does NOT Do
  =========================================================
  This is an ADMINISTRATIVE SUPPORT actor ONLY. It NEVER:
    • Issues or authorizes any air traffic clearance (takeoff, landing, altitude, heading, speed)
    • Provides or coordinates any separation instruction or separation assurance
    • Makes any real-time go/no-go or safety-critical control decision
    • Processes, monitors, or analyzes live radar/flight-data for operational purposes
    • Coordinates real-time in-flight operations or live radio communication
    • Makes or influences any tactical airspace management decision
    • Authorizes facility equipment operational status or safety determinations
    • Delegates or assumes any operational control or pilot-in-command responsibility
    • Performs any time-critical decision required during active traffic handling

  It ONLY supports ground/administrative / pre-shift / post-shift back-office workflows.
  All proposals that touch any of the above are HARD blocks (`:hold`), never
  overridable.

  `check` is a pure function of (request, context, proposal, store) ->
  verdict; it never mutates the store. The StateGraph's `:decide` node
  routes on the verdict:
    :hard? true                → :hold  (irreversible, no write)
    :escalate? true            → :request-approval (interrupt-before)
    otherwise                  → :commit

  HARD invariants (:hard? true, ALWAYS :hold, never overridable):
    1. controller & facility provenance — request's controller-id and facility-id must
       be registered.
    2. no-actuation — proposal :effect must be :propose.
    3. no-operational-authority — any proposal op touching clearance issuance, separation,
       real-time control, or any safety-critical operational decision is permanently
       blocked (even :propose effect). HARD block, not escalation. Complete exclusion
       list in :disallowed-ops.

  ESCALATION invariants (:escalate? true, ALWAYS human sign-off):
    4. :op :flag-equipment-anomaly (always escalates — equipment anomalies
       always surface to human review; never auto-proceed)
    5. low confidence (< `confidence-floor`)"
  (:require [atc-support.store :as store]))

(def confidence-floor 0.6)

(def ^:private disallowed-ops
  "Operations permanently blocked (no proposal effect will permit these).
  Any proposal with these ops is a :hard? violation."
  #{:clearance-issuance
    :separation-instruction
    :go-no-go-decision
    :real-time-dispatch
    :airspace-management
    :radar-monitoring
    :flight-data-analysis
    :operational-authority
    :safety-determination
    :radio-coordination
    :tactical-separation
    :in-flight-decision})

(def ^:private escalating-ops
  "Operations that always require human sign-off (escalate via :request-approval)."
  #{:flag-equipment-anomaly})

(defn- hard-violations [{:keys [proposal]} controller-record facility-record]
  (cond-> []
    (nil? controller-record)
    (conj {:rule :no-controller :detail "未登録 controller"})

    (nil? facility-record)
    (conj {:rule :no-facility :detail "未登録 facility"})

    (not= :propose (:effect proposal))
    (conj {:rule :no-actuation :detail "effect は :propose のみ許可（直接書込禁止）"})

    (contains? disallowed-ops (:op proposal))
    (conj {:rule :forbidden-scope
           :detail "This operation touches clearance issuance, separation instructions, real-time control, or any safety-critical operational decision. Permanently excluded from this actor's scope."})))

(defn check
  "Assess a proposal against `request`/`context`/`proposal` and a `store`
  implementing `store.Store`. Returns
  `{:ok? bool :violations [...] :confidence n :hard? bool :escalate? bool}`."
  [request context proposal store]
  (let [controller-record (store/controller store (:controller-id request))
        facility-record (store/facility store (:facility-id request))
        hard (hard-violations {:proposal proposal} controller-record facility-record)
        hard? (boolean (seq hard))
        conf (or (:confidence proposal) 0.0)
        low? (< conf confidence-floor)
        escalating? (contains? escalating-ops (:op proposal))]
    {:ok? (and (not hard?) (not low?) (not escalating?))
     :violations hard
     :confidence conf
     :hard? hard?
     :escalate? (and (not hard?) (or low? escalating?))}))
