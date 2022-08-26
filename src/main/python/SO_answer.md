Disclaimer: Measuring performance of a program by launching a main a few times gives very approximate results. But, I admit, it is far easier than to setup a real benchmark ;-).

First and foremost, let's avoid polluting a public web-server. Let's launch a local web-server. It will both:
 * Avoid spamming distant services
 * Remove network inconsistencies by restraining our measures on local host.

That said, in my opinion, despite the fact that there are many ways to improve the java code.

First, the test-case contains two factors that are not very good fit for java language:
* Short running script: JVM is known for its slow startup, and indeed, it make it far slower than a lot of competitors for short-lived applications (although, I am not sure if it is at disadvantage against Python).
* Code with few CPU task and a lot of IO/latency bound tasks.JVM threading model is (at least for now) very bad in this regard.

In the specific case of network IO, and particularly for HTTP requests, It is possible to improve performance by using either:
* standard java.net HttpClient (available since java 11)
* third-party libraries (Netty, Spring WebFlux, ktor, etc.).

Now, I've tried to rework java test with two different HTTP clients:

* Spring WebFlux + Netty engine
* Java.net Http client

For the few tests I've run against a local hello server, I must admit that I am very disappointed with Java -_-.

TODO: add more details and explanations
 

