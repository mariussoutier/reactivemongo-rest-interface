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

Deployment
----------

This is a normal Play app, so run `play dist` and deploy the produced binaries on your server.
