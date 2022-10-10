package study.datajpa.entity;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import study.datajpa.repository.MemberRepository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.LOCAL_DATE_TIME;
import static org.assertj.core.api.InstanceOfAssertFactories.LOCAL_TIME;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class MemberTest {

    @PersistenceContext
    private EntityManager em;
    @Autowired
    private MemberRepository memberRepository;

    @Test
    @Rollback(value = false)
    void JpaEventBaseEntity() throws InterruptedException {
        //given
        Member member = new Member("member1");

        Member member1 = memberRepository.save(member); // @PrePersist

        Thread.sleep(3000); // create시간과 update시간 사이에 텀을 줘서 테스트하기 위함
        member.setUserName("member2");

        //when
        Member findMember = memberRepository.findById(member.getId()).get();
        //then
        System.out.println("findMember.getCreatedDate = " + findMember.getCreatedDate());
        System.out.println("findMember.getUpdatedDate = " + findMember.getLastModifiedDate());
        System.out.println("findMember.getCreatedBy = " + findMember.getCreatedBy());
        System.out.println("findMember.getLastModifiedBy = " + findMember.getLastModifiedBy());
    }

}