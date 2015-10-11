package org.craftsmenlabs.gareth.rest.resource;

import org.craftsmenlabs.gareth.api.ExperimentEngine;
import org.craftsmenlabs.gareth.api.context.ExperimentContext;
import org.craftsmenlabs.gareth.api.exception.GarethUnknownExperimentException;
import org.craftsmenlabs.gareth.rest.v1.assembler.ExperimentAssembler;
import org.craftsmenlabs.gareth.rest.v1.entity.Experiment;

import javax.inject.Inject;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

/**
 * Created by hylke on 09/10/15.
 */
@Path("/experiments-rerun")
public class ExperimentRerunResource {

    @Inject
    private ExperimentEngine experimentEngine;

    @Path("{hash}")
    public Response rerunExperiment(final @PathParam("hash") String hash) {
        Response response = null;
        try {
            final ExperimentContext experimentContext = experimentEngine.findExperimentContextForHash(hash);
            experimentEngine.planExperimentContext(experimentContext);
            response = Response.accepted().build();
        } catch (final GarethUnknownExperimentException e) {
            response = Response.status(Response.Status.NOT_FOUND).build();
        }
        return response;
    }
}