package study.datajpa.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import study.datajpa.dto.MemberDto;
import study.datajpa.entity.Member;
import study.datajpa.entity.Team;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest
@Transactional
@Rollback(value = false)
class MemberRepositoryTest {

    @Autowired
    MemberRepository memberRepository; // 인터페이스를 주입받음
    @Autowired
    TeamRepository teamRepository;

    @PersistenceContext
    EntityManager em;

    @Test
    void saveMember() {
        //given
        Member member = new Member("memberB");
        //when
        Member savedMember = memberRepository.save(member); // 인터페이스로부터 가져옴

        // 없을수도 있으므로 기본적으로 Optional로 제공함
        Optional<Member> byId = memberRepository.findById(savedMember.getId());
        Member findMember = byId.get(); // get() 으로 Optional에서 Member 꺼냄, 널이면 NoSuchElementException 발생
        //then
        assertThat(findMember.getUserName()).isEqualTo("memberB");
        assertThat(findMember.getId()).isEqualTo(member.getId());
        assertThat(findMember).isEqualTo(member);
    }

    @Test
    void basicCRUD() {
        Member member1 = new Member("member1");
        Member member2 = new Member("member2");
        memberRepository.save(member1);
        memberRepository.save(member2);

        // Member 단건 테스트
        Member findMember1 = memberRepository.findById(member1.getId()).get();
        Member findMember2 = memberRepository.findById(member2.getId()).get();
        assertThat(findMember1).isEqualTo(member1);
        assertThat(findMember2).isEqualTo(member2);

        // Member 리스트 테스트
        List<Member> members = memberRepository.findAll();
        assertThat(members.size()).isEqualTo(2);
        assertThat(members.get(0)).isEqualTo(member1);

        // Member 카운트 테스트
        long count = memberRepository.count();
        assertThat(count).isEqualTo(2);

        // 삭제 테스트
        memberRepository.delete(member1);
        memberRepository.delete(member2);
        Long deleteCount = memberRepository.count();
        assertThat(deleteCount).isEqualTo(0);
    }

    @Test
    void findByUserNameAndAgeGreaterThen() {
        //given
        Member m1 = new Member("AAA", 10);
        Member m2 = new Member("AAA", 20);
        memberRepository.save(m1);
        memberRepository.save(m2);
        //when
        List<Member> members = memberRepository.findByUserNameAndAgeGreaterThan("AAA", 15);
        //then
        assertThat(members.get(0)).isEqualTo(m2);
        assertThat(members.get(0).getUserName()).isEqualTo("AAA");
        assertThat(members.get(0).getAge()).isEqualTo(20);
        assertThat(members.size()).isEqualTo(1);
    }

    @Test
    void findTopMember() {
        List<Member> top3 = memberRepository.findTop3HelloBy();
    }

    @Test
    void namedQuery() {
        //given
        Member m1 = new Member("AAA", 10);
        Member m2 = new Member("AAA", 20);
        memberRepository.save(m1);
        memberRepository.save(m2);
        //when
        List<Member> members = memberRepository.findByUserName("AAA");
        //then
        Member findMember = members.get(0);
        assertThat(findMember).isEqualTo(m1);
    }

    @Test
    void testQuery() {
        //given
        Member m1 = new Member("AAA", 10);
        Member m2 = new Member("AAA", 20);
        memberRepository.save(m1);
        memberRepository.save(m2);
        //when
        List<Member> members = memberRepository.findUser("AAA", 10);
        //then
        assertThat(members.get(0)).isEqualTo(m1);
    }

    @Test
    void findUserNameList() {
        //given
        Member m1 = new Member("AAA", 10);
        Member m2 = new Member("BBB", 20);
        memberRepository.save(m1);
        memberRepository.save(m2);
        List<String> userName = memberRepository.findUsernameList();
        for (String name : userName) {
            System.out.println(name);
        }
        //when
        List<Member> members = memberRepository.findUser("AAA", 10);
        //then
        assertThat(members.get(0)).isEqualTo(m1);
    }

