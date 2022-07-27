# GraphQL authentication issue reproducer
This is a simple Quarkus GraphQL project that uses the the https://quarkus.io/guides/microprofile-graphql as a base to reproduce an issue where `AuthenticationRequest` is not always propagated correctly through the GraphQL API.

The quarkus GraphQL example has been modified by securing one of the API methods (`allFilms`), and leveraging `quarkus-elytron-security-properties-file` to setup a set of user credentials, while leaving the rest methods unsecured.

To reproduce the issue one should run the included [JMeter Test Plan](graphql-auth-test.jmx) that uses 50 threads and 1000 iterations to execute 2 GraphQL queries, 1 on an unsecured resource and the other on the secured one. The test uses a Json Assertion to verify  that no error codes are returned by the API (`SecurityException` error codes are configured to be returned to the response data).

With the above setup, the test of **2.10.4.Final** version returns an `unauthorized` error code for ~0.07% of the requests to the secured resource on an 8 core / 16 thread machine that can sustain more than 8000 reqs/s. Testing **2.9.2.Final** produces 0 errors (no matter how many loops). 

In addition, with **2.10.4.Final** if one disables the unsecured query requests (i.e. hit only the secured one) then some (fewer) requests **still fail**, which is not the case with **2.10.3.Final**.

Finally, when testing 2.10.4.Final vs 2.10.3.Final with both queries, 2.10.4.Final consistently returns a higher percentage of errors than 2.10.3.Final. 