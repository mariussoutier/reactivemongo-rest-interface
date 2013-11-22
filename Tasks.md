# Tasks

[X] Develop proxy mode - simply forward all requests with path and method to the configured service URL. First use case is MongoDB REST interface.
[] Develop generic REST interface using ReactiveMongo. URL scheme is similar to MongoDB's REST interface, but a little more HTTP-like i.e. `/databaseName/collectionName?filter=a:b&filter=c:d`
[] Make the mode configurable via Controller dependency injection
[] Write performance tests using Gatling for both operational modes
