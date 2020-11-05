package scoring.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import scoring.domain.Inspection;
import scoring.domain.Restaurant;

import java.time.Instant;
import java.util.List;

@Repository
public interface InspectionRepository extends MongoRepository<Inspection, String> {
    Page<Inspection> findByTypeOrderByDate(String nameKeyword, Pageable page);

    List<Inspection> findByRestaurant(Restaurant restaurant);

    List<Inspection> findByDateBetween(Instant startDate, Instant endDate);
}
