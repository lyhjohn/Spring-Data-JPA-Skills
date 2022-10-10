package study.datajpa.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import study.datajpa.entity.Member;

import java.util.List;

public interface MemberRepositoryCustom {

    List<Member> findMemberCustom();
}
