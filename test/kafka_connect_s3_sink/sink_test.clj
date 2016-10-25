(ns kafka-connect-s3-sink.sink-test
  (:require [clojure.test :refer [deftest is]]
            [kafka-connect-s3-sink.sink :refer :all]))

(deftest get-record-content-test
  (is (= (get-record-content {:content-path []}
                             {:key "key" :value "value"})
         "{\"key\":\"key\",\"value\":\"value\"}"))
  (is (= (get-record-content {:content-path [:value :data]}
                             {:value {:data "base64"}})
         "base64")))

(deftest get-record-filename-test
  (is (= (get-record-filename {:filename-path [:key] :prefix ""} {:key "key"})
         "key"))
  (is (= (get-record-filename {:filename-path [:key :id] :prefix ""} {:key {:id "key"}})
         "key"))
  (is (= (get-record-filename {:filename-path [:key] :prefix "folder/"} {:key "key"})
         "folder/key")))

(deftest make-creds-test
  (is (= (make-creds {:aws.access_key_id "access-key"
                      :aws.secret_access_key "secret-key"
                      :aws.endpoint "endpoint"})
         {:access-key "access-key"
          :secret-key "secret-key"
          :endpoint "endpoint"})))

(deftest parse-json-path-test
  (is (= (parse-json-path "[]") []))
  (is (= (parse-json-path "[\"key\"]") [:key]))
  (is (= (parse-json-path "[\"key\", \"id\"]") [:key :id]))
  (is (= (parse-json-path "[\"value\", 0, \"id\"]") [:value 0 :id])))

(deftest convert-task-config-test
  (is (= (convert-task-config {:aws.access_key_id "access-key"
                               :aws.secret_access_key "secret-key"
                               :aws.endpoint "endpoint"
                               :filename.path.json "[\"value\", \"id\"]"
                               :content.path.json "[\"value\"]"
                               :s3.bucket "bucket"
                               :s3.prefix "folder/"})
         {:filename-path [:value :id]
          :content-path [:value]
          :bucket "bucket"
          :prefix "folder/"
          :creds {:access-key "access-key"
                  :secret-key "secret-key"
                  :endpoint "endpoint"}})))

(deftest sink-available
  (is (instance? org.apache.kafka.connect.sink.SinkConnector
                 (new org.clojars.yanatan16.kafka.connect.s3.S3SinkConnector)))
  (is (instance? org.apache.kafka.connect.sink.SinkTask
                 (new org.clojars.yanatan16.kafka.connect.s3.S3SinkTask))))
