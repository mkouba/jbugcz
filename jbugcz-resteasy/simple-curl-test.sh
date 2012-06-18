#!/bin/sh
ORDER_JSON='{"id":"01"}'

# Get token
# curl -v -X POST http://localhost:8080/jbugcz-resteasy/rest/token?password=Foo
TOKEN=$(curl -X POST http://localhost:8080/jbugcz-resteasy/rest/token?password=Foo)

# Create order
# curl -v -H "Content-Type: application/json" -d $ORDER_JSON '{"id":"01"}' -X POST http://localhost:8080/jbugcz-resteasy/rest/order?token=$TOKEN
curl -H "Content-Type: application/json" -d $ORDER_JSON -X POST http://localhost:8080/jbugcz-resteasy/rest/order?token=$TOKEN

# Get order 01
# curl -v http://localhost:8080/jbugcz-resteasy/rest/order/01?token=7bcc9d04-eb46-403c-8e8b-913a6c1f4731 
RESPONSE_DATA=$(curl -H "Accept: application/json" http://localhost:8080/jbugcz-resteasy/rest/order/01?token=$TOKEN)

if [ $RESPONSE_DATA != $ORDER_JSON ]; then
{
    echo "TEST DID NOT PASS!"
    exit 1
} fi

