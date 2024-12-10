package com.ifortex.bookservice.repository.specification;

import com.ifortex.bookservice.dto.SearchCriteria;
import com.ifortex.bookservice.model.Book;
import com.ifortex.bookservice.model.Member;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

@Component
public class SpecificationBuilderImpl implements SpecificationBuilder {

    @Override
    public Specification<Book> getAllBooksByCriteria(SearchCriteria searchCriteria) {
        return (root, query, criteriaBuilder) -> {

            Predicate predicate = criteriaBuilder.conjunction();

            if (searchCriteria.getTitle() != null && !searchCriteria.getTitle().trim().isEmpty()) {
                predicate = criteriaBuilder.and(predicate,
                        criteriaBuilder.like(root.get("title"), "%" + searchCriteria.getTitle() + "%"));
            }

            if (searchCriteria.getAuthor() != null && !searchCriteria.getAuthor().trim().isEmpty()) {
                predicate = criteriaBuilder.and(predicate,
                        criteriaBuilder.like(root.get("author"), "%" + searchCriteria.getAuthor() + "%"));
            }

            if (searchCriteria.getGenre() != null && !searchCriteria.getGenre().trim().isEmpty()) {
                Join<Book, String> genres = root.join("genre");
                predicate = criteriaBuilder.and(predicate,
                        criteriaBuilder.equal(genres, searchCriteria.getGenre()));
            }

            if (searchCriteria.getDescription() != null && !searchCriteria.getDescription().trim().isEmpty()) {
                predicate = criteriaBuilder.and(predicate,
                        criteriaBuilder.like(root.get("description"), "%" + searchCriteria.getDescription() + "%"));
            }

            if (searchCriteria.getYear() != null && searchCriteria.getYear() > 0) {
                predicate = criteriaBuilder.and(predicate,
                        criteriaBuilder.equal(
                                criteriaBuilder.function(
                                        "date_part",
                                        Integer.class,
                                        criteriaBuilder.literal("year"),
                                        root.get("publicationDate")),
                                searchCriteria.getYear()
                        ));
            }

            query.where(predicate);
            query.orderBy(criteriaBuilder.desc(root.get("publicationDate")));

            return predicate;
        };
    }

    @Override
    public Specification<Member> registeredInYearAndNoBooks(int year) {
        return (root, query, criteriaBuilder) -> {

            Predicate yearPredicate = criteriaBuilder.equal(
                    criteriaBuilder.function("date_part", Integer.class,
                            criteriaBuilder.literal("year"), root.get("membershipDate")),
                    year);

            Predicate noBooksPredicate = criteriaBuilder.isEmpty(root.get("borrowedBooks"));

            return criteriaBuilder.and(yearPredicate, noBooksPredicate);
        };
    }
}