package study.datajpa.repository;

import org.hibernate.boot.model.source.spi.AttributePath;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import study.datajpa.dto.MemberDto;
import study.datajpa.entity.Member;

import javax.persistence.LockModeType;
import javax.persistence.QueryHint;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long>, MemberRepositoryCustom { // 엔티티 객체, PK타입

    /**
     * 메서드로 쿼리 생성해주는 강력한 기능
     * GreaterThan = 보다 큰 값을 가져오는 쿼리문으로 인식
     * 단순한 변수명 = 같은 값 가져오는 쿼리문으로 인식
     * https://docs.spring.io/spring-data/jpa/docs/current/reference/html/#jpa.query-methods
     * 공식 문서 Query Methods - Query Creation 란에서 확인 가능
     * find...By : 엔티티의 모든 값을 다 가져옴
     */
    List<Member> findByUserNameAndAgeGreaterThan(String userName, int age);

    List<Member> findTop3HelloBy();

    @Query(name = "Member.findByUsername")
        // Member에 선언한 NamedQuery / 생략 가능
        // 메서드 명을 갖고 먼저 Member에서 네임드쿼리를 찾음. 만약, 네임드쿼리가 없다면 메서드이름으로 쿼리를 생성함 (find...By 와 같은)
    List<Member> findByUserName(@Param("userName") String userName);

    // 이름이 없는 네임드쿼리. 문법 오류가 있다면 어플 동작 시점에 파싱해서 에러 발생
    @Query("select m from Member m where m.userName = :userName and m.age = :age")
    List<Member> findUser(@Param("userName") String userName, @Param("age") int age);

    @Query("select m.userName from Member m")
    List<String> findUsernameList();

    /**
     * Dto로 반환하기
     */
    @Query("select new study.datajpa.dto.MemberDto(m.id, m.userName, t.name) from Member m join m.team t")
    List<MemberDto> findMemberDto();

    @Query("select m from Member m where m.userName in :names")
        // names 안에 있는 이름과 동일한 name을 갖는 Member를 List로 뽑음
    List<Member> findByNames(@Param("names") Collection<String> names);

    // 컬렉션 : 엔티티를 컬렉션으로 꺼내올 때는 매개변수와 일치하는 엔티티가 없어도 null이 아니라 빈 값으로 가져와짐.
    // 따라서 List로 가져올 때는 != null을 사용하면 안됨!! (always true)
    List<Member> findListByUserName(String userName);

    // 단건 : 단건 조회에서는 매개변수와 일치하는 엔티티가 없을 시 null. (null 예외는 발생 X, 값이 null로 나옴)
    Member findMemberByUserName(String userName);

    Optional<Member> findOptionalByUserName(String userName); // 단건 Optional

    /**
     * 반환값이 Page면 count쿼리를 통해 TotalCount를 조회함.
     * 그런데 join을 통해 가져오는 쿼리를 작성했을 시 count쿼리가 기본적으로 left join으로 동작하기 때문에 쿼리가 복잡해지면 성능이 굉장히 나빠질 수 있음
     * 그래서 countQuery를 따로 작성해줘야함. 그러면 join 없이 단순한 count를 조회하기때문에 성능에 좋음
     */
    @Query(value = "select m from Member m left join m.team t", // @Query 생략 가능 -> 단순하게 페이징 함
            countQuery = "select count(m) from Member m")
    Page<Member> findByAge(int age, Pageable pageable);

    @Modifying(clearAutomatically = true)
    // 어노테이션이 필수임, clearAutomatically를 사용하면 일일이 flush, clear를 안해줘도 됨
    @Query("update Member m set m.age = m.age + 1 where m.age >= :age")
        // 벌크성 수정 쿼리
    int bulkAgePlus(@Param("age") int age);

    @Query("select m from Member m join fetch m.team")
    List<Member> findMemberFetchJoin();


    /**
     * JPQL 없이 페치조인 하고 싶을 때 엔티티그래프 사용.
     */
    @Override
    @EntityGraph(attributePaths = {"team"})
    List<Member> findAll();

    @Query("select m from Member m")
    @EntityGraph
    List<Member> findMemberEntityGraph();

    @EntityGraph(attributePaths = {"team"})
//    @EntityGraph("Member.all")
    List<Member> findEntityGraphByUserName(@Param("username") String username);


    @QueryHints(value = @QueryHint(name = "org.hibernate.readOnly", value = "true"))
    Member findReadOnlyByUserName(String userName);

    @Lock(LockModeType.PESSIMISTIC_READ)
    List<Member> findLockByUserName(String userName);
}