    @Test
    void findMemberDto() {
        //given
        Member m1 = new Member("AAA", 10);
        memberRepository.save(m1);

        Team t1 = new Team("TeamA");
        teamRepository.save(t1);

        m1.setTeam(t1);

        List<MemberDto> memberDto = memberRepository.findMemberDto();
        for (MemberDto dto : memberDto) {
            System.out.println("dto = " + dto);
            System.out.println(dto.getId());
            System.out.println(dto.getName());
        }
    }

    @Test
    void findByNames() {
        //given
        Member m1 = new Member("AAA", 10);
        Member m2 = new Member("BBB", 20);
        memberRepository.save(m1);
        memberRepository.save(m2);
        List<Member> names = memberRepository.findByNames(Arrays.asList("AAA", "BBB"));

        for (Member member : names) {
            System.out.println("memberName = " + member);
        }
    }

    @Test
    void returnType() {
        //given
        Member m1 = new Member("AAA", 10);
        Member m2 = new Member("BBB", 20);
        memberRepository.save(m1);
        memberRepository.save(m2);


        Optional<Member> result = memberRepository.findOptionalByUserName("AAA");
        System.out.println("result = " + result);

    }

    @Test
    void paging() {
        //given
        memberRepository.save(new Member("Mamber1", 10));
        memberRepository.save(new Member("Mamber2", 10));
        memberRepository.save(new Member("Mamber3", 10));
        memberRepository.save(new Member("Mamber4", 10));
        memberRepository.save(new Member("Mamber5", 10));

        int age = 10;

        // paging 패턴 설정
        // 0 페이지부터 3개를 가져와서 한 페이지로 묶음. (0,1,2 / 3,4) -> 총 두 페이지로 나뉨
        // ★페이지 인덱스는 0부터 시작★
        PageRequest pageRequest = PageRequest.of(2, 2, Sort.by(Sort.Direction.DESC, "userName"));// 0부터시작하는 것에 주의

        //when
        //Page, Slice, List 반환 타입에 따라 다름
        // 반환타입이 Page면 count 쿼리가 자동으로 실행되어 TotalCount를 가져옴
        Page<Member> page = memberRepository.findByAge(age, pageRequest);
//        memberRepository.findTop3ByAge(age); 정렬 따위의 조건 없이 단순하게 먼저 저장된 순서로 3건 조회하고 싶을 때 그냥 이런식으로 하면 됨

        // count 쿼리를 안날리므로 TotalCount 조회 불가 (TotalCount 필요 없는 상황에서 최적화)
        // 내부적으로 쿼리 실행시 limit + 1 으로 조회해서 다음페이지 확인 가능하게 해줌 (더보기를 눌러 다음페이지 확인하는 데 쓰임)
        // 인터페이스의 메서드도 반환타입도 Slice 로 해줘야함 (Page가 Slice를 상속받기 때문에 Page를 반환타입으로 메서드 구현해도 에러가 안생기므로 주의!)
        Slice<Member> slicePage = memberRepository.findByAge(age, pageRequest);

        // TotalCount, 다음페이지 조회 없이 단순히 몇 개 단위로 데이터를 가져올 때 쓰임
//        List<Member> listMember = memberRepository.findByAge(age, pageRequest);

        // 가져온 엔티티를 api로 반환할 때에는 Dto로 변환이 필수!
        page.map(m -> new MemberDto(m.getId(), m.getUserName(), null));

        //then
        List<Member> content = page.getContent(); // page에 있는 객체를 리스트로 꺼내옴

        for (Member member : page) {
            System.out.println("memberPage = " + member);
        }

        for (Member member : content) {
            System.out.println("member = " + member);
        }

        System.out.println("totalPages = " + page.getTotalPages());


        assertThat(content.size()).isEqualTo(3);
        assertThat(page.getTotalElements()).isEqualTo(5); // TotalCount 를 가져옴
        assertThat(page.getNumber()).isEqualTo(0); // 페이지 번호를 가져옴
        assertThat(page.getTotalPages()).isEqualTo(2); // 총 페이지 갯수 (0,1,2 / 3,4 -> 총 두 페이지)
        assertThat(page.isFirst()).isTrue();
        assertThat(page.hasNext()).isTrue();
    }

