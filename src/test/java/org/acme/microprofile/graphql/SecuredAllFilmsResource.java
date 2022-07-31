package org.acme.microprofile.graphql;

import io.quarkus.security.Authenticated;
import org.eclipse.microprofile.graphql.Description;
import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Query;

import javax.inject.Inject;
import java.util.List;

@GraphQLApi
public class SecuredAllFilmsResource {

    @Inject
    GalaxyService service;

    @Query("allFilmsSecured")
    @Description("Get all Films from a galaxy far far away")
    @Authenticated
    public List<Film> getAllFilmsSecured() {
        return service.getAllFilms();
    }
}
