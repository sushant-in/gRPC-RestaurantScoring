# Problem Statement
### Restaurant score service

    Write a backend service which provides CRUD APIs (preferrably in gRPC) for restaurant scoring. Data to be populated to get start can be found at Data SF.

    · The restaurants those which haven't been updated in last 1 year should be tagged as outdated and the status of the restaurant should be updated based on new CRUD API calls.
    
    · The service should also expose REST APIs for UI to consume.
    
    ·  Ensure to make use of database of your choice to store and persist data.

## README

## Implementation

### Assumptions
    1. Restaurant addition /update is typically legal process (via busineess registration), hence it's expected this record is going to be read-heavy.
    2. InspectionReports can be added but not modified/deleted (visible form INSPECTION TYPE containing postfix *_FOLLOWUP) and that makes sense to have a completed trail of ground reports. Any unfinished inspection need to be, followed up and new report of type XXX_FOLLOWUP will get submitted.

### Architectural & Design choice
    1. CRUD APIs are designed keeping aforementioned assumptions, will be implementing using gRPC, as suggested.
    2. Use Springboot and SpringData to implement Data Access Layer, which requires only Inteface definitions for repository implementation.
    2. Requirement: tagging reports having no updated/inspected in a year, need to be tagged as outdated has been implemented as getAll type of API.
        A. Internal field will store LastUpdated along with record, and this gets updated on every update and insert.
        B. Outdated Restaurants can be found using LISTING API (by setting outdatedOnly to True in the RestaurantQuery). 
        C. REST API response APIs can be cached by URI, and this cache can be refreshed every 24 hours, probably during non-business hour.
    3. Service will be stateless to provide seamless scaling.

### Database of choice
    Since it appears that most to the calls to the system would be to create new records and read existing, a document DB such as MongoDB/MongoDB Atlas seems viable choice. We can use mongoDB shards and distributes the read and write workload across different nodes in a shared cluster to have a scale out architecture db-layer and deploying config server and shards as replica sets to provide High Availability.
    Furthermore, we can use in-memory cache to enhance the reads performamce for REST GET API, having appropriate expiration polity as per URI.


### Build process
    Run command: "./gradlew clean build" from the root dir, this will also execute implemented unit tests, against in-memory mongodb
 
### GRPC Client-Server interactions/testing
    1. Update DB configuration in the file: application.properties under "src/resources"
    2. Start server by running class: RestaurantScoringServer
    3. Start Client by running class: RestaurantScoringClient

### Starting Envoy proxy for transcoding gRPC to HTTP/JSON for API consumption bu Rest clients/UI:
    1. Need to have docker running in the localhost
    2. Start server by running class: RestaurantScoringServer
    3. Start Envoy proxy by running shell script: "./start-envoy.sh" form the root dir.

### If you experience any issues, look at the configurations at:
    1. envoy-config.yml
    2. start-envoy.sh
    
    Both available under the root dir.
    
### future work
    1. Comprehensive unit testing and static code analysis intregated with build.
    2. gRPC client implementation can be converted as gRPC Service unit test with small change, wasn't sure if would be preferred.
    3. Configure TLS Security with certs and then we can use OAuth2.0 based authentication/authorization or any other token based on available infra.
    4. Setting up interceptors for error handling and Deadlines/Cancellation.
 
