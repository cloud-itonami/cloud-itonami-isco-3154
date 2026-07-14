(ns atc-support.store
  "SSoT for the ISCO-08 3154 air traffic control administrative support actor.
  Store is a protocol injected into the `atc-support.actor` StateGraph
  — `MemStore` is the default, deterministic, zero-dep backend; a
  Datomic/kotoba-server-backed implementation can be swapped in without
  touching the actor or governor (itonami actor pattern, per
  ADR-2607011000 / CLAUDE.md Actors section).

  Domain:

    controller  — a registered controller (:controller-id, :license, :facility-id)
    facility    — a registered ATC facility (:facility-id, :name, :type)
    operation   — a committed ground/administrative operation (shift handover note,
                 training record, equipment anomaly flag, facility maintenance coord)
                 — written ONLY via commit-operation!, never mutated in place
    ledger      — an append-only audit trail of every proposal/verdict/
                 disposition, regardless of outcome (commit or hold)")

(defprotocol Store
  (controller [s controller-id])
  (facility [s facility-id])
  (operations-of [s facility-id])
  (ledger [s])
  (register-controller! [s controller])
  (register-facility! [s facility])
  (commit-operation! [s operation])
  (append-ledger! [s fact]))

(defrecord MemStore [a]
  Store
  (controller [_ controller-id] (get-in @a [:controllers controller-id]))
  (facility [_ facility-id] (get-in @a [:facilities facility-id]))
  (operations-of [_ facility-id]
    (filter #(= facility-id (:facility-id %)) (:operations @a)))
  (ledger [_] (:ledger @a))
  (register-controller! [s controller]
    (swap! a assoc-in [:controllers (:controller-id controller)] controller) s)
  (register-facility! [s facility]
    (swap! a assoc-in [:facilities (:facility-id facility)] facility) s)
  (commit-operation! [s operation]
    (swap! a update :operations (fnil conj []) operation) s)
  (append-ledger! [s fact]
    (swap! a update :ledger (fnil conj []) fact) s))

(defn mem-store
  ([] (mem-store {}))
  ([seed] (->MemStore (atom (merge {:controllers {} :facilities {} :operations [] :ledger []} seed)))))
