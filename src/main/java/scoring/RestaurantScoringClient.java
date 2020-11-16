package scoring;

import com.google.protobuf.Empty;
import com.google.protobuf.Timestamp;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scoring.v1.*;

import java.util.Iterator;

/**
 * Client implementation for RestaurantScoringGrpc server
 *
 * @author sushantv
 */
public class RestaurantScoringClient {
    protected static final Logger logger = LoggerFactory.getLogger(RestaurantScoringClient.class);

    public static void main(String[] args) {
        ManagedChannel channel = ManagedChannelBuilder
                .forAddress("localhost", 8082)
                .usePlaintext() // disable TLS which is enabled by default and requires certificates
                .build();

        logger.info("## Find a Restaurant that doesn't exist...");
        RestaurantScoringServiceGrpc.RestaurantScoringServiceBlockingStub svcClient = RestaurantScoringServiceGrpc.newBlockingStub(channel);
        try {
            Restaurant r = svcClient.getRestaurant(RestaurantId.newBuilder()
                    .setValue("123")
                    .build());
            logger.info("response: " + r);
        } catch (StatusRuntimeException e) {
            logger.error(e.getStatus().getDescription() + " " + e.getMessage());
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
        }

        String createdRestaurantId = createRestaurant(svcClient);

        logger.info("## Getting Restaurant with id " + createdRestaurantId);
        Restaurant existingRestaurant = svcClient.getRestaurant(RestaurantId.newBuilder()
                .setValue(createdRestaurantId)
                .build());
        logger.info("response: " + existingRestaurant);


        deleteRestaurant(svcClient, createdRestaurantId);
        try {
            logger.info("## Getting Restaurant with id, post deletion " + createdRestaurantId);
            Restaurant existingRestaurant2 = svcClient.getRestaurant(RestaurantId.newBuilder()
                    .setValue(createdRestaurantId)
                    .build());
            logger.info("response: " + existingRestaurant2);
        } catch (StatusRuntimeException e) {
            logger.error(e.getStatus().getDescription() + " " + e.getMessage());
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
        }
        createdRestaurantId = createRestaurant(svcClient);

        createInspectionReport(svcClient, createdRestaurantId);

        listRestaurant(svcClient);

        deleteRestaurant(svcClient, createdRestaurantId);


    }

    private static void listRestaurant(RestaurantScoringServiceGrpc.RestaurantScoringServiceBlockingStub svcClient) {
        logger.info("Listing Restaurants");
        Iterator<Restaurant> response = svcClient.listRestaurant(RestaurantQuery.newBuilder()
                .setOutdatedOnly(false)
                .setLimit(10)
                .setPageNo(0)
                .build());
        logger.info("Response: " + response);
    }

    private static void deleteRestaurant(RestaurantScoringServiceGrpc.RestaurantScoringServiceBlockingStub svcClient, String restaurantId) {
        logger.info("Deleting Restaurant with id " + restaurantId);
        Empty empty = svcClient.deleteRestaurant(RestaurantId.newBuilder()
                .setValue(restaurantId)
                .build());
        logger.info("response: " + empty);
    }

    private static String createRestaurant(RestaurantScoringServiceGrpc.RestaurantScoringServiceBlockingStub svcClient) {
        logger.info("## Creating Restaurant ");
        Restaurant r1 = Restaurant.newBuilder()
                .setId("80137")
                .setName("Los Trinos Restaurant")
                .setAddress("5672 Mission St")
                .setCity("San Francisco")
                .setState("CA")
                .setPostalCode("94112")
                .setPhoneNumber("+14155575645").build();

        Restaurant createdRestaurantResponse = svcClient.addRestaurant(r1);

        String createdRestaurantId = createdRestaurantResponse.getId();
        logger.info("response: " + createdRestaurantResponse);
        return createdRestaurantId;
    }

    private static String createInspectionReport(RestaurantScoringServiceGrpc.RestaurantScoringServiceBlockingStub svcClient, String restaurantId) {
        logger.info("## Creating Inspection ");

        Violation violation1 = Violation.newBuilder()
                .setId("97975_20190725_103124")
                .setDescription("Inadequately cleaned or sanitized food contact surfaces")
                .setRisk(RiskCategory.MODERATE_RISK)
                .build();
        Violation violation2 = Violation.newBuilder()
                .setId("85986_20161011_103114")
                .setDescription("High risk vermin infestation")
                .setRisk(RiskCategory.HIGH_RISK)
                .build();
        Inspection inspection = Inspection.newBuilder()
                .setRestaurantId(restaurantId)
                .setInspectionDate(Timestamp.newBuilder().build())
                .setType(InspectionType.ROUTINE_SCHEDULED)
                .setScore(96)
                .addViolations(violation1)
                .addViolations(violation2)
                .build();

        Inspection response = svcClient.addInspectionReport(inspection);

        String createdInspectionId = response.getId();
        logger.info("Response: " + createdInspectionId);
        return createdInspectionId;
    }
}
