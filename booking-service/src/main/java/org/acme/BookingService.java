package org.acme;

import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import io.vertx.core.json.JsonObject;
import org.acme.data.Purchase;
import org.eclipse.microprofile.lra.annotation.Compensate;
import org.eclipse.microprofile.lra.annotation.Complete;
import org.eclipse.microprofile.lra.annotation.Forget;
import org.eclipse.microprofile.lra.annotation.ParticipantStatus;
import org.eclipse.microprofile.lra.annotation.ws.rs.LRA;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import static org.eclipse.microprofile.lra.annotation.ws.rs.LRA.LRA_HTTP_CONTEXT_HEADER;

@Path("trip")
@Produces(MediaType.APPLICATION_JSON)
public class BookingService {

    private final Logger log = LoggerFactory.getLogger(BookingService.class);

    @Inject
    @RestClient
    FlightService flightService;

    @Inject
    @RestClient
    HotelService hotelService;

    private static final Map<Integer, String> CUSTOMERS;
    private static final Map<Integer, String> ITEMS;
    private static final Map<Integer, String> CARDS;
    private static final Map<Integer, String> STORE;

    static {
        CUSTOMERS = new HashMap<Integer, String>();
        CUSTOMERS.put(1, "mike");
        CUSTOMERS.put(2, "joe");
        CUSTOMERS.put(3, "sarah");
    }

    static {
        ITEMS = new HashMap<Integer, String>();
        ITEMS.put(1, "hotel Mariott, flight First Class");
        ITEMS.put(2, "hotel St Regis, flight First Class");
        ITEMS.put(3, "hotel Four Seasons, flight Business Class");
        ITEMS.put(4, "hotel InterContinental, flight Economy Class");
    }

    static {
        CARDS = new HashMap<Integer, String>();
        CARDS.put(1, "4111-1111-1111-1111");
        CARDS.put(2, "5105-1051-0510-5100");
    }

    static {
        STORE = new HashMap<Integer, String>();
        STORE.put(1, "BNE");
        STORE.put(2, "SYD");
        STORE.put(3, "MLB");
        STORE.put(4, "ADE");
        STORE.put(5, "PTH");
    }

    @GET
    @Path("book")
    @LRA(value = LRA.Type.REQUIRES_NEW,
            //timeLimit = 1000, timeUnit = ChronoUnit.MILLIS, // timeout
            cancelOn = {
                    Response.Status.INTERNAL_SERVER_ERROR // cancel on a 500 code
            },
            cancelOnFamily = {
                    Response.Status.Family.CLIENT_ERROR // cancel on any 4xx code
            })
    public Response bookTrip(@HeaderParam(LRA_HTTP_CONTEXT_HEADER) URI lraId) {
        log.info(">>> bookTrip #{}", lraId);
        BigDecimal price = new BigDecimal(ThreadLocalRandom.current().nextDouble(1, 10)).setScale(2, RoundingMode.HALF_UP);
        Purchase purchase = new Purchase(
                lraId.toASCIIString(),
                CUSTOMERS.get(ThreadLocalRandom.current().nextInt(1, CUSTOMERS.size() + 1)),
                CARDS.get(ThreadLocalRandom.current().nextInt(1, CARDS.size() + 1)),
                ITEMS.get(ThreadLocalRandom.current().nextInt(1, ITEMS.size() + 1)),
                STORE.get(ThreadLocalRandom.current().nextInt(1, STORE.size() + 1)),
                ThreadLocalRandom.current().nextInt(1, 10),
                price.doubleValue());
        JsonObject response = new JsonObject().put("message", "Trip Booked LRA #" + lraId);
        try {
            Uni<Response> flightResponse = Uni.createFrom().item(flightService.buy(purchase))
                    .runSubscriptionOn(Infrastructure.getDefaultWorkerPool());
            Uni<Response> hotelResponse = Uni.createFrom().item(hotelService.buy(purchase))
                    .runSubscriptionOn(Infrastructure.getDefaultWorkerPool());
            response.put("flight", flightResponse.await().indefinitely().readEntity(JsonObject.class));
            response.put("hotel", hotelResponse.await().indefinitely().readEntity(JsonObject.class));
        } catch (Exception ex) {
            response.put("message", "Caught Exception: " + ex);
            return Response.serverError().entity(response).build();
        }
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
