package com.dpoltronieri.kafra.data;

import jakarta.persistence.*;
import net.dv8tion.jda.api.entities.Role;

import java.util.Objects;

@Entity
@Table(name = "roles")
public class RoleDTO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private Long roleId;

    @ManyToOne
    @JoinColumn(name = "guild_id")
    private GuildDTO guild;

    private String name;

    private int position;

    private boolean isMentionable;

    private boolean isHoisted;

    private boolean isManaged;

    private boolean isPublicRole;

    public RoleDTO(Role role, GuildDTO guildDTO) {
        this.roleId = role.getIdLong();
        this.name = role.getName();
        this.position = role.getPosition();
        this.isMentionable = role.isMentionable();
        this.isHoisted = role.isHoisted();
        this.isManaged = role.isManaged();
        this.isPublicRole = role.isPublicRole();
        this.guild = guildDTO;
    }

    public RoleDTO() {
    }

    public void updateRoleDTO(Role role) {
        this.name = role.getName();
        this.position = role.getPosition();
        this.isMentionable = role.isMentionable();
        this.isHoisted = role.isHoisted();
        this.isManaged = role.isManaged();
        this.isPublicRole = role.isPublicRole();
    }

    public boolean hasChanged(Role role) {
        return !Objects.equals(this.name, role.getName()) ||
                this.position != role.getPosition() ||
                this.isMentionable != role.isMentionable() ||
                this.isHoisted != role.isHoisted() ||
                this.isManaged != role.isManaged() ||
                this.isPublicRole != role.isPublicRole();
    }

    // Getters and Setters ...

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getRoleId() {
        return this.roleId;
    }

    public void setRoleId(Long roleId) {
        this.roleId = roleId;
    }

    public GuildDTO getGuild() {
        return this.guild;
    }

    public void setGuild(GuildDTO guildDTO) {
        this.guild = guildDTO;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getPosition() {
        return this.position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public boolean isIsMentionable() {
        return this.isMentionable;
    }

    public boolean getIsMentionable() {
        return this.isMentionable;
    }

    public void setIsMentionable(boolean isMentionable) {
        this.isMentionable = isMentionable;
    }

    public boolean isIsHoisted() {
        return this.isHoisted;
    }

    public boolean getIsHoisted() {
        return this.isHoisted;
    }

    public void setIsHoisted(boolean isHoisted) {
        this.isHoisted = isHoisted;
    }

    public boolean isIsManaged() {
        return this.isManaged;
    }

    public boolean getIsManaged() {
        return this.isManaged;
    }

    public void setIsManaged(boolean isManaged) {
        this.isManaged = isManaged;
    }

    public boolean isIsPublicRole() {
        return this.isPublicRole;
    }

    public boolean getIsPublicRole() {
        return this.isPublicRole;
    }

    public void setIsPublicRole(boolean isPublicRole) {
        this.isPublicRole = isPublicRole;
    }
}