# flight-service

Run application
````bash
cd flight-service
mvn quarkus:dev
````

Make a random purchase
```bash
curl -s -H 'accept: */*' http://localhost:8082/flight/buy | jq .
```

