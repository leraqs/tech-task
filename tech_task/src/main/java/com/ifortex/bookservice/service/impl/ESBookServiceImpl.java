package com.ifortex.bookservice.service.impl;

import com.ifortex.bookservice.dto.SearchCriteria;
import com.ifortex.bookservice.model.Book;
import com.ifortex.bookservice.service.BookService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ESBookServiceImpl implements BookService {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Map<String, Long> getBooks() {

        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Book> criteriaQuery = criteriaBuilder.createQuery(Book.class);
        Root<Book> root = criteriaQuery.from(Book.class);
        criteriaQuery.select(root);

        List<Book> books = entityManager.createQuery(criteriaQuery).getResultList();

        Map<String, Long> genreCount = new HashMap<>();
        for (Book book : books) {
            for (String genre : book.getGenres()) {
                genreCount.put(genre, genreCount.getOrDefault(genre, 0L) + 1);
            }
        }

        return genreCount.entrySet()
                .stream()
                .sorted((entry1, entry2) -> Long.compare(entry2.getValue(), entry1.getValue()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                        (e1, e2) -> e1, LinkedHashMap::new));
    }

    @Override
    public List<Book> getAllByCriteria(SearchCriteria searchCriteria) {

        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Book> query = criteriaBuilder.createQuery(Book.class);
        Root<Book> root = query.from(Book.class);

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

        return entityManager.createQuery(query).getResultList();
    }
}
