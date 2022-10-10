package study.datajpa.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.EntityGraph;
import study.datajpa.entity.Member;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

@RequiredArgsConstructor
public class MemberRepositoryImpl implements MemberRepositoryCustom{

//    @PersistenceContext / @RequiredArgsConstructor 사용해서 생략 가능함
    private final EntityManager em;

    @Override
    public List<Member> findMemberCustom() {

        return em.createQuery("select m from Member m")
                .getResultList();
    }


}
