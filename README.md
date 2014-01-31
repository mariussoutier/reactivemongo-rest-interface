ReactiveMongo REST interface
============================

A generic REST proxy for a MongoDB database using Play and ReactiveMongo.


REST Proxy
----------

If the built-in [MongoDB REST service](http://docs.mongodb.org/ecosystem/tools/http-interfaces/) is sufficient
for your needs but you need CORS support (e.g. access the service directly from a front-end),
enable the `simpleProxy` mode. You should also set `forwardTo` (the port defaults to your MongoDB's port + 1000).

```bash
mongoREST {
  simpleProxy {
    enabled=true
    forwardTo="http://localhost:28017"
  }
  cors {
    enabled=true
    origin="http://www.yourclientsideapp.com"
  }
}
```

Generic REST interface
----------------------

`Under development`

To enable a full REST service supporting insert and update, leave the `simpleProxy` disabled and
set the mongoUri to your MongoDB server(s).

```bash
mongoREST {
  mongoUri="mongodb://localhost:27017,localhost:27018"
  cors {...}
}
```

### Find

To fetch all elements in a collection, you issue a command similar to MongoDB's default REST
interface:

`GET /myDatabase/users`

You can filter the result by adding parameters in the form `query=<key>:<value`.

`GET /myDatabase/users?query=lastName:Smith&query=firstName:John`

This also works with the dot-notation to match sub-documents:

`GET /myDatabase/users?query=name.lastName:Smith&query=name.firstName:John`

If you use ObjectIds, surround the id with `ObjectId()`:

`GET /myDatabase/users?query=bossId:ObjectId(528dcd062f79c2ff911e3434)`

### Find One by Id

To access one entry with a given `_id`, add the id to the route:

`GET /myDatabase/users/1234`

If you use ObjectIds, surround the id with `ObjectId()`:

`GET /myDatabase/users/ObjectId(528dcd062f79c2ff911e3434)`

### Remove

To delete one entry with a given `_id`, use the same style as find one but use the DELETE method:

`DELETE /myDatabase/users/ObjectId(528dcd062f79c2ff911e3434)`

### Insert

`curl -X POST -H "Content-Type: application/json" -d '{"firstName":John}' "http://localhost:9000/myDatabase/users"`

### Update

`curl -X PUT -H "Content-Type: application/json" -d '{"firstName":John}' "http://localhost:9000/myDatabase/users/ObjectId(528dcd062f79c2ff911e3434)"`


Deployment
----------

This is a normal Play app, so run `play dist` and deploy the produced binaries on your server.
