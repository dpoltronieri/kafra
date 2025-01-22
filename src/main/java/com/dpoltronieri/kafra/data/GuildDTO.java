package com.dpoltronieri.kafra.data;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import net.dv8tion.jda.api.entities.Guild;

import java.util.Objects;

@Entity
@Table(name = "guilds")
public class GuildDTO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private Long guildId;

    private String name;

    private String iconUrl;

    private Long ownerId;

    private int memberCount;

    private String botRoleMention;

    // private List<GuildChannel> guildChanels;

    // Constructor to create GuildDTO from Guild object
    public GuildDTO(Guild guild) {
        this.guildId = guild.getIdLong();
        this.name = guild.getName();
        this.iconUrl = guild.getIconUrl();
        this.ownerId = guild.getOwnerIdLong();
        this.memberCount = guild.getMemberCount();
        this.botRoleMention = guild.getBotRole().getAsMention();
        // this.guildChanels = guild.getChannels();
    }


    public GuildDTO() {
    }


    public void updateGuildDTO(Guild guild) {
        // this.guildId = guild.getIdLong();
        this.name = guild.getName();
        this.iconUrl = guild.getIconUrl();
        this.ownerId = guild.getOwnerIdLong();
        this.memberCount = guild.getMemberCount();
        this.botRoleMention = guild.getBotRole().getAsMention();
    }

    public boolean hasChanged(Guild guild) {
        return !Objects.equals(this.name, guild.getName()) ||
                !Objects.equals(this.iconUrl, guild.getIconUrl()) ||
                !Objects.equals(this.ownerId, guild.getOwnerIdLong()) ||
                this.memberCount != guild.getMemberCount() ||
                !Objects.equals(this.botRoleMention, guild.getBotRole().getAsMention());
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getGuildId() {
        return this.guildId;
    }

    public void setGuildId(Long guildId) {
        this.guildId = guildId;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIconUrl() {
        return this.iconUrl;
    }

    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }

    public Long getOwnerId() {
        return this.ownerId;
    }

    public void setOwnerId(Long ownerId) {
        this.ownerId = ownerId;
    }

    public int getMemberCount() {
        return this.memberCount;
    }

    public void setMemberCount(int memberCount) {
        this.memberCount = memberCount;
    }

    public String getBotRoleMention() {
        return this.botRoleMention;
    }

    public void setBotRoleMention(String region) {
        this.botRoleMention = region;
    }


    @Override
    public String toString() {
        return "{" +
                " id='" + getId() + "'" +
                ", guildId='" + getGuildId() + "'" +
                ", name='" + getName() + "'" +
                ", iconUrl='" + getIconUrl() + "'" +
                ", ownerId='" + getOwnerId() + "'" +
                ", memberCount='" + getMemberCount() + "'" +
                ", region='" + getBotRoleMention() + "'" +
                "}";
    }


}