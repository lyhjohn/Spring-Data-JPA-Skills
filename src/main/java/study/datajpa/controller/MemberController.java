package study.datajpa.controller;

import lombok.Builder;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;
import study.datajpa.dto.MemberDto;
import study.datajpa.entity.Member;
import study.datajpa.repository.MemberJpaRepository;
import study.datajpa.repository.MemberRepository;
import study.datajpa.repository.MemberRepositoryCustom;

import javax.annotation.PostConstruct;

@RestController
@RequiredArgsConstructor
public class MemberController {


    private final MemberRepository memberRepository;

    private final MemberJpaRepository memberJpaRepository;

    @GetMapping("/members/{id}")
    public String findMember(@PathVariable("id") Long id) {
        Member member = memberRepository.findById(id).get();
        return member.getUserName();
    }

    @PostMapping("/members0")
    public Member findMember3(@RequestBody Member member) {
        return member;
    }

    /**
     * pk 값을 받아 값을 조회할 때는 도메인 클래스 컨버터 사용 가능 (활용도 낮음:pk를 받아야하고 간단해야함)
     * HTTP 요청은 회원 id 를 받지만 도메인 클래스 컨버터가 중간에 동작해서 회원 엔티티 객체를 반환
     * 도메인 클래스 컨버터도 리파지토리를 사용해서 엔티티를 찾음
     * ★주의: 도메인 클래스 컨버터로 엔티티를 파라미터로 받으면, 이 엔티티는 단순 조회용으로만 사용해야 한다.
     * (트랜잭션이 없는 범위에서 엔티티를 조회했으므로, 엔티티를 변경해도 DB에 반영되지 않는다.)
     */
    @GetMapping("/members2/{id}")
    public String findMember2(@PathVariable("id") Member member) {
        return member.getUserName();
    }

    /**
     * http://localhost:8080/members?page=0 으로 페이지마다 값 호출 가능 (디폴트 20개)
     * http://localhost:8080/members?page=3&size=3 으로 페이지, 사이즈 정할 수 있음
     * http://localhost:8080/members?page=3&size=3&sort=age,desc&sort=id,desc 이렇게도 가능(디폴트는 asc)
     * 파라미터를 받으면 PageRequest를 자동으로 생성하고 값을 채워서 동작하게 해줌
     */
    @GetMapping("/members")
    public Page<MemberDto> list(@PageableDefault(size=5) Pageable pageable) {
        Page<Member> page = memberRepository.findAll(pageable);
        return page.map(member -> new MemberDto(member));
        // 페이지 호출 시 디폴트값 글로벌 변경은 application.yml에서 pageable 설정 변경 가능
        // @PageableDefault로 특정 메서드에 디폴트값 설정도 가능
    }

    @PostConstruct
    public void init() {
        for (int i = 0; i < 100; i++) {
            memberRepository.save(new Member("user" + i, i));
        }
    }

}
