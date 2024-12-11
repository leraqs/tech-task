package com.ifortex.bookservice.service.impl;

import com.ifortex.bookservice.dto.SearchCriteria;
import com.ifortex.bookservice.model.Book;
import com.ifortex.bookservice.service.BookService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ESBookServiceImpl implements BookService {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Map<String, Long> getBooks() {
        String sql = """
                    SELECT UNNEST(genre) AS genre, COUNT(*) AS count
                    FROM books
                    GROUP BY genre
                    ORDER BY count DESC
                """;

        List<Object[]> results = entityManager.createNativeQuery(sql).getResultList();

        Map<String, Long> genreCount = new LinkedHashMap<>();
        for (Object[] result : results) {
            String genre = (String) result[0];
            Long count = ((Number) result[1]).longValue();
            genreCount.put(genre, count);
        }

        return genreCount;
    }

    @Override
    public List<Book> getAllByCriteria(SearchCriteria searchCriteria) {

        String baseSql = """
                SELECT *
                FROM books
                WHERE 1=1
                """;

        List<String> conditions = new ArrayList<>();
        Map<String, Object> parameters = new HashMap<>();

        if (searchCriteria.getTitle() != null && !searchCriteria.getTitle().trim().isEmpty()) {
            conditions.add("title ILIKE :title");
            parameters.put("title", "%" + searchCriteria.getTitle() + "%");
        }

        if (searchCriteria.getAuthor() != null && !searchCriteria.getAuthor().trim().isEmpty()) {
            conditions.add("author ILIKE :author");
            parameters.put("author", "%" + searchCriteria.getAuthor() + "%");
        }

        if (searchCriteria.getGenre() != null && !searchCriteria.getGenre().trim().isEmpty()) {
            conditions.add(":genre = ANY (genre)");
            parameters.put("genre", searchCriteria.getGenre());
        }

        if (searchCriteria.getDescription() != null && !searchCriteria.getDescription().trim().isEmpty()) {
            conditions.add("description ILIKE :description");
            parameters.put("description", "%" + searchCriteria.getDescription() + "%");
        }

        if (searchCriteria.getYear() != null && searchCriteria.getYear() > 0) {
            conditions.add("EXTRACT(YEAR FROM publication_date) = :year");
            parameters.put("year", searchCriteria.getYear());
        }

        String sql = baseSql + (conditions.isEmpty() ? "" : " AND " + String.join(" AND ", conditions)) +
                " ORDER BY publication_date DESC";

        Query query = entityManager.createNativeQuery(sql, Book.class);
        parameters.forEach(query::setParameter);

        return query.getResultList();
    }

}

