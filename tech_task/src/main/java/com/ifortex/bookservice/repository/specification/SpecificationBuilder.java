package com.ifortex.bookservice.repository.specification;

import com.ifortex.bookservice.dto.SearchCriteria;
import com.ifortex.bookservice.model.Book;
import org.springframework.data.jpa.domain.Specification;

public interface SpecificationBuilder {

    Specification<Book> getAllBooksByCriteria(SearchCriteria searchCriteria);

}
