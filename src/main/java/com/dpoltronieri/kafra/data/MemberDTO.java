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
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;

@Entity
@Table(name = "members")
public class MemberDTO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private Long memberId;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private UserDTO user;

    @ManyToOne
    @JoinColumn(name = "guild_id")
    private GuildDTO guild;

    private String nickname;

    private boolean isTimedOut;

    @ManyToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.DETACH })
    @JoinTable(name = "member_role",
            joinColumns = @JoinColumn(name = "member_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"))
    private List<RoleDTO> roles = new ArrayList<>();

    // ... other relevant fields (e.g., joinedAt, roles, etc.)

    public MemberDTO(Member member, GuildDTO guildDTO, UserDTO userDTO) {
        this.memberId = member.getIdLong();
        this.user = userDTO;
        this.guild = guildDTO;
        this.nickname = member.getNickname();
        this.isTimedOut = member.isTimedOut();
        // Map the Member's Roles to RoleDTOs
        this.roles = new ArrayList<>();
        for (Role role : member.getRoles()) {
            RoleDTO roleDTO = new RoleDTO(role, guildDTO); // Assuming you have a RoleDTO constructor that takes a Role
            this.roles.add(roleDTO);
        }
    }

    public MemberDTO() {
    }

    public void updateMemberDTO(Member member) {
        this.nickname = member.getNickname();
        this.isTimedOut = member.isTimedOut();

        // Update roles - efficiently handle additions and removals
        List<Role> memberRoles = member.getRoles();
        List<RoleDTO> updatedRoles = new ArrayList<>();

        // 1. Add new roles or find existing ones
        for (Role role : memberRoles) {
            RoleDTO roleDTO = findRoleDTO(role.getIdLong()); // Helper method to find RoleDTO by roleId
            if (roleDTO == null) {
                // Role not found, create a new one (assuming you have a method to find or create RoleDTO by Role)
                roleDTO = new RoleDTO(role, this.guild);
            }
            updatedRoles.add(roleDTO);
        }

        // 2. Update the roles list
        this.roles.clear();
        this.roles.addAll(updatedRoles);
    }

    public boolean hasChanged(Member member) {
        if (!Objects.equals(this.nickname, member.getNickname())) {
            return true;
        }
        if (this.isTimedOut != member.isTimedOut()) {
            return true;
        }

        // Check if the roles have changed
        List<Role> memberRoles = member.getRoles();
        if (this.roles.size() != memberRoles.size()) {
            return true;
        }

        for (Role role : memberRoles) {
            if (findRoleDTO(role.getIdLong()) == null) {
                return true; // Role in Member not found in MemberDTO
            }
        }

        return false;
    }

    // Helper method to find a RoleDTO in the current list by roleId
    private RoleDTO findRoleDTO(Long roleId) {
        for (RoleDTO roleDTO : this.roles) {
            if (Objects.equals(roleDTO.getRoleId(), roleId)) {
                return roleDTO;
            }
        }
        return null;
    }

    // Getters and Setters ...

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

    public UserDTO getUser() {
        return this.user;
    }

    public void setUser(UserDTO userDTO) {
        this.user = userDTO;
    }

    public GuildDTO getGuild() {
        return this.guild;
    }

    public void setGuild(GuildDTO guildDTO) {
        this.guild = guildDTO;
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

    public List<RoleDTO> getRoles() {
        return this.roles;
    }

    public void setRoles(List<RoleDTO> roles) {
        this.roles = roles;
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