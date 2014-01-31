# Tasks

- [x] Develop proxy mode - simply forward all requests with path and method to the configured service URL.
- [x] Make the mode configurable via Controller dependency injection
- [ ] Develop generic REST interface using ReactiveMongo. URL scheme is similar to MongoDB's REST interface
 - [x] Implement the GET verb to query for one document by key (`/<db>/<coll>/oid:<id>`)
 - [x] Implement the GET verb to query for documents (`/<db>/<coll>?query=doc.subdoc:value`)
 - [x] Implement the DELETE verb to remove a single document
 - [x] Implement the POST verb to insert a document
 - [x] Implement the PUT verb to update a single document
 - [ ] Implement the PUT verb to update multiple documents
- [ ] Exclude fields via `exclude` parameter
- [ ] Offset and limit document via `offset` and `limit`
- [ ] Write performance tests using Gatling for both operational modes
- [ ] Extend the dist task so a downloadable service can be provided and the user doesn't have to run play dist himself
