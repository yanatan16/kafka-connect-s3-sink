(ns kafka-connect-s3-sink.config-test
  (:require [clojure.test :refer [deftest is]]
            [kafka-connect-s3-sink.config :refer :all])
  (:import [org.apache.kafka.common.config ConfigException]))

(deftest json-path-validator-test
  (is (nil? (.ensureValid json-path-validator "foo" "[]")))
  (is (nil? (.ensureValid json-path-validator "foo" "[\"key\"]")))
  (is (nil? (.ensureValid json-path-validator "foo" "[\"value\", \"field\", 0]")))

  (is (thrown? ConfigException (.ensureValid json-path-validator "foo" "")))
  (is (thrown? ConfigException (.ensureValid json-path-validator "foo" "{\"value\": \"key\"}")))
  (is (thrown? ConfigException (.ensureValid json-path-validator "foo" "[\"value\" {\"foo\": \"bar\"}]")))
  (is (thrown? ConfigException (.ensureValid json-path-validator "foo" "\"foo\"")))
  (is (thrown? ConfigException (.ensureValid json-path-validator "foo" "badjson"))))
