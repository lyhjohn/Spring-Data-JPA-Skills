package study.datajpa.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import study.datajpa.entity.Member;

@Data
public class MemberDto {
    private Long id;
    private String name;
    private String teamName;

    public MemberDto(Long id, String name, String teamName) {
        this.id = id;
        this.name = name;
        this.teamName = teamName;
    }

    public MemberDto(Member member) {
        this.id = member.getId();
        this.name = member.getUserName();
    }
}
