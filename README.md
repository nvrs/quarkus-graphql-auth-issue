
# GraphQL authentication issue reproducer

## Describe the bug
After upgrading from **2.9.2.Final**, requests to the GraphQL API for secured resources (queries / mutations) may spuriously fail with an "unauthorized" error-code. 

I have created a [simple Quarkus GraphQL project](https://github.com/nvrs/quarkus-graphql-auth-issue) that uses the the https://quarkus.io/guides/microprofile-graphql as a base to exhibit / reproduce the issue. Specifically, the quarkus GraphQL example has been modified by adding an extra method to the API (`allFilmsSecured`) that's secured using the standard `@Authenticated` annotation, and leveraged the `quarkus-elytron-security-properties-file` extension to configure a set of valid user credentials.

## Expected behavior
When querying the secured endpoint with the correct credentials (HTTP Basic Auth) concurrenctly with / without other unsecured endpoints, one should not receive `unthorized` error-code reponses.

## Actual behavior
Given enough concurrent requests (tests are configured to 50 but failures occur with fewer too), some will fail with `unthorized` error-code. What's also suprising is that the unexpected behaviour is not exactly the same for all versions > 2.9.2.Final. Specifically, for versions up to **2.10.3.Final** the bug seems to occur only when there are concurrent requests with different security credentials and / or a combination of resources with different security restrictions. However on **2.10.4.Final** and **2.11.1.Final** failures are observed even when loading the service with concurrent requests for the same resource with the exact same credentials.

## How to Reproduce?

The Junit tests in `ConcurrentAuthTest` always reproduces the issue on two machines that I have tried it out (8 cores / 16 threads - Ryzen 58000x, and 4 cores / 8 threads intel i7 kaby lake). On the test class there are two tests, one that attempts concurrent requests on both secured and unsecured resources (that fails on all versions > **2.9.2.Final**), and another that only performs concurrent requests on the secured resouce that fails only on **2.10.4.Final** and **2.11.1.Final**.

A [JMeter Test Plan](graphql-auth-test.jmx) is also included that reproduces the issue very reliably (JSON Assertions fail when receiving `unauthorized` error codes).