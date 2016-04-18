# ConcurrentWebServer
### Sung, Justin

Implement a HTTP web-server to handle the basic HTTP requests. The server must be concurrent, i.e., able to serve more than one request at the same time. Additionally, for logging purposes, the server should be able to record each client's IP address along with the requested files in a shared data-structure.

A performance evaluation. The evaluation must compare the performance of the concurrent web-server with a version of the web-server that processes requests sequentially. In particular, the evaluation must examine the effect on the speed-up of the concurrent web-server when the size of the served pages varies. The evaluation must also examine the effect of logging on the performance. To make the measurements, you can use httperf.
