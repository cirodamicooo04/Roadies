package it.roadies.travel_service.data.dao.specification;

import it.roadies.travel_service.data.entity.Travel;
import it.roadies.travel_service.data.entity.TravelDeparture;
import it.roadies.travel_service.data.entity.enumerations.Continent;
import jakarta.persistence.criteria.Join;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;

public class TravelSpecification {
    public static Specification<Travel> hasDestination(String destination) {

        return (root, query, cb) -> {
            if (destination == null) return null;
            return cb.equal(cb.lower(root.get("destination")), destination.toLowerCase().trim());
        };

    }

    public static Specification<Travel> hasContinent(Continent continent){
        return (root,query,cb) -> {
            if (continent == null) return null;
            return cb.equal(root.get("continent"), continent);
        };
    }

    public static Specification<Travel> hasCountry(String country){
        return (root,query,cb) -> {
            if (country == null) return null;
            return cb.equal(cb.lower(root.get("country")), country.toLowerCase().trim());
        };
    }

    public static Specification<Travel> hasPriceRange(BigDecimal minPrice, BigDecimal maxPrice){
        return (root,query,cb) -> {
            if (minPrice == null && maxPrice == null) return null;
            Join<Travel, TravelDeparture> departure = root.join("departures");
            query.distinct(true); //evito duplicati se un viaggio ha più partenze nello stesso range
            if (minPrice == null){ return cb.lessThanOrEqualTo(departure.get("price"), maxPrice); }
            if (maxPrice == null){ return cb.greaterThanOrEqualTo(departure.get("price"), minPrice); }
            return cb.between(departure.get("price"), minPrice, maxPrice);
        };
    }

    public static Specification<Travel> hasDurationRange(Integer minDurationDays, Integer maxDurationDays) {
        return (root, query, cb) -> {
            if (minDurationDays == null && maxDurationDays == null) return null;
            if (minDurationDays == null) {
                return cb.lessThanOrEqualTo(root.get("durationDays"), maxDurationDays);
            }
            if (maxDurationDays == null) {
                return cb.greaterThanOrEqualTo(root.get("durationDays"), minDurationDays);
            }
            return cb.between(root.get("durationDays"), minDurationDays, maxDurationDays);
        };
    }
}
