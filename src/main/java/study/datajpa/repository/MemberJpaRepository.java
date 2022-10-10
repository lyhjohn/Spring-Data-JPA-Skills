package study.datajpa.repository;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import study.datajpa.entity.Member;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;
import java.util.Optional;

@Repository
public class MemberJpaRepository {

    @PersistenceContext
    private EntityManager em;

    @Transactional
    public Member save(Member member) {

        em.persist(member);
        return member;
    }

    public void delete(Member member) {
        em.remove(member);
    }

    public List<Member> findAll() {
        return em.createQuery("select m from Member m", Member.class) // 객체를 대상으로하는 jpql / 반환타입이 Member.class
                .getResultList();
    }

    public Optional<Member> findById(Long id) {
        Member member = em.find(Member.class, id);
        return Optional.ofNullable(member); // member가 Null 일수도 있음을 알려주면서 Optional로 반환
    }

    public Long count() {
        return em.createQuery("select count(m) from Member m", Long.class) // count의 반환타입은 Long
                .getSingleResult(); // 결과는 하나만 반환
    }

    public Member find(Long id) {
        return em.find(Member.class, id);
    }

    public List<Member> findByUserNameAndAgeGreaterThen(String userName, int age) {
        return em.createQuery("select m from Member m where m.userName = :userName and m.age > :age", Member.class)
                .setParameter("userName", userName)
                .setParameter("age", age)
                .getResultList();
    }

    public List<Member> findByPage(int age, int offset, int limit) { // offset 부터 limit까지 페이지로 가져올 것
        return em.createQuery("select m from Member m where m.age = :age " +
                        "order by m.userName desc", Member.class)
                .setParameter("age", age)
                .setFirstResult(offset)
                .setMaxResults(limit)
                .getResultList();
    }

    public long totalCount(int age) {
        return em.createQuery("select count(m) from Member m where m.age = :age", Long.class)
                .setParameter("age", age)
                .getSingleResult();
    }

    public int bulkAgePlus(int age) {
        return em.createQuery("update Member m set m.age = m.age + 1 where m.age >= :age")
                .setParameter("age", age)
                .executeUpdate(); // 응답값의 개수가 리턴됨
    }


}
