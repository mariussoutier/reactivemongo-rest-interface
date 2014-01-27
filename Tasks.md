# Tasks

[X] Develop proxy mode - simply forward all requests with path and method to the configured service URL.
[X] Make the mode configurable via Controller dependency injection
[] Develop generic REST interface using ReactiveMongo. URL scheme is similar to MongoDB's REST interface, but a little more HTTP-like i.e. `/databaseName/collectionName?filter=a:b&filter=c:d`
 [] Implement the GET verb to query for documents
 [] Implement the DELETE verb to remove documents
 [] Implement the POST verb to insert documents
 [] Implement the PUT verb to update documents
[] Write performance tests using Gatling for both operational modes
[] Extend the dist task so a downloadable service can be provided and the user doesn't have to run play dist himself
