# FEDEX assignment
## How to run application in Docker with API dependency
```bash
mvn clean install && docker-compose up
```
API can be access by the url: http://localhost:4001/swagger-ui.html

## How to run API service for tests
```bash
docker run -p 4000:8080 -d xyzassessment/backend-services
```

## Design decisions

API configuration was created for each API.  

For each separate domain concept (track, shipment, pricing) I created a separate service. This would allow process data in a different ways if needed.

### AS-1
`AsyncApiDataFetcher` was created for AS-1. It is just common case with no complication. 
As a coincidence all API responses have very similar structure, so we can use generic fetcher.

###  AS-2
For AS-2 we need to decorate `AsyncApiDataFetcher` with the queue.
For that `QueueDecoratedAsyncApiDataFetcher` was created. For each API we need a separate queue that's why it was marked as "prototype".

I made a simple Spring Scheduler which checks the queue and publishes updates when needed. Also queue could be easily replaced by some external queue.

### AS-3
For AS-3 we need to flush remaining items from the queue by the timer. I used the same scheduler from AS-2 to check it.

## Information
The requirement was to implement solution in a reactive way during 8 hours. The solution implemented using Spring WebFlux.
Unfortunately I don't have enough experience to put this technology in my CV. That's why my solution could be not optimal. 
