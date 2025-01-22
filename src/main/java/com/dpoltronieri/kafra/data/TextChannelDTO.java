package com.dpoltronieri.kafra.data;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

import java.util.Objects;

@Entity
@Table(name = "text_channels")
public class TextChannelDTO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private Long channelId;

    @ManyToOne
    @JoinColumn(name = "guild_id")
    private GuildDTO guild;

    private String name;

    private String topic;

    private boolean isNSFW;

    private Long parentCategoryId;

    public TextChannelDTO(TextChannel textChannel, GuildDTO guildDTO) {
        this.channelId = textChannel.getIdLong();
        this.name = textChannel.getName();
        this.topic = textChannel.getTopic();
        this.isNSFW = textChannel.isNSFW();
        this.parentCategoryId = textChannel.getParentCategoryIdLong();
        this.guild = guildDTO;
    }

    public TextChannelDTO() {
    }

    public void updateTextChannelDTO(TextChannel textChannel) {
        this.name = textChannel.getName();
        this.topic = textChannel.getTopic();
        this.isNSFW = textChannel.isNSFW();
        this.parentCategoryId = textChannel.getParentCategoryIdLong();
    }

    public boolean hasChanged(TextChannel textChannel) {
        return !Objects.equals(this.name, textChannel.getName()) ||
                !Objects.equals(this.topic, textChannel.getTopic()) ||
                this.isNSFW != textChannel.isNSFW() ||
                !Objects.equals(this.parentCategoryId, textChannel.getParentCategoryIdLong());
    }

    // Getters and Setters ...

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getChannelId() {
        return this.channelId;
    }

    public void setChannelId(Long channelId) {
        this.channelId = channelId;
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

    public String getTopic() {
        return this.topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public boolean isIsNSFW() {
        return this.isNSFW;
    }

    public boolean getIsNSFW() {
        return this.isNSFW;
    }

    public void setIsNSFW(boolean isNSFW) {
        this.isNSFW = isNSFW;
    }

    public Long getParentCategoryId() {
        return this.parentCategoryId;
    }

    public void setParentCategoryId(Long parentCategoryId) {
        this.parentCategoryId = parentCategoryId;
    }

    @Override
    public String toString() {
        return "TextChannelDTO{" +
                "id=" + id +
                ", channelId='" + channelId + '\'' +
                ", name='" + name + '\'' +
                ", topic='" + topic + '\'' +
                ", isNSFW=" + isNSFW +
                ", parentCategoryId='" + parentCategoryId + '\'' +
                '}';
    }
}