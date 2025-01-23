package com.dpoltronieri.kafra.data;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "raids")
public class Raid {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // @Transient
    private Long eventId; // Still useful for temporary association with the message, but won't be persisted as is

    private String name;
    private String date;
    private String time;
    private String description;
    private Integer sizeLimit;

    @ManyToOne
    @JoinColumn(name = "creator_id")
    private MemberDTO creator;

    @ManyToMany
    @JoinTable(
        name = "raid_confirmed_participants",
        joinColumns = @JoinColumn(name = "raid_id"),
        inverseJoinColumns = @JoinColumn(name = "member_id")
    )
    private List<MemberDTO> confirmedParticipants;

    @ManyToMany
    @JoinTable(
        name = "raid_unconfirmed_participants",
        joinColumns = @JoinColumn(name = "raid_id"),
        inverseJoinColumns = @JoinColumn(name = "member_id")
    )
    private List<MemberDTO> unconfirmedParticipants;

    @ManyToMany
    @JoinTable(
        name = "raid_benched_participants",
        joinColumns = @JoinColumn(name = "raid_id"),
        inverseJoinColumns = @JoinColumn(name = "member_id")
    )
    private List<MemberDTO> benchedParticipants;

    // Constructor, Getters, and Setters

    public Raid() {
        this.confirmedParticipants = new ArrayList<>();
        this.unconfirmedParticipants = new ArrayList<>();
        this.benchedParticipants = new ArrayList<>();
    }
    
    // Updated constructor to match new signature
    public Raid(Long eventId, String name, String date, String time, String description, Integer sizeLimit, MemberDTO creator) {
        this.eventId = eventId;
        this.name = name;
        this.date = date;
        this.time = time;
        this.description = description;
        this.sizeLimit = sizeLimit;
        this.creator = creator;
        this.confirmedParticipants = new ArrayList<>();
        this.unconfirmedParticipants = new ArrayList<>();
        this.benchedParticipants = new ArrayList<>();
    }

    // Getters
    public Long getId() {
        return id;
    }

    // Add a getter for eventId
    public Long getEventId() {
        return eventId;
    }

    public String getName() {
        return name;
    }

    public String getDate() {
        return date;
    }

    public String getTime() {
        return time;
    }

    public String getDescription() {
        return description;
    }

    public Integer getSizeLimit() {
        return sizeLimit;
    }

    public MemberDTO getCreator() {
        return creator;
    }

    public List<MemberDTO> getConfirmedParticipants() {
        return confirmedParticipants;
    }

    public List<MemberDTO> getUnconfirmedParticipants() {
        return unconfirmedParticipants;
    }

    public List<MemberDTO> getBenchedParticipants() {
        return benchedParticipants;
    }

    // Setters
    public void setId(Long id) {
        this.id = id;
    }

    public void setEventId(Long eventId) {
        this.eventId = eventId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setSizeLimit(Integer sizeLimit) {
        this.sizeLimit = sizeLimit;
    }

    public void setCreator(MemberDTO creator) {
        this.creator = creator;
    }
    // Methods to manage participants
    public void addConfirmedParticipant(MemberDTO member) {
        confirmedParticipants.add(member);
        unconfirmedParticipants.remove(member);
        benchedParticipants.remove(member);
    }

    public void addUnconfirmedParticipant(MemberDTO member) {
        unconfirmedParticipants.add(member);
        confirmedParticipants.remove(member);
        benchedParticipants.remove(member);
    }

    public void addBenchedParticipant(MemberDTO member) {
        benchedParticipants.add(member);
        confirmedParticipants.remove(member);
        unconfirmedParticipants.remove(member);
    }

    public void removeParticipant(MemberDTO member) {
        confirmedParticipants.remove(member);
        unconfirmedParticipants.remove(member);
        benchedParticipants.remove(member);
    }
}