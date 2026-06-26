package it.roadies.travel_service.data.dao.specification;

import it.roadies.travel_service.data.entity.Activity;
import it.roadies.travel_service.data.entity.ActivityDeparture;
import it.roadies.travel_service.data.entity.Travel;
import it.roadies.travel_service.data.entity.enumerations.Continent;
import jakarta.persistence.criteria.Join;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;

public class ActivitySpecification {
    public static Specification<Activity> hasDestination(String destination) {
        return (root, query, cb) -> {
            if (destination == null) return null;
            return cb.equal(cb.lower(root.get("destination")), destination.toLowerCase().trim());
        };
    }

    public static Specification<Activity> hasContinent(Continent continent){
        return (root, query, cb) -> {
            if (continent == null) return null;
            return cb.equal(root.get("continent"), continent);
        };
    }

    public static Specification<Activity> hasCountry(String country){
        return (root,query,cb) -> {
            if (country == null) return null;
            return cb.equal(cb.lower(root.get("country")), country.toLowerCase().trim());
        };
    }

    public static Specification<Activity> hasPriceRange (BigDecimal minPrice, BigDecimal maxPrice){
        return (root, query, cb) -> {
            if (minPrice == null && maxPrice == null) return null;
            Join<Activity, ActivityDeparture> departure = root.join("departures");
            query.distinct(true);
            if (minPrice == null){ return cb.lessThanOrEqualTo(departure.get("price"), maxPrice); }
            if (maxPrice == null){ return cb.greaterThanOrEqualTo(departure.get("price"), minPrice); }
            return cb.between(departure.get("price"), minPrice, maxPrice);
        };
    }

    public static Specification<Activity> isStandalone(){
        return (root, query, cb) -> cb.isNull(root.get("travel"));
    }
}
