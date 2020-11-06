package scoring;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.data.domain.Page;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import scoring.domain.Inspection;
import scoring.domain.Restaurant;
import scoring.domain.Violation;
import scoring.repository.InspectionRepository;
import scoring.repository.RestaurantRepository;
import scoring.v1.InspectionType;
import scoring.v1.RiskCategory;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@DataMongoTest
@ExtendWith(SpringExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class RestaurantScoringRepositoryTests {
    List<Restaurant> dataRestaurants;
    @Autowired
    private RestaurantRepository restaurantRepository;
    @Autowired
    private InspectionRepository inspectionRepository;
    private List<Restaurant> dbRestaurants;
    private Inspection dataInspection;

    @BeforeAll
    public void beforeAll() {
        String id = UUID.randomUUID().toString();
        Restaurant r1 = new Restaurant(null,
                "BREADBELLY",
                "1408 Clement St",
                "San Francisco",
                "CA",
                "94118",
                "14157240859");

        Restaurant r2 = new Restaurant(null,
                "Great Gold Restaurant",
                "3161 24th St.",
                "San Francisco",
                "CA",
                "94110",
                null);

        Restaurant r3 = new Restaurant(null,
                "Pronto Pizza",
                "798 Eddy St",
                "San Francisco",
                "CA",
                "94109",
                null);

        dataRestaurants = List.of(r1, r2, r3);
        dbRestaurants = this.restaurantRepository.saveAll(dataRestaurants);


        List<Violation> violations = List.of(
                new Violation("97975_20190725_103124", "Inadequately cleaned or sanitized food contact surfaces", RiskCategory.MODERATE_RISK),
                new Violation("85986_20161011_103114", "High risk vermin infestation", RiskCategory.MODERATE_RISK));

        dataInspection = new Inspection("97975_20190725", dbRestaurants.get(0), InspectionType.ROUTINE_SCHEDULED, 96, violations, Instant.now());
    }

    @Test
    @Order(1)
    public void findRestaurantById() {
        Optional<Restaurant> restaurant = restaurantRepository.findById(dbRestaurants.get(0).getId());

        assertTrue(restaurant.isPresent());
        assertEquals(dbRestaurants.get(0).getName(), restaurant.get().getName());
    }

    @Test
    @Order(2)
    public void findAllRestaurants() {
        List<Restaurant> restaurants = restaurantRepository.findAll();
        assertEquals(dbRestaurants.size(),restaurants.size());
    }

    @Test
    @Order(3)
    public void findOutdatedRestaurants() {
        Page<Restaurant> restaurants = restaurantRepository.findByLastUpdatedBefore(Instant.now(), null);
        assertEquals(restaurants.getTotalElements(), dbRestaurants.size());// should be same size for in-mem db
        restaurants = restaurantRepository.findByLastUpdatedBefore(Instant.now().minus(365, ChronoUnit.DAYS), null);
        assertEquals(restaurants.getTotalElements(), 0);// should be 0 size for in-mem db
    }

    @Test
    @Order(4)
    public void insertAndfindInspectionById() {
        Inspection dbInspection = inspectionRepository.insert(dataInspection);
        Optional<Inspection> ins = inspectionRepository.findById(dbInspection.getId());
        assertTrue(ins.isPresent());
        assertEquals(dbInspection.getRestaurant(), ins.get().getRestaurant());
    }

    @Test
    @Order(4)
    public void cascadeOnSaveTest() {

        Restaurant rs1 = new Restaurant(
                "80137",
                "Los Trinos Restaurant",
                "5672 Mission St",
                "San Francisco",
                "CA",
                "94112",
                "+14155575645");

        List<Violation> violations = List.of(
                new Violation("80137_20161005_103149", "Wiping cloths not clean or properly stored or inadequate sanitizer.", RiskCategory.LOW_RISK),
                new Violation("80137_20161005_103144", "Unapproved or unmaintained equipment or utensils.", RiskCategory.LOW_RISK));

        Inspection ins = new Inspection("80137_20161005", rs1, InspectionType.ROUTINE_UNSCHEDULED, 92, violations, Instant.now());

        inspectionRepository.insert(ins);

        Optional<Inspection> dbInspection = inspectionRepository.findById("80137_20161005");
        assertTrue(dbInspection.isPresent());


        Optional<Restaurant> dbRestaurant = restaurantRepository.findById("80137");
        assertTrue("Restaurant cascade save has failed!",dbRestaurant.isPresent());
        assertEquals(rs1,dbRestaurant.get());
    }
}
