package org.swmaestro.repl.gifthub.auth.service;

import org.swmaestro.repl.gifthub.auth.dto.SignUpDto;
import org.swmaestro.repl.gifthub.auth.entity.Member;

import java.util.List;

public interface MemberService {
    Long create(SignUpDto signUpDTO);

    Member read(SignUpDto signUpDto);

    int count();

    List<Member> list();

    Long update(Long id, Member member);

    Long delete(Long id);
}
