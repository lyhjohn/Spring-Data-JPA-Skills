package study.datajpa.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import study.datajpa.entity.Member;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class MemberJpaRepositoryTest {

    @Autowired private MemberJpaRepository memberJpaRepository;

    @Test
    void saveMember() {
        //given
        Member member = new Member("memberA");
        //when
        Member saveMember = memberJpaRepository.save(member);
        Member findMember = memberJpaRepository.find(saveMember.getId());
        //then
        assertThat(findMember.getId()).isEqualTo(member.getId());
        assertThat(findMember.getUserName()).isEqualTo(member.getUserName());
        assertThat(findMember).isEqualTo(member); // 같은 트랜잭션 안에서는 같은 인스턴스로 보장됨 (1차캐쉬에서 꺼내온 Member)
    }

    @Test
    void basicCRUD() {
        Member member1 = new Member("member1");
        Member member2 = new Member("member2");
        memberJpaRepository.save(member1);
        memberJpaRepository.save(member2);

        // Member 단건 테스트
        Member findMember1 = memberJpaRepository.findById(member1.getId()).get();
        Member findMember2 = memberJpaRepository.findById(member2.getId()).get();
        assertThat(findMember1).isEqualTo(member1);
        assertThat(findMember2).isEqualTo(member2);

        // Member 리스트 테스트
        List<Member> members = memberJpaRepository.findAll();
        assertThat(members.size()).isEqualTo(2);
        assertThat(members.get(0)).isEqualTo(member1);

        // Member 카운트 테스트
        long count = memberJpaRepository.count();
        assertThat(count).isEqualTo(2);

        // 삭제 테스트
        memberJpaRepository.delete(member1);
        memberJpaRepository.delete(member2);
        Long deleteCount = memberJpaRepository.count();
        assertThat(deleteCount).isEqualTo(0);
    }

    @Test
    void findByUserNameAndAgeGreaterThen() {
        //given
        Member m1 = new Member("AAA", 10);
        Member m2 = new Member("AAA", 20);
        memberJpaRepository.save(m1);
        memberJpaRepository.save(m2);
        //when
        List<Member> members = memberJpaRepository.findByUserNameAndAgeGreaterThen("AAA", 15);
        //then
        assertThat(members.get(0)).isEqualTo(m2);
        assertThat(members.get(0).getUserName()).isEqualTo("AAA");
        assertThat(members.get(0).getAge()).isEqualTo(20);
        assertThat(members.size()).isEqualTo(1);
    }

    @Test
    void paging() {
        //given
        memberJpaRepository.save(new Member("Mamber1", 10));
        memberJpaRepository.save(new Member("Mamber2", 10));
        memberJpaRepository.save(new Member("Mamber3", 10));
        memberJpaRepository.save(new Member("Mamber4", 10));
        memberJpaRepository.save(new Member("Mamber5", 10));

        int age = 10;
        int offset = 1;
        int limit = 3;

        //when
        List<Member> members = memberJpaRepository.findByPage(age, offset, limit);
        long totalCount = memberJpaRepository.totalCount(age);

        //then
        assertThat(members.size()).isEqualTo(3);
        System.out.println("members = " + members);
        assertThat(totalCount).isEqualTo(5);
    }

    @Test
    @Rollback(value = false)
    void bulkUpdate() {
        //given
        memberJpaRepository.save(new Member("Mamber1", 10));
        memberJpaRepository.save(new Member("Mamber2", 19));
        memberJpaRepository.save(new Member("Mamber3", 20));
        memberJpaRepository.save(new Member("Mamber4", 21));
        memberJpaRepository.save(new Member("Mamber5", 40));
        //when
        int resultCount = memberJpaRepository.bulkAgePlus(20);
        //then
        assertThat(resultCount).isEqualTo(3);
    }
}