package scoring.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import scoring.domain.Restaurant;

import java.time.Instant;

@Repository
public interface RestaurantRepository extends MongoRepository<Restaurant, String> {
    Page<Restaurant> findByNameLikeOrderByName(String nameKeyword, Pageable page);

    @Query("{ 'lastUpdated' : { $lt: ?0 } }")
    Page<Restaurant> findByLastUpdatedBefore(Instant startDate, Pageable page);
}
