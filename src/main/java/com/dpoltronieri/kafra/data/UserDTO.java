package com.dpoltronieri.kafra.data;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import net.dv8tion.jda.api.entities.User;

@Entity
@Table(name = "users", indexes = {
    @Index(name = "idx_user_id", columnList = "user_id", unique = true)
})
public class UserDTO {
    @Id // Mark this field as the primary key
    @GeneratedValue(strategy = GenerationType.IDENTITY) 
    private Long id; 

    @Column(unique = true, nullable = false)
    private Long userId; 

    @Column //(name = "user_name") // Optional: Specify column name if different from field name
    private String username; 

    @Column //(name = "discriminator")
    private String discriminator;

    @Column //(name = "avatar_url")
    private String avatarUrl;

    @Column
    private boolean bot;

    @Column
    private boolean system;

    
    public UserDTO(User user) {
            // this.id = 
        this.userId = user.getIdLong();
        this.username = user.getName();
        this.discriminator = user.getDiscriminator();
        this.avatarUrl = user.getAvatarUrl();
        this.bot = user.isBot();
        this.system = user.isSystem();
    }


    public UserDTO() {
    }


    public void updateUserDTO(User user) {
        // this.discordID = user.getId();
        this.username = user.getName();
        this.discriminator = user.getDiscriminator();
        this.avatarUrl = user.getAvatarUrl();
        this.bot = user.isBot();
        this.system = user.isSystem();
    }

    // Getters and Setters

    @Override
    public String toString() {
        return "JDAUser{" +
                "id='" + id + '\'' +
                ", username='" + username + '\'' +
                ", discriminator='" + discriminator + '\'' +
                ", avatarUrl='" + avatarUrl + '\'' +
                ", bot=" + bot +
                ", system=" + system +
                '}';
    }


    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return this.username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getDiscriminator() {
        return this.discriminator;
    }

    public void setDiscriminator(String discriminator) {
        this.discriminator = discriminator;
    }

    public String getAvatarUrl() {
        return this.avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public boolean isBot() {
        return this.bot;
    }

    public boolean getBot() {
        return this.bot;
    }

    public void setBot(boolean bot) {
        this.bot = bot;
    }

    public boolean isSystem() {
        return this.system;
    }

    public boolean getSystem() {
        return this.system;
    }

    public void setSystem(boolean system) {
        this.system = system;
    }

    public Long getUserId() {
        return this.userId;
    }

    public void setUserId(Long discordID) {
        this.userId = discordID;
    }


    

}
