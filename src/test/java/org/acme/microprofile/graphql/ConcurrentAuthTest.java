package org.acme.microprofile.graphql;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.response.Response;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static io.restassured.RestAssured.given;

@QuarkusTest
public class ConcurrentAuthTest {

    private String userName = "scott";
    @ConfigProperty(name = "quarkus.security.users.embedded.users.scott")
    private String pass;

    @Test
    public void allFilmsOnlyConcurrentAccess() throws InterruptedException, ExecutionException {
        ExecutorService executor = Executors.newFixedThreadPool(50);

        var iterations = 2000;
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
    public void allFilmsAndFilmByIdConcurrentAccess() throws InterruptedException, ExecutionException {
        ExecutorService executor = Executors.newFixedThreadPool(50);

        var iterations = 2000;
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
        String requestBody =
                "{\"query\":" +
                        "\"" +
                        "{" +
                        " allFilms  {" +
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
                .basic(userName, pass)
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
