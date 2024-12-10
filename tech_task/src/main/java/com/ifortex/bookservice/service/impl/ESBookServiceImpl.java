package com.ifortex.bookservice.service.impl;

import com.ifortex.bookservice.dto.SearchCriteria;
import com.ifortex.bookservice.model.Book;
import com.ifortex.bookservice.repository.BookRepository;
import com.ifortex.bookservice.repository.specification.SpecificationBuilder;
import com.ifortex.bookservice.service.BookService;
import io.micrometer.common.util.StringUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ESBookServiceImpl implements BookService {

    private final BookRepository bookRepository;
    private final SpecificationBuilder specificationBuilder;

    @Override
    public Map<String, Long> getBooks() {
        List<Book> books = bookRepository.findAll();
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

        Specification<Book> specification = specificationBuilder.getAllBooksByCriteria(searchCriteria);

        return bookRepository.findAll(specification);
    }
}
