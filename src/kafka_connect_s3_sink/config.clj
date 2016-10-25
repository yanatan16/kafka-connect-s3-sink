(ns kafka-connect-s3-sink.config
  (:require [franzy.connect.config :refer [make-config-def] :as cfg]
            [cheshire.core :as json]))


(def json-path-validator
  (cfg/validator
   (fn [val]
     (try (let [path (json/parse-string val true)]
            (and (seq? path)
                 (or (#{"value" "key"} (first path))
                     (zero? (count path)))
                 (every? #(or (string? %) (integer? %)) path)))
          (catch Exception e false)))
   "Invalid JSON"))

(def config
  (make-config-def
   (:filename.path.json :type/string ::cfg/no-default-value
                        json-path-validator :importance/high
                        "JSON path of nested keys to get the filename of the record. Can include numbers for array references. First key should be either \"key\" or \"value\".")
   #_(:content.path.json :type/string "[\"value\"]"
                       json-path-validator :importance/medium
                       "JSON path of nested keys to get content of the record. Can include numbers for array references. First key should be either \"key\" or \"value\". Defaults to [\"value\"].")
   (:s3.bucket :type/string ::cfg/no-default-value :importance/high
               "S3 Bucket")

   (:s3.prefix :type/string "" :importance/low
               "Prefix on filename. Usually a folder name followed by delimiter (usually /). Defaults to empty string.")
   (:aws.access_key_id :type/string ::cfg/no-default-value :importance/high
                       "AWS Access Key ID")
   (:aws.secret_access_key :type/string ::cfg/no-default-value :importance/high
                           "AWS Secret Access Key")
   (:aws.endpoint :type/string "us-east-1" :importance/medium
                  "AWS Endpoint")))
