package org.acme;

import io.smallrye.reactive.messaging.kafka.KafkaRecord;
import io.vertx.core.json.JsonObject;
import org.acme.data.Purchase;
import org.eclipse.microprofile.lra.annotation.Compensate;
import org.eclipse.microprofile.lra.annotation.Complete;
import org.eclipse.microprofile.lra.annotation.Forget;
import org.eclipse.microprofile.lra.annotation.ParticipantStatus;
import org.eclipse.microprofile.lra.annotation.ws.rs.LRA;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.ExampleObject;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.jboss.resteasy.annotations.jaxrs.HeaderParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.security.SecureRandom;

import static org.eclipse.microprofile.lra.annotation.ws.rs.LRA.LRA_HTTP_CONTEXT_HEADER;

@Path("payment")
@Produces(MediaType.APPLICATION_JSON)
public class PaymentService {

    private final Logger log = LoggerFactory.getLogger(PaymentService.class);

    @Inject
    @Channel("payments-emit-out")
    Emitter<Purchase> payments;

    @POST
    @LRA(value = LRA.Type.REQUIRED,
            end = false,
            cancelOn = {
                    Response.Status.INTERNAL_SERVER_ERROR // cancel on a 500 code
            },
            cancelOnFamily = {
                    Response.Status.Family.CLIENT_ERROR // cancel on any 4xx code
            })
    @Path("pay")
    @Operation(operationId = "pay", summary = "make a payment", description = "This operation makes a payment.", deprecated = false, hidden = false)
    @Tag(name = "Admin")
    @APIResponse(responseCode = "200", description = "Action successful", content = @Content(schema = @Schema(implementation = Response.class), examples = @ExampleObject(name = "example", value = "{\"message\": \"Payment Made #1.\"}")))
    @APIResponse(responseCode = "400", description = "Unable to process input", content = @Content(schema = @Schema(implementation = Response.class), examples = @ExampleObject(name = "example", value = "{\"message\": \"Cannot process input\"}")))
    @APIResponse(responseCode = "500", description = "Server Error", content = @Content(schema = @Schema(implementation = Response.class), examples = @ExampleObject(name = "example", value = "{\"reason\": \"Server error\"}")))
    public Response pay(@HeaderParam(LRA_HTTP_CONTEXT_HEADER) URI lraId, Purchase purchase) {
        purchase.setItem("PAYMENT# ".concat(purchase.getItem()));
        log.info(">>> Payment received for LRA {} and Purchase {}", lraId, purchase);
//        if (new SecureRandom().nextBoolean())
//            return Response.serverError().build();
        payments.send(KafkaRecord.of(lraId, purchase));
        JsonObject response = new JsonObject().put("message", "Payment Made LRA #" + lraId);
        response.put("purchase", JsonObject.mapFrom(purchase));
        return Response.ok(response).build();
    }

    @PUT
    @Path("/end")
    @LRA(value = LRA.Type.MANDATORY)
    public Response end(@HeaderParam(LRA_HTTP_CONTEXT_HEADER) URI lraId) {
        log.info("LRA end {}", lraId);
        return Response.ok(lraId.toASCIIString()).build();
    }

    @PUT
    @LRA
    @Path("compensate")
    @Compensate
    public Response compensate(@HeaderParam(LRA_HTTP_CONTEXT_HEADER) URI lraId) {
        log.info(">>> Compensate LRA {}", lraId);
        try {
            Purchase purchase = new Purchase(lraId.toASCIIString(), "CANCELLED PAYMENT");
            payments.send(KafkaRecord.of(purchase.getPurchaseKey(), purchase));
        } catch (Exception ex) {
            JsonObject response = new JsonObject().put("message", "Caught Exception: " + ex);
            return Response.serverError().entity(response).build();
        }
        return Response.ok(ParticipantStatus.Compensated.name()).build();
    }

    @PUT
    @Path("/complete")
    @Complete
    public Response complete(@HeaderParam(LRA_HTTP_CONTEXT_HEADER) URI lraId) {
        log.info("LRA completing {}", lraId);
        return Response.ok(lraId.toASCIIString()).build();
    }

    @Forget
    @Path("/forget")
    @DELETE
    public Response forget(@HeaderParam(LRA_HTTP_CONTEXT_HEADER) URI lraId) {
        log.info(">>> Forget LRA {}", lraId);
        return Response.ok().build();
    }
}
