package com.ifortex.bookservice.repository;

import com.ifortex.bookservice.model.Book;
import com.ifortex.bookservice.model.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface BookRepository extends JpaRepository<Book, Long> {
}

