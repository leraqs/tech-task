package com.ifortex.bookservice.service.impl;

import com.ifortex.bookservice.model.Member;
import com.ifortex.bookservice.service.MemberService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ESMemberServiceImpl implements MemberService {

    private static final int REGISTRATION_YEAR = 2023;
    private static final String GENRE = "Romance";

    @PersistenceContext
    private EntityManager entityManager;

    @SneakyThrows
    @Override
    public Member findMember() {
        String sql = """
                    SELECT m.*
                    FROM members m
                    JOIN member_books mb ON m.id = mb.member_id
                    JOIN books b ON mb.book_id = b.id
                    WHERE EXISTS (
                        SELECT 1
                        FROM books b2
                        JOIN member_books mb2 ON mb2.book_id = b2.id
                        WHERE mb2.member_id = m.id
                        AND :genre = ANY (b2.genre)
                    )
                    ORDER BY (
                        SELECT MIN(b3.publication_date)
                        FROM books b3
                        JOIN member_books mb3 ON mb3.book_id = b3.id
                        WHERE mb3.member_id = m.id
                        AND :genre = ANY (b3.genre)
                    ) ASC
                    LIMIT 1
                """;

        return (Member) entityManager.createNativeQuery(sql, Member.class)
                .setParameter("genre", GENRE)
                .getResultStream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No member found"));
    }

    @Override
    public List<Member> findMembers() {
        String sql = """
                    SELECT m.*
                    FROM members m
                    WHERE EXTRACT(YEAR FROM m.membership_date) = :year
                    AND NOT EXISTS (
                        SELECT 1
                        FROM member_books mb
                        WHERE mb.member_id = m.id
                    )
                """;

        return entityManager.createNativeQuery(sql, Member.class)
                .setParameter("year", REGISTRATION_YEAR)
                .getResultList();
    }
}