    @Test
    @Rollback(value = false)
    void bulkUpdate() {
        //given
        memberRepository.save(new Member("Member1", 10));
        memberRepository.save(new Member("Member2", 19));
        memberRepository.save(new Member("Member3", 20));
        memberRepository.save(new Member("Member4", 21));
        memberRepository.save(new Member("Member5", 40));
        //when
        // JPA는 setter 등으로 값을 변경하면 변경감지 기능을 통해 자동으로 업데이트가 되는 방식임.
        // 하지만, 일괄적인 업데이트에 있어서는 성능 향상을 위해 벌크 업데이트가 가능함.
        // 레파지토리에 Query로 업데이트 하면 되는데 Modifying이 필수임. (아래 이유 때문)
        int resultCount = memberRepository.bulkAgePlus(20);
//        em.clear();

        List<Member> result = memberRepository.findByUserName("Member5");


        Member member5 = result.get(0);
        System.out.println("member5 = " + member5);
        // 벌크 업데이트를 하면 db 값은 수정이 되나 영속성 컨텍스트에는 기존 값으로 남아있음
        // 그러므로 clear를 통해 영속성 컨텍스트의 데이터를 날려버려야함
        // 트랜잭션이 끝나버리면 상관 없는데, 같은 트랜잭션 안에서 해당 값을 사용한 로직이 있다면
        // db에 있는 값과 영속성 컨텍스트에 있는 값이 달라서 큰일남 (벌크 연산 실행 후 데이터 조회 등)
        // 인터페이스 메소드에 clearAutomatically를 true로 설정하면 생략 가능

        //then
        assertThat(resultCount).isEqualTo(3);
    }

    @Test
    public void findMemberLazy() {
        //given
        //member1 -> teamA
        //member2 -> teamB

        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");
        teamRepository.save(teamA);
        teamRepository.save(teamB);
        Member member1 = new Member("member1", 10, teamA);
        Member member2 = new Member("member2", 10, teamB);
        memberRepository.save(member1);
        memberRepository.save(member2);

        em.flush();
        em.clear();

        //when
        //select Member
//        List<Member> members = memberRepository.findAll();
//        findAll로 가져오면 지연로딩에 따라 프록시객체로 팀을 가져오기 때문에
//        팀 객체에서 값을 꺼내기 위해서는 셀렉트쿼리를 추가로 N번 실행하게됨

        // 페치조인을 사ㅣ용하면 셀렉트쿼리 한번으로 다 가져옴
        // 매번 JPQL로 페치조인 작성하는 불편함을 줄여주는게 엔티티그래프임.
//        List<Member> members = memberRepository.findMemberFetchJoin();

        List<Member> members = memberRepository.findEntityGraphByUserName("member1");

        for (Member member: members) {
            System.out.println("member = " + member.getUserName());
            System.out.println("member.teamClass = " + member.getTeam().getClass());
            System.out.println("member.team = " + member.getTeam().getName());
        }
        //then
    }

    @Test
    void queryHint() {
        //given
        Member member1 = memberRepository.save(new Member("member1", 10));
        em.flush(); // db에 값 반영
        em.clear(); // 1차캐쉬 초기화
        //when
//        Member findMember = memberRepository.findById(member1.getId()).get(); // 영속성컨텍스트에서 값 가져옴
//        findMember.setUserName("member2"); // 변경감지 동작

        // db의 값을 꺼내오면, 업데이트를 대비해서 꺼내온 값과 동기화된 원본 값 둘 다 준비해둠.
        // 하지만, 업데이트가 목적이 아니라 단순 조회를 위해서 꺼내온 경우에 원본값을 메모리에 갖고있을 필요가 없음.
        // 이것을 최적화 하는 방법이 QueryHints

        Member findReadOnlyMember = memberRepository.findReadOnlyByUserName("member1");
        findReadOnlyMember.setUserName("member2");
        em.flush();

        //then
    }

    @Test
    void lock() {
        //given
        Member member1 = memberRepository.save(new Member("member1", 10));
        em.flush(); // db에 값 반영

        //when
        List<Member> result = memberRepository.findLockByUserName("member1");
    }

    @Test
    void callCustom() {
        List<Member> result = memberRepository.findMemberCustom();
    }
}