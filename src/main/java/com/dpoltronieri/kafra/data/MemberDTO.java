package com.dpoltronieri.kafra.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import net.dv8tion.jda.api.entities.Member;

@Entity
@Table(name = "members")
public class MemberDTO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private Long memberId;

    private Long userId;

    private Long guildId;

    private String nickname;

    private boolean isTimedOut;
    
    // @OneToMany(mappedBy = "guild", cascade = CascadeType.ALL, orphanRemoval = true)
    @OneToMany(cascade = CascadeType.ALL)
    private List<RoleDTO> roles;
    
        // ... other relevant fields (e.g., joinedAt, roles, etc.)
    
    public MemberDTO(Member member) {
            this.memberId = member.getIdLong();
            this.userId = member.getUser().getIdLong();
            this.guildId = member.getGuild().getIdLong();
            this.nickname = member.getNickname();
            this.isTimedOut = member.isTimedOut();
            this.roles = new ArrayList<>();
            member.getRoles().forEach(role -> {
                this.roles.add(new RoleDTO(role));
        });
    }

    public MemberDTO() {
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getMemberId() {
        return this.memberId;
    }

    public void setMemberId(Long memberId) {
        this.memberId = memberId;
    }

    public Long getUserId() {
        return this.userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getGuildId() {
        return this.guildId;
    }

    public void setGuildId(Long guildId) {
        this.guildId = guildId;
    }

    public String getNickname() {
        return this.nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public boolean isIsTimedOut() {
        return this.isTimedOut;
    }

    public boolean getIsTimedOut() {
        return this.isTimedOut;
    }

    public void setIsTimedOut(boolean isTimedOut) {
        this.isTimedOut = isTimedOut;
    }
    

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MemberDTO memberDTO = (MemberDTO) o;
        return id.equals(memberDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
