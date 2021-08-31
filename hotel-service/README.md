# hotel-service

Run application
````bash
cd hotel-service
mvn quarkus:dev
````

Make a random purchase
```bash
curl -s -XPOST -H 'Content-Type: application/json' http://localhost:8081/hotel/buy -d '{ "item": "hotel St Regis", "customerId": "mike", "quantity": 1, "price": 100.00 }' | jq .
```
