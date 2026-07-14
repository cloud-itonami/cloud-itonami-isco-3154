(ns atc-support.actor-test
  (:require [clojure.test :refer [deftest is testing]]
            [atc-support.store :as store]
            [atc-support.advisor :as advisor]
            [atc-support.actor :as actor]))

(defn- fresh-store-and-graph []
  (let [st (store/mem-store)]
    (store/register-facility! st {:facility-id "PHL" :name "Philadelphia International" :type "tower"})
    (store/register-controller! st {:controller-id "ctrl-1" :license "Advanced Rating" :facility-id "PHL"})
    (let [g (actor/build-graph {:store st :advisor (advisor/mock-advisor)})]
      [st g])))

(deftest run-clean-shift-handover-to-commit
  (let [[st g] (fresh-store-and-graph)
        request {:controller-id "ctrl-1" :facility-id "PHL" :op :log-shift-handover-note :stake :low}
        result (actor/run-request! g request {} "thread-1")]
    (is (= :done (:status result)))
    (is (not (:interrupted result)))
    (is (some? (-> result :state :operation)))
    (is (= :log-shift-handover-note (-> result :state :operation :op)))))

(deftest run-request-escalates-on-equipment-anomaly
  (let [[st g] (fresh-store-and-graph)
        request {:controller-id "ctrl-1" :facility-id "PHL" :op :flag-equipment-anomaly :stake :high}
        result (actor/run-request! g request {} "thread-2")]
    (is (= :interrupted (:status result)))
    (is (some? (-> result :state :proposal)))
    (is (= :flag-equipment-anomaly (-> result :state :proposal :op)))))

(deftest run-request-holds-on-hard-violation
  (let [[st g] (fresh-store-and-graph)
        request {:controller-id "ctrl-9999" :facility-id "PHL" :op :log-shift-handover-note :stake :low}
        result (actor/run-request! g request {} "thread-3")]
    (is (= :done (:status result)))
    (is (nil? (-> result :state :operation)))
    (is (some? (-> result :state :verdict)))))

(deftest approve-escalated-request
  (let [[st g] (fresh-store-and-graph)
        req1 {:controller-id "ctrl-1" :facility-id "PHL" :op :flag-equipment-anomaly :stake :high}
        result1 (actor/run-request! g req1 {} "thread-4")]
    (is (= :interrupted (:status result1)))
    (let [result2 (actor/approve! g "thread-4")]
      (is (= :done (:status result2)))
      (is (some? (-> result2 :state :operation))))))

(deftest audit-ledger-records-all-dispositions
  (let [[st g] (fresh-store-and-graph)
        req1 {:controller-id "ctrl-1" :facility-id "PHL" :op :log-shift-handover-note :stake :low}
        _result1 (actor/run-request! g req1 {} "thread-5")
        ledger (store/ledger st)]
    (is (> (count ledger) 0))
    (is (some #(= :commit (:disposition %)) ledger))))
