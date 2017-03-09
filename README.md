# handler-demo

A demonstration of [system issue#106](https://github.com/danielsz/system/issues/106).

This app contains two routes

- `GET /`
- `POST /webhook`

The first one is wrapped in a middleware that checks for an `X-CSRF-TOKEN`
header. If the request is a POST, and the header is not present, then it returns
a `403 Not Allowed`.

So the setup is like this (pseudocode)

``` clojure
(routes
  (csrf-middleware (GET "/" ,,,))
  (POST "/webhook" ,,,))
```

The `POST /webhook` route sits outside of this middleware. Webhooks have their
own authentication, this route should not care about CSRF tokens.

But even though the middleware does not wrap `POST /webhook`, it still rejects
requests intended for it.

```
$ curl localhost:10101
app OK
$ curl -X POST localhost:10101/webhook
POST requests must include CSRF token header
```

The reason is that `ring.core/routes` only "cascades" to the next route if the
previous route returned `nil`. In this case the middleware kicks in first, and
returns 403, causing `ring.core/routes` to stop routing, and so `POST /webhook`
is never reached.

The solution would be to have the webhook route come first

``` clojure
(routes
  (POST "/webhook" ,,,)
  (csrf-middleware (GET "/" ,,,)))
```

But because of the way `system.components.handler` works, this is impossible.
[Routes with their own middleware are always put first in the stack](https://github.com/danielsz/system/blob/master/src/system/components/handler.clj#L33-L43).
There is now way to influence this ordering

## Running the code

Just do `lein repl`, it will start the system, Jetty will run at port 10101.

```
$ curl localhost:10101
app OK
```


## License

Copyright © 2016 Arne Brasseur

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.

## Chestnut

Created with [Chestnut](http://plexus.github.io/chestnut/) 0.15.0-SNAPSHOT (242699d0).
