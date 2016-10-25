# kafka-connect-s3-sink

A kafka sink connector for pushing records to s3. Different from other s3 connectors, this connector pushes single records as files in s3, and names them as a value from the record itself. It currently only supports JSON formatting.

Since it writes a file for each record, this connector is not suitable for high-throughput topics or buckets that you often LIST all keys.

## Usage

To install into a kafka-connect classpath, simply download an uberjar from the releases page or build it yourself:

```
lein uberjar
cp target/kafka-connect-s3-sink-standalone.jar <destination classpath>
```

Then you can start connectors as normal through the REST API or Confluent's Kafka Control Center.

## Configuration

The `connector.class` is `org.clojars.yanatan16.kafka.connect.s3.S3SinkConnector`.

It has the following custom configurations (above and beyond the [normal sink configurations](http://docs.confluent.io/2.0.0/connect/userguide.html#configuring-connectors)).

- `filename.path.json` An JSON vector of get-in keys, such as `["key", "id"]` to get the `id` field in the record key. Or the key itself: `["key"]`. (Required)
- `content.path.json` An JSON vector of get-in keys, such as `["value","data"]` to get the `data` field in the record value. Or the value itself: `["value"]`. If this points to a string, the string itself will be written, otherwise it will be JSON encoded. (Required)
- `s3.bucket` S3 Bucket (required)
- `s3.prefix` Prefix on the filename (possibly a directory, defaults to empty string)
- `aws.access_key_id` AWS Access Key ID
- `aws.secret_access_key` AWS Secret Access Key

## Testing

Unit tests can be run in the normal clojure way:

```
lein test
```

Integration tests have not been set up yet, but you can set up a test pipeline by getting a slack token and a docker daemon setup:

```
export DOCKER=<docker_host>

# Start the zk/kafka/connect containers (uberjar is important!)
lein uberjar # if you forget this, you might need restart your docker machine
docker-compose up -d

# Now insert things into kafka. I like to use kafkacat
echo '{"id":"task123", "thing": "wrote a kafka-connector s3 sink for pushing records."}' | kafkacat -b $DOCKER:9092 -t test-topic -P

# and send it!
curl $DOCKER:8083/connectors -XPOST -H'Content-type: application/json' -H'Accept: application/json' -d '{
  "name": "kafka-connect-s3-sink-test",
  "config": {
      "topics": "test-topic",
      "connector.class": "org.clojars.yanatan16.kafka.connect.s3.S3SinkConnector",
      "filename.path.json": "[\"value\",\"id\"]",
      "content.path.json": "[\"value\"]",
      "s3.bucket": "MY_BUCKET",
      "s3.prefix": "folder/",
      "aws.access_key_id": "MY_AWS_ACCESS_KEY_ID",
      "aws.secret_access_key": "MY_AWS_SECRET_ACCESS_KEY"
  }
}'

# Now check s3! Your post should be there.
aws s3 ls s3://${S3_BUCKET}/${S3_PREFIX}

# If not, look at:
docker-compose logs
```

## License

See [LICENSE](/LICENSE) file.
