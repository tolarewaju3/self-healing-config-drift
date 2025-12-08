package org.codelikethewind;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/control")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CallControlResource {

    @Inject
    CallControlManager callControlManager;

    @GET
    public Response getStatus() {
        return Response.ok(new StatusResponse(
                callControlManager.getDropRate(),
                callControlManager.isEmitterEnabled()
        )).build();
    }

    @POST
    @Path("/drop-rate")
    public Response updateDropRate(@QueryParam("value") double value) {
        callControlManager.setDropRate(value);
        return Response.ok("Drop rate set to " + value).build();
    }

    @POST
    @Path("/enable")
    public Response enableEmitter() {
        callControlManager.setEmitterEnabled(true);
        return Response.ok("Emitter enabled").build();
    }

    @POST
    @Path("/disable")
    public Response disableEmitter() {
        callControlManager.setEmitterEnabled(false);
        return Response.ok("Emitter disabled").build();
    }

    public static class StatusResponse {
        public double dropRate;
        public boolean emitterEnabled;

        public StatusResponse(double dropRate, boolean emitterEnabled) {
            this.dropRate = dropRate;
            this.emitterEnabled = emitterEnabled;
        }
    }
}
