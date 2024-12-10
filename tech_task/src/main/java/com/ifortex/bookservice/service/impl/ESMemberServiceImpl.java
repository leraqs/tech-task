package com.ifortex.bookservice.service.impl;

import com.ifortex.bookservice.model.Book;
import com.ifortex.bookservice.model.Member;
import com.ifortex.bookservice.repository.MemberRepository;
import com.ifortex.bookservice.repository.specification.SpecificationBuilder;
import com.ifortex.bookservice.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ESMemberServiceImpl implements MemberService {

    private static final int REGISTRATION_YEAR = 2023;
    private static final String GENRE = "Romance";
    private final MemberRepository memberRepository;
    private final SpecificationBuilder specificationBuilder;

    @Override
    public Member findMember() {

        List<Member> members = memberRepository.findAll();

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

        Specification<Member> spec = specificationBuilder.registeredInYearAndNoBooks(REGISTRATION_YEAR);

        return memberRepository.findAll(spec);
    }
}
