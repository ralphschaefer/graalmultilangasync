# A Async Tale in GraalVM
## or from JAVA to JS and back again

### get it running:
- install ```podman```
- create the podman image locally with ```build.sh```
- and then execute it with ```run.sh```

### what it does:
- demonstrate the use of async interface in Scala an JS to run threaded JS simultaneously
- uses of GraalVM 20

## how it works:

### ```resources/js/callAsync.js```
- a JS snippet that defines 3 async request, which then will bes aggregated asynchronously.
  And then being sent as a result with callbacks to the scala caller
- ```await``` in this js is possible - and working, because the function is defined asynchronously
- ```async``` isn't required, if a JS-Promise will be returned 

### ```scala/example/Main.scala```
- callAsync.js is loaded from resources
- a new js-context is acquired from the JSContextPool
- callAsync.js is executed
- the callbacks are registered via the callback "then"-method
- in the callback a scala Promise is resolved
- then the result from the promise.future will be awaited and shown

### ```scala/example/JsContextPool.scala```
- according to the documentation, GraalVM requires every JS thread to have its own JS-context
- A pool of 100 contexts is created, which can be reused
- every created context shares the same JS engine
- every created context got a new member binding ```callAsync```, that creates and runs  ```class TestAsync``` 
  and therefor allows async execution of the embedded JS function with defined payload

### ```scala/example/TestAsync.scala```
- ```class TestAsync```
    - implements "Thenabel", so it returns a proper Promis in JS
    - Runs business logik in TestAsync.RunJS
    - propagates results via estAsync.interopThread to JS caller
    - create thread for request
    - add synthetic sleep of 1.5s to execution (for demo purpose)
- ```class TestAsync.RunJS```
    - extracts payload and function from input json
    - calls JS function with payload
    - converts results to string
- ```class TestAsync.InteropThread```
    - extra thread for communication with on graalVM JS context, because JS in GraalVM allows only one thread to communicate with context
    - implemented as an "actor" (kind of) on which you can send elements via a queue to
- Execution Context
    - an execution context is also defined here used by Main.scala

### podman
- ```build.sh```: creates podman image of demo
- ```run.sh```: runs podman demo image