(ns kafka-connect-s3-sink.sink
  (:require [franzy.connect.sink :as sink]
            [clojure.tools.logging :as log]
            [cheshire.core :as json]
            [aws.sdk.s3 :as s3]
            [kafka-connect-s3-sink.config :refer [config]])
  (:import [java.io InputStream ByteArrayInputStream]))

(defn get-record-content [{:keys [content-path]} record]
  (let [content (-> (select-keys record [:key :value])
                    (get-in content-path))]
    (if (string? content)
      content
      (json/generate-string content))))

(defn get-record-filename [{:keys [filename-path prefix]} record]
  (->> filename-path
       (get-in record)
       (str prefix)))

(defn put-record [{:keys [bucket creds] :as cfg} record]
  (let [content (get-record-content cfg record)
        filename (get-record-filename cfg record)]
    (if (and filename content)
      (s3/put-object creds bucket filename content)
      (log/warn "Found an illegal record with a null filename or contents."))))

(defn make-creds [cfg]
  {:access-key (:aws.access_key_id cfg)
   :secret-key (:aws.secret_access_key cfg)
   :endpoint (:aws.endpoint cfg)})

(defn parse-json-path [json-path]
  (->> (json/parse-string json-path)
       (map #(if (string? %) (keyword %) %))
       vec))

(defn convert-task-config [cfg]
  {:filename-path (parse-json-path (:filename.path.json cfg))
   :content-path (parse-json-path (:content.path.json cfg))
   :bucket (:s3.bucket cfg)
   :prefix (:s3.prefix cfg)
   :creds (make-creds cfg)})

(sink/make-sink
 org.clojars.yanatan16.kafka.connect.s3.S3Sink
 {:start (fn [cfg]
           (let [taskcfg (convert-task-config cfg)]
             (log/infof "Starting S3 Sink Task with config: %s" (pr-str taskcfg))
             taskcfg))

  :stop (fn [_] (log/info "Stopping S3 Sink Task."))

  :put-1 (fn [cfg record]
           (log/debugf "Pushing kafka record %s" (pr-str record))
           (put-record cfg record)
           cfg)}

 {:config-def config
  :start (fn [cfg _]
           (log/infof "Starting S3 Sink Connector with config: %s" (pr-str cfg))
           cfg)
  :stop (fn [_] (log/info "Stopping S3 Sink Connector"))})
