package com.dpoltronieri.kafra.data;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "missions")
public class Mission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name;

    @Column(nullable = false)
    private String type;

    @Column(columnDefinition = "TEXT") // Use TEXT for potentially longer descriptions
    private String description;

    @ManyToOne
    @JoinColumn(name = "creator_id")
    private MemberDTO creator;

    @ManyToOne
    @JoinColumn(name = "guild_id") // Add this for the relationship with GuildDTO
    private GuildDTO guild;

    // Constructors
    public Mission() {
    }

    public Mission(String name, String type, String description, MemberDTO creator, GuildDTO guild) {
        this.name = name;
        this.type = type;
        this.description = description;
        this.creator = creator;
        this.guild = guild;
    }

    // Getters and Setters

    // ... (Getters and setters for id, name, type, description, creator)

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return this.type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public MemberDTO getCreator() {
        return this.creator;
    }

    public void setCreator(MemberDTO creator) {
        this.creator = creator;
    }

    public GuildDTO getGuild() {
        return guild;
    }

    public void setGuild(GuildDTO guild) {
        this.guild = guild;
    }
}