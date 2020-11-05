package scoring.service;

import com.google.protobuf.Empty;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import scoring.repository.InspectionRepository;
import scoring.repository.RestaurantRepository;
import scoring.util.Transformer;
import scoring.v1.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

/**
 * Service layer implementation for restaurant scoring
 *
 * @author sushantv
 */
@Service
public class RestaurantScoringServiceImpl extends RestaurantScoringServiceGrpc.RestaurantScoringServiceImplBase {
    public static final int PAGE_SIZE = 100;
    protected static final Logger logger = LoggerFactory.getLogger(RestaurantScoringServiceImpl.class);
    @Autowired
    private RestaurantRepository restaurantRepository;
    @Autowired
    private InspectionRepository inspectionRepository;

    @Override
    public void addRestaurant(Restaurant restaurant, StreamObserver<Restaurant> responseObserver) {
        logger.info("createRestaurant() called");
        scoring.domain.Restaurant r = restaurantRepository.insert(Transformer.fromGrpcRestaurant(restaurant));
        responseObserver.onNext(Transformer.toGrpcRestaurant(r));
        responseObserver.onCompleted();
    }

    @Override
    public void deleteRestaurant(scoring.v1.RestaurantId request,
                                 io.grpc.stub.StreamObserver<com.google.protobuf.Empty> responseObserver) {
        logger.info("deleteRestaurant() called");
        restaurantRepository.deleteById(request.getValue());
        responseObserver.onNext(Empty.newBuilder().build());
        responseObserver.onCompleted();
    }

    @Override
    public void getRestaurant(scoring.v1.RestaurantId request,
                              io.grpc.stub.StreamObserver<scoring.v1.Restaurant> responseObserver) {
        logger.info("getRestaurant() called");
        Optional<scoring.domain.Restaurant> optionalRestaurant = restaurantRepository.findById(request.getValue());
        if (optionalRestaurant.isPresent()) {
            responseObserver.onNext(Transformer.toGrpcRestaurant(optionalRestaurant.get()));
            responseObserver.onCompleted();
        } else {
            responseObserver.onError(Status.NOT_FOUND
                    .withDescription("no Restaurant with id: " + request.getValue())
                    .asRuntimeException());
        }
    }

    @Override
    public void listRestaurant(scoring.v1.RestaurantQuery query,
                               io.grpc.stub.StreamObserver<scoring.v1.Restaurant> responseObserver) {
        logger.info("listRestaurants() called with: " + query);
        Page<scoring.domain.Restaurant> restaurants = queryRestaurants(query);
        if (restaurants != null && !restaurants.isEmpty()) {
            restaurants.forEach(r -> responseObserver.onNext(Transformer.toGrpcRestaurant(r))
            );
        }
        responseObserver.onCompleted();
    }

    @Override
    public void updateRestaurant(Restaurant restaurant, StreamObserver<Restaurant> responseObserver) {
        scoring.domain.Restaurant r = restaurantRepository.save(Transformer.fromGrpcRestaurant(restaurant));
        responseObserver.onNext(Transformer.toGrpcRestaurant(r));
        responseObserver.onCompleted();
    }

    @Override
    public void addInspectionReport(Inspection inspection, StreamObserver<Inspection> responseObserver) {
        Optional<scoring.domain.Restaurant> optRes = restaurantRepository.findById(inspection.getRestaurantId());
        if (optRes.isPresent()) {
            scoring.domain.Inspection i = inspectionRepository.save(Transformer.fromGrpcInspection(inspection, optRes.get()));
            responseObserver.onNext(Transformer.toGrpcInspection(i));
            responseObserver.onCompleted();
        } else {
            responseObserver.onError(Status.NOT_FOUND
                    .withDescription("no Restaurant with id: " + inspection.getRestaurantId())
                    .asRuntimeException());
        }
    }

    @Override
    public void getInspectionReport(InspectionId request, StreamObserver<Inspection> responseObserver) {
        Optional<scoring.domain.Inspection> optInspection = inspectionRepository.findById(request.getValue());
        if (optInspection.isPresent()) {
            responseObserver.onNext(Transformer.toGrpcInspection(optInspection.get()));
            responseObserver.onCompleted();
        } else {
            responseObserver.onError(Status.NOT_FOUND
                    .withDescription("no Inspection report with id: " + request.getValue())
                    .asRuntimeException());
        }
    }

    @Override
    public void listInspectionReports(InspectionQuery query, StreamObserver<Inspection> responseObserver) {
        Page<scoring.domain.Inspection> inspections = queryInspections(query);
        if (!inspections.isEmpty()) {
            inspections.forEach(i -> responseObserver.onNext(Transformer.toGrpcInspection(i))
            );
        }
        responseObserver.onCompleted();
    }

    @Override
    public void findInspectionReportsForRestaurant(RestaurantId request, StreamObserver<Inspection> responseObserver) {
        Optional<scoring.domain.Restaurant> optRestaurant = restaurantRepository.findById(request.getValue());
        if (optRestaurant.isPresent()) {
            List<scoring.domain.Inspection> inspections = inspectionRepository.findByRestaurant(optRestaurant.get());
            if (inspections != null && !inspections.isEmpty()) {
                inspections.forEach(i -> responseObserver.onNext(Transformer.toGrpcInspection(i))
                );
            }
            responseObserver.onCompleted();
        } else {
            responseObserver.onError(Status.NOT_FOUND
                    .withDescription("no Restaurant with id: " + request.getValue())
                    .asRuntimeException());
        }
    }


    private Page<scoring.domain.Restaurant> queryRestaurants(RestaurantQuery query) {
        final Pageable page = PageRequest.of(query.getPageNo(), query.getLimit() > 0 ? query.getLimit() : PAGE_SIZE);
        if (query.getOutdatedOnly()) {// Restaurant not inspected for last 1 year
            Instant yrBefore = Instant.now().minus(365, ChronoUnit.DAYS);
            return restaurantRepository.findByLastUpdatedBefore(yrBefore, page);
        } else {
            if (query.getNameKeyword() != null && !query.getNameKeyword().isBlank()) {//keyword search
                return restaurantRepository.findByNameLikeOrderByName(query.getNameKeyword(), page);
            } else {
                return restaurantRepository.findAll(page);
            }
        }
    }


    private Page<scoring.domain.Inspection> queryInspections(InspectionQuery query) {
        final Pageable page = PageRequest.of(query.getPageNo(), query.getLimit() > 0 ? query.getLimit() : PAGE_SIZE);
        return inspectionRepository.findAll(page);
    }
}
