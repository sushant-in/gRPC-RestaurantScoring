package scoring;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.env.Environment;
import org.springframework.data.mongodb.core.MongoTemplate;
import scoring.service.RestaurantScoringServiceImpl;

import java.io.IOException;

/**
 * Entry point with main
 *
 * @author sushantv
 */
@SpringBootApplication
@ComponentScan({"scoring.service", "scoring.repository"})
public class RestaurantScoringServer {
    protected static final Logger logger = LoggerFactory.getLogger(RestaurantScoringServer.class);
    private static RestaurantScoringServiceImpl scoringService;
    private final Environment environment;
    private final MongoTemplate mongoTemplate;

    public RestaurantScoringServer(Environment environment, MongoTemplate mongoTemplate, RestaurantScoringServiceImpl scoringService) {
        this.environment = environment;
        this.mongoTemplate = mongoTemplate;
        RestaurantScoringServer.scoringService = scoringService;
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        SpringApplication.run(RestaurantScoringServer.class, args);


        Server service = ServerBuilder.forPort(53000)
                .addService(scoringService)
                .build()
                .start();

        Runtime.getRuntime().addShutdownHook(new Thread(service::shutdownNow));
        logger.info("Started listening for rpc calls on 53000...");
        service.awaitTermination();
    }
}
