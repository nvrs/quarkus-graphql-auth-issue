package org.acme.microprofile.graphql;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.response.Response;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static io.restassured.RestAssured.given;

@QuarkusTest
public class ConcurrentAuthTest {

    private int iterations = 3000;

    @Inject
    UserUtil userUtil;

    @Test
    public void concurrentAllFilmsOnly() throws InterruptedException, ExecutionException {
        ExecutorService executor = Executors.newFixedThreadPool(50);

        var futures = new ArrayList<CompletableFuture<Boolean>>(iterations);
        for (int i = 0; i < iterations; i++) {
            futures.add(CompletableFuture.supplyAsync(this::allFilmsRequestWithAuth, executor)
                    .thenApply(r -> !r.getBody().asString().contains("unauthorized"))
            );
        }
        Optional<Boolean> success = getTestResult(futures);
        Assertions.assertTrue(success.orElse(false), "Unauthorized response codes were found");
        executor.shutdown();
    }

    @Test
    public void concurrentAllFilmsAndFilmById() throws InterruptedException, ExecutionException {
        ExecutorService executor = Executors.newFixedThreadPool(50);

        var futures = new ArrayList<CompletableFuture<Boolean>>(iterations);
        for (int i = 0; i < iterations; i++) {
            futures.add(CompletableFuture.supplyAsync(this::allFilmsRequestWithAuth, executor)
                    .thenApply(r -> !r.getBody().asString().contains("unauthorized"))
            );
            futures.add(CompletableFuture.supplyAsync(this::getFilmRequestNoAuth, executor)
                    .thenApply(r -> !r.getBody().asString().contains("unauthorized"))
            );
        }
        Optional<Boolean> success = getTestResult(futures);
        Assertions.assertTrue(success.orElse(false), "Unauthorized response codes were found");
        executor.shutdown();
    }

    private static Optional<Boolean> getTestResult(ArrayList<CompletableFuture<Boolean>> futures) throws InterruptedException, ExecutionException {
        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenApply(v -> futures.stream()
                        .map(CompletableFuture::join)
                        .reduce(Boolean::logicalAnd)).get();
    }

    private Response allFilmsRequestWithAuth() {
        var user = userUtil.getSomeUserEntry();
        String requestBody =
                "{\"query\":" +
                        "\"" +
                        "{" +
                        " allFilmsSecured  {" +
                        " title" +
                        " director" +
                        " releaseDate" +
                        " episodeID" +
                        "}" +
                        "}" +
                        "\"" +
                        "}";

        return given()
                .body(requestBody)
                .auth()
                .preemptive()
                .basic(user.userName, user.password)
                .post("/graphql/");
    }

    private Response getFilmRequestNoAuth() {
        String requestBody =
                "{\"query\":" +
                        "\"" +
                        "{" +
                        "film (filmId: 1)  {" +
                        " title" +
                        " director" +
                        " releaseDate" +
                        " episodeID" +
                        "}" +
                        "}" +
                        "\"" +
                        "}";

        return given()
                .body(requestBody)
                .post("/graphql/");
    }
}
