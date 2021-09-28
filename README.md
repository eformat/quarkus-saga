# quarkus-saga

Implementing the Saga Pattern in quarkus using quarkus-lra.

The LRA (short for Long Running Action) participant extension is useful in microservice based designs where different services can benefit from a relaxed notion of distributed consistency.

When booking a trip (a flight and a hotel), the payment service has an induced error rate which causes compensating actions.

- http://quarkus.io/guides/lra
- https://github.com/quarkusio/quarkusio.github.io/blob/d2cb1bc3c2b8f78c3b1a02fda1f6a44e6a836fca/_posts/2021-08-23-using-lra.adoc


## Running locally

Run kafka cluster 
```bash
podman-compose up -d
```

(Optional) Create kafka topics
```bash
add_path /opt/kafka_2.13-2.8.0/bin
kafka-topics.sh --create --bootstrap-server localhost:9092 --replication-factor 1 --partitions 1 --topic bookings
kafka-topics.sh --create --bootstrap-server localhost:9092 --replication-factor 1 --partitions 1 --topic payments
```

Check
```bash
kafkacat -b localhost:9092 -L
```

Watch processed topic
```bash
kafkacat -b localhost:9092 -t bookings -o beginning -C -f '\nKey (%K bytes): %k
Value (%S bytes): %s
Timestamp: %T
Partition: %p
Offset: %o
Headers: %h'

kafkacat -b localhost:9092 -t payments -o beginning -C -f '\nKey (%K bytes): %k
Value (%S bytes): %s
Timestamp: %T
Partition: %p
Offset: %o
Headers: %h'
```

Install data-library
```bash
cd data-library && mvn clean package install
```

Package Apps
```bash
mvn clean package
```

Run lra coordinator
```bash
java -Dquarkus.http.port=50000 -jar narayana-lra-coordinator/target/quarkus-app/quarkus-run.jar &
```

Run Apps using dev profile locally
```bash
java -Dquarkus.profile=dev -jar ./flight-service/target/quarkus-app/quarkus-run.jar
java -Dquarkus.profile=dev -jar ./hotel-service/target/quarkus-app/quarkus-run.jar
java -Dquarkus.profile=dev -jar ./payment-service/target/quarkus-app/quarkus-run.jar
java -Dquarkus.profile=dev -jar ./booking-service/target/quarkus-app/quarkus-run.jar
```

Book a trip
```bash
curl -s -vv -H 'accept: */*' http://localhost:8080/trip/book | jq .
```

Make individual flight, hotel, payment for testing
```bash
curl -s -XPOST -H 'Content-Type: application/json' http://localhost:8081/hotel/buy -d '{ "item": "hotel", "customerId": "mike", "quantity": 1, "price": 100.00 }' | jq .
curl -s -XPOST -H 'Content-Type: application/json' http://localhost:8082/flight/buy -d '{ "item": "flight", "customerId": "mike", "quantity": 1, "price": 100.00 }' | jq .
curl -s -XPOST -H 'Content-Type: application/json' http://localhost:8083/payment/pay -d '{ "item": "flight", "customerId": "mike", "quantity": 1, "price": 100.00 }' | jq .
```

Check LRA - should be empty if no failures
```bash
curl -s http://localhost:50000/lra-coordinator | jq .
```

## Introduce random failure

Introduce some random failure into the payment service:
```java
diff --git a/payment-service/src/main/java/org/acme/PaymentService.java b/payment-service/src/main/java/org/acme/PaymentService.java
index 94a6293..4f63c47 100644
--- a/payment-service/src/main/java/org/acme/PaymentService.java
+++ b/payment-service/src/main/java/org/acme/PaymentService.java
@@ -59,8 +59,8 @@ public class PaymentService {
     public Response pay(@HeaderParam(LRA_HTTP_CONTEXT_HEADER) URI lraId, Purchase purchase) {
         purchase.setItem("PAYMENT# ".concat(purchase.getItem()));
         log.info(">>> Payment received for LRA {} and Purchase {}", lraId, purchase);
-//        if (new SecureRandom().nextBoolean())
-//            return Response.serverError().build();
+        if (new SecureRandom().nextBoolean())
+            return Response.serverError().build();
         payments.send(KafkaRecord.of(lraId, purchase));
         JsonObject response = new JsonObject().put("message", "Payment Made LRA #" + lraId);
         response.put("purchase", JsonObject.mapFrom(purchase));
```

Rebuild payment-service and restart it
```bash
cd payment-service && mvn clean package
java -Dquarkus.profile=dev -jar ./payment-service/target/quarkus-app/quarkus-run.jar
```

Now when a failure happens, you should see compensating actions (CANCEL of bookings and payments) in the Kafka (bookings, payments) topics.
