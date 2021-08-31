# booking-service

Make a random trip
```bash
curl -vv -H 'accept: */*' http://localhost:8080/trip/book | jq .
```

Cancel a trip
```bash
curl -vv -XPUT --header "Long-Running-Action: $LRA_URL" -H 'accept: */*' http://localhost:8080/trip/complete | jq .
```

Forget a trip
```bash
curl -vv -XDELETE --header "Long-Running-Action: $LRA_URL" -H 'accept: */*' http://localhost:8080/trip/forget/1 | jq .
```

