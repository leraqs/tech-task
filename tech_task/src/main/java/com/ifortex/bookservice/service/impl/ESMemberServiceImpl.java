package com.ifortex.bookservice.service.impl;

import com.ifortex.bookservice.model.Book;
import com.ifortex.bookservice.model.Member;
import com.ifortex.bookservice.service.MemberService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
public class ESMemberServiceImpl implements MemberService {

    private static final int REGISTRATION_YEAR = 2023;
    private static final String GENRE = "Romance";

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Member findMember() {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Member> criteriaQuery = criteriaBuilder.createQuery(Member.class);
        Root<Member> root = criteriaQuery.from(Member.class);
        criteriaQuery.select(root);

        List<Member> members = entityManager.createQuery(criteriaQuery).getResultList();

        return members.stream()
                .filter(member -> member.getBorrowedBooks().stream()
                        .anyMatch(book -> book.getGenres().contains(GENRE)))
                .max(Comparator.comparing(member ->
                        member.getBorrowedBooks().stream()
                                .filter(book -> book.getGenres().contains(GENRE))
                                .min(Comparator.comparing(Book::getPublicationDate))
                                .get()
                                .getPublicationDate()))
                .orElseThrow(() -> new RuntimeException("No member found"));
    }

    @Override
    public List<Member> findMembers() {

        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Member> criteriaQuery = criteriaBuilder.createQuery(Member.class);
        Root<Member> root = criteriaQuery.from(Member.class);

        Predicate yearPredicate = criteriaBuilder.equal(
                criteriaBuilder.function("date_part", Integer.class,
                        criteriaBuilder.literal("year"), root.get("membershipDate")),
                REGISTRATION_YEAR);

        Predicate noBooksPredicate = criteriaBuilder.isEmpty(root.get("borrowedBooks"));

        criteriaQuery.where(criteriaBuilder.and(yearPredicate, noBooksPredicate));

        return entityManager.createQuery(criteriaQuery).getResultList();
    }
}
