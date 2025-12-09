package org.codelikethewind;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@Path("/control")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CallControlResource {

    @Inject
    CallControlManager callControlManager;

    @ConfigProperty(name = "MAX_ACTIVE_CALLS", defaultValue = "-1")
    int maxActiveCalls;

    @GET
    public Response getStatus() {
        return Response.ok(new StatusResponse(
                callControlManager.getDropRate(),
                callControlManager.isEmitterEnabled(),
                callControlManager.getActiveCallCount(),
                maxActiveCalls
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

    @POST
    @Path("/reset-calls")
    public Response resetActiveCalls() {
        callControlManager.resetActiveCalls();
        return Response.ok("Active calls reset to 0").build();
    }

    public static class StatusResponse {
        public double dropRate;
        public boolean emitterEnabled;
        public int activeCallCount;
        public int maxActiveCalls;

        public StatusResponse(double dropRate, boolean emitterEnabled, int activeCallCount, int maxActiveCalls) {
            this.dropRate = dropRate;
            this.emitterEnabled = emitterEnabled;
            this.activeCallCount = activeCallCount;
            this.maxActiveCalls = maxActiveCalls;
        }
    }
}
