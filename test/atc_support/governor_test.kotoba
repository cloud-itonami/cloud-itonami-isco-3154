(ns atc-support.governor-test
  (:require [clojure.test :refer [deftest is testing]]
            [atc-support.store :as store]
            [atc-support.governor :as governor]))

(defn- fresh-store []
  (let [st (store/mem-store)]
    (store/register-facility! st {:facility-id "PHL" :name "Philadelphia International" :type "tower"})
    (store/register-controller! st {:controller-id "ctrl-1" :license "Advanced Rating" :facility-id "PHL"})
    st))

(deftest ok-on-clean-shift-handover-note
  (let [st (fresh-store)
        proposal {:op :log-shift-handover-note :effect :propose :confidence 0.9 :stake :low}
        v (governor/check {:controller-id "ctrl-1" :facility-id "PHL"} {} proposal st)]
    (is (:ok? v))
    (is (not (:hard? v)))
    (is (not (:escalate? v)))))

(deftest ok-on-draft-training-record
  (let [st (fresh-store)
        proposal {:op :draft-training-record :effect :propose :confidence 0.9 :stake :low}
        v (governor/check {:controller-id "ctrl-1" :facility-id "PHL"} {} proposal st)]
    (is (:ok? v))
    (is (not (:hard? v)))
    (is (not (:escalate? v)))))

(deftest ok-on-coordinate-facility-maintenance
  (let [st (fresh-store)
        proposal {:op :coordinate-facility-maintenance :effect :propose :confidence 0.9 :stake :low}
        v (governor/check {:controller-id "ctrl-1" :facility-id "PHL"} {} proposal st)]
    (is (:ok? v))
    (is (not (:hard? v)))
    (is (not (:escalate? v)))))

(deftest hard-on-unregistered-controller
  (let [st (fresh-store)
        proposal {:op :log-shift-handover-note :effect :propose :confidence 0.9 :stake :low}
        v (governor/check {:controller-id "ctrl-9999" :facility-id "PHL"} {} proposal st)]
    (is (:hard? v))
    (is (some #(= :no-controller (:rule %)) (:violations v)))))

(deftest hard-on-unregistered-facility
  (let [st (fresh-store)
        proposal {:op :log-shift-handover-note :effect :propose :confidence 0.9 :stake :low}
        v (governor/check {:controller-id "ctrl-1" :facility-id "ZZZ"} {} proposal st)]
    (is (:hard? v))
    (is (some #(= :no-facility (:rule %)) (:violations v)))))

(deftest hard-on-no-actuation-violation
  (let [st (fresh-store)
        proposal {:op :log-shift-handover-note :effect :direct-write :confidence 0.9 :stake :low}
        v (governor/check {:controller-id "ctrl-1" :facility-id "PHL"} {} proposal st)]
    (is (:hard? v))
    (is (some #(= :no-actuation (:rule %)) (:violations v)))))

(deftest hard-on-clearance-issuance-attempt
  (let [st (fresh-store)
        proposal {:op :clearance-issuance :effect :propose :confidence 0.9 :stake :high}
        v (governor/check {:controller-id "ctrl-1" :facility-id "PHL"} {} proposal st)]
    (is (:hard? v))
    (is (some #(= :forbidden-scope (:rule %)) (:violations v)))))

(deftest hard-on-separation-instruction-attempt
  (let [st (fresh-store)
        proposal {:op :separation-instruction :effect :propose :confidence 0.9 :stake :high}
        v (governor/check {:controller-id "ctrl-1" :facility-id "PHL"} {} proposal st)]
    (is (:hard? v))
    (is (some #(= :forbidden-scope (:rule %)) (:violations v)))))

(deftest hard-on-real-time-dispatch-attempt
  (let [st (fresh-store)
        proposal {:op :real-time-dispatch :effect :propose :confidence 0.9 :stake :high}
        v (governor/check {:controller-id "ctrl-1" :facility-id "PHL"} {} proposal st)]
    (is (:hard? v))
    (is (some #(= :forbidden-scope (:rule %)) (:violations v)))))

(deftest hard-on-radar-monitoring-attempt
  (let [st (fresh-store)
        proposal {:op :radar-monitoring :effect :propose :confidence 0.9 :stake :high}
        v (governor/check {:controller-id "ctrl-1" :facility-id "PHL"} {} proposal st)]
    (is (:hard? v))
    (is (some #(= :forbidden-scope (:rule %)) (:violations v)))))

(deftest escalates-on-equipment-anomaly
  (let [st (fresh-store)
        proposal {:op :flag-equipment-anomaly :effect :propose :confidence 0.9 :stake :high}
        v (governor/check {:controller-id "ctrl-1" :facility-id "PHL"} {} proposal st)]
    (is (:escalate? v))
    (is (not (:hard? v)))))

(deftest escalates-on-low-confidence
  (let [st (fresh-store)
        proposal {:op :log-shift-handover-note :effect :propose :confidence 0.2 :stake :low}
        v (governor/check {:controller-id "ctrl-1" :facility-id "PHL"} {} proposal st)]
    (is (:escalate? v))
    (is (not (:hard? v)))))

(deftest store-operations-and-ledger-append-only
  (let [st (fresh-store)]
    (store/commit-operation! st {:controller-id "ctrl-1" :facility-id "PHL" :op :log-shift-handover-note})
    (store/append-ledger! st {:disposition :commit})
    (is (= 1 (count (store/operations-of st "PHL"))))
    (is (= 1 (count (store/ledger st))))))
