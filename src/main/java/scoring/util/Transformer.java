package scoring.util;

import com.google.protobuf.Timestamp;
import com.google.type.LatLng;
import scoring.domain.Inspection;
import scoring.domain.Restaurant;
import scoring.domain.Violation;

import java.time.Instant;
import java.util.ArrayList;

/**
 * Conversion of GRPC objects to SpringData-Mongo complaint annotated Domain object
 * jprotoc can be explored to do this automatically https://github.com/salesforce/grpc-java-contrib
 *
 * @author sushantv
 */
public class Transformer {

    public static scoring.v1.Restaurant toGrpcRestaurant(Restaurant restaurant) {
        scoring.v1.Restaurant.Builder rb = scoring.v1.Restaurant.newBuilder()
                .setId(restaurant.getId())
                .setName(restaurant.getName())
                .setAddress(restaurant.getAddress())
                .setCity(restaurant.getCity())
                .setState(restaurant.getState());

        if (restaurant.getStatus() != null) {
            rb.setStatus(restaurant.getStatus());
        }
        if (restaurant.getPostalCode() != null) {
            rb.setPostalCode(restaurant.getPostalCode());
        }
        if (restaurant.getPhoneNumber() != null) {
            rb.setPhoneNumber(restaurant.getPhoneNumber());
        }
        if (restaurant.getLatitude() != null && restaurant.getLongitude() != null) {
            rb.setLatitude(restaurant.getLatitude());
            rb.setLongitude(restaurant.getLongitude());
            rb.setLocation(LatLng.newBuilder().setLatitude(restaurant.getLatitude()).setLongitude(restaurant.getLongitude()));
        }

        return rb.build();
    }

    public static Restaurant fromGrpcRestaurant(scoring.v1.Restaurant r) {
        Double lat = null, lon = null;
        if (r.hasLocation()) {
            lat = r.getLocation().getLatitude();
            lon = r.getLocation().getLongitude();
        } else if (r.getLatitude() > 0 && r.getLongitude() > 0) {
            lat = r.getLatitude();
            lon = r.getLongitude();
        }
        return new Restaurant(r.getId(), r.getName(), r.getAddress(), r.getCity(), r.getState(), r.getPostalCode(),
                r.getPhoneNumber(), r.getStatus(), lat, lon);
    }

    public static scoring.v1.Inspection toGrpcInspection(Inspection inspection) {
        scoring.v1.Inspection.Builder ib = scoring.v1.Inspection.newBuilder()
                .setId(inspection.getId())
                .setRestaurantId(inspection.getRestaurant().getId())
                .setInspectionDate(Timestamp.newBuilder()
                        .setSeconds(inspection.getDate().getEpochSecond())
                        .setNanos(inspection.getDate().getNano()).build())
                .setScore(inspection.getScore())
                .setType(inspection.getType());
        if (inspection.getViolations() != null && !inspection.getViolations().isEmpty()) {
            inspection.getViolations().forEach(v -> ib.addViolations(toGrpcViolation(v)));
        }
        return ib.build();
    }

    public static Inspection fromGrpcInspection(scoring.v1.Inspection in, Restaurant restaurant) {
        ArrayList<Violation> violations = null;
        if (in.getViolationsList() != null && !in.getViolationsList().isEmpty()) {
            ArrayList<Violation> finalViolations = new ArrayList<>();
            in.getViolationsList().forEach(v ->
                    finalViolations.add(fromGrpcViolation(v))
            );
            violations = finalViolations;
        }
        Instant instant = Instant.ofEpochSecond(in.getInspectionDate().getSeconds(), in.getInspectionDate().getNanos());
        return new Inspection(in.getId(), restaurant, in.getType(), in.getScore(), violations, instant);
    }

    public static scoring.v1.Violation toGrpcViolation(Violation violation) {
        return scoring.v1.Violation.newBuilder()
                .setId(violation.getId())
                .setDescription(violation.getDescription())
                .setRisk(violation.getRisk()).build();
    }

    public static Violation fromGrpcViolation(scoring.v1.Violation v) {
        return new Violation(v.getId(), v.getDescription(), v.getRisk());
    }
}
