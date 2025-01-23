package com.dpoltronieri.kafra.service;

import com.dpoltronieri.kafra.data.GuildDTO;
import com.dpoltronieri.kafra.data.GuildDTORepository;
import com.dpoltronieri.kafra.data.MemberDTO;
import com.dpoltronieri.kafra.data.MemberDTORepository;
import com.dpoltronieri.kafra.data.Raid;
import com.dpoltronieri.kafra.data.RaidRepository;
import com.dpoltronieri.kafra.data.RoleDTO;
import com.dpoltronieri.kafra.data.RoleDTORepository;
import com.dpoltronieri.kafra.data.TextChannelDTO;
import com.dpoltronieri.kafra.data.TextChannelDTORepository;
import com.dpoltronieri.kafra.data.UserDTO;
import com.dpoltronieri.kafra.data.UserDTORepository;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.Interaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DataPersistenceService {

    private static final Logger logger = LoggerFactory.getLogger(DataPersistenceService.class);

    private final GuildDTORepository guildRepository;
    private final UserDTORepository userRepository;
    private final MemberDTORepository memberRepository;
    private final RoleDTORepository roleRepository;
    private final TextChannelDTORepository textChannelRepository;
    private final RaidRepository raidRepository;

    public DataPersistenceService(GuildDTORepository guildRepository,
                                    UserDTORepository userRepository,
                                    MemberDTORepository memberRepository,
                                    RoleDTORepository roleRepository,
                                    TextChannelDTORepository textChannelRepository,
                                    RaidRepository raidRepository) {
        this.guildRepository = guildRepository;
        this.userRepository = userRepository;
        this.memberRepository = memberRepository;
        this.roleRepository = roleRepository;
        this.textChannelRepository = textChannelRepository;
        this.raidRepository = raidRepository;
    }

    // ... other methods for Guild, User, Member, Role, TextChannel, Raid ...

    @Transactional
    public void saveDataFromInteraction(Interaction interaction) {
        try {
            Guild guild = null;
            Member member = null;

            if (interaction instanceof SlashCommandInteractionEvent) {
                SlashCommandInteractionEvent slashEvent = (SlashCommandInteractionEvent) interaction;
                guild = slashEvent.getGuild();
                member = slashEvent.getMember();
            } else if (interaction instanceof ButtonInteractionEvent) {
                ButtonInteractionEvent buttonEvent = (ButtonInteractionEvent) interaction;
                guild = buttonEvent.getGuild();
                member = buttonEvent.getMember();
            } else if (interaction instanceof ModalInteractionEvent) {
                ModalInteractionEvent modalEvent = (ModalInteractionEvent) interaction;
                guild = modalEvent.getGuild();
                member = modalEvent.getMember();
            }

            if (guild == null) {
                logger.warn("Guild is null in interaction event. Skipping data persistence.");
                return;
            }
            final Guild finalGuild = guild; // Effectively final declaration
            Long guildId = guild.getIdLong();

            // Guild
            GuildDTO guildDTO = guildRepository.findByGuildId(guildId)
                    .orElseGet(() -> {
                        GuildDTO newGuildDTO = new GuildDTO(finalGuild);
                        logger.info("Creating new GuildDTO for guildId: {}", guildId);
                        return guildRepository.save(newGuildDTO);
                    });

            // Update Guild Information if changed
            if (guildDTO.hasChanged(guild)) {
                guildDTO.updateGuildDTO(guild);
                guildRepository.save(guildDTO);
                logger.info("Updated GuildDTO for guildId: {}", guildId);
            }

            if (member == null) {
                logger.warn("Member is null in interaction event. Skipping member data persistence.");
                return;
            }
            final Member finalMember = member;
            // User
            User user = member.getUser();
            Long userId = user.getIdLong();

            UserDTO userDTO = userRepository.findByUserId(userId)
                    .orElseGet(() -> {
                        UserDTO newUserDTO = new UserDTO(user);
                        logger.info("Creating new UserDTO for userId: {}", userId);
                        return userRepository.save(newUserDTO);
                    });

            // Update User Information if changed
            if (userDTO.hasChanged(user)) {
                userDTO.updateUserDTO(user);
                userRepository.save(userDTO);
                logger.info("Updated UserDTO for userId: {}", userId);
            }

            // Member
            Long memberId = member.getIdLong();
            

            MemberDTO memberDTO = memberRepository.findByMemberId(memberId)
                    .orElseGet(() -> {
                        MemberDTO newMemberDTO = new MemberDTO(finalMember, guildDTO, userDTO);
                        logger.info("Creating new MemberDTO for memberId: {}", memberId);
                        return memberRepository.save(newMemberDTO);
                    });

            // Update Member Information if changed
            if (memberDTO.hasChanged(member)) {
                memberDTO.updateMemberDTO(member);
                memberRepository.save(memberDTO);
                logger.info("Updated MemberDTO for memberId: {}", memberId);
            }

            // Roles
            saveRoles(member, guildDTO);

            // TextChannels
            saveTextChannels(guild, guildDTO);
        } catch (Exception e) {
            logger.error("Error during data persistence", e);
        }
    }

    private void saveRoles(Member member, GuildDTO guildDTO) {
        for (Role role : member.getRoles()) {
            Long roleId = role.getIdLong();
            RoleDTO roleDTO = roleRepository.findByRoleIdAndGuild(roleId, guildDTO)
                    .orElseGet(() -> {
                        RoleDTO newRoleDTO = new RoleDTO(role, guildDTO);
                        logger.info("Creating new RoleDTO for roleId: {}, guildId: {}", roleId, guildDTO.getGuildId());
                        return roleRepository.save(newRoleDTO);
                    });

            // Update Role Information if changed
            if (roleDTO.hasChanged(role)) {
                roleDTO.updateRoleDTO(role);
                roleRepository.save(roleDTO);
                logger.info("Updated RoleDTO for roleId: {}, guildId: {}", roleId, guildDTO.getGuildId());
            }
        }
    }

    private void saveTextChannels(Guild guild, GuildDTO guildDTO) {
        for (TextChannel textChannel : guild.getTextChannels()) {
            Long textChannelId = textChannel.getIdLong();
            TextChannelDTO textChannelDTO = textChannelRepository.findByChannelIdAndGuild(textChannelId, guildDTO)
                    .orElseGet(() -> {
                        TextChannelDTO newTextChannelDTO = new TextChannelDTO(textChannel, guildDTO);
                        logger.info("Creating new TextChannelDTO for textChannelId: {}, guildId: {}", textChannelId, guildDTO.getGuildId());
                        return textChannelRepository.save(newTextChannelDTO);
                    });

            // Update TextChannel Information if changed
            if (textChannelDTO.hasChanged(textChannel)) {
                textChannelDTO.updateTextChannelDTO(textChannel);
                textChannelRepository.save(textChannelDTO);
                logger.info("Updated TextChannelDTO for textChannelId: {}, guildId: {}", textChannelId, guildDTO.getGuildId());
            }
        }
    }

       @Transactional(readOnly = true)
    public Raid findRaidByEventId(Long eventId) {
        return raidRepository.findByEventId(eventId);
    }

    @Transactional
    public Raid saveRaid(Raid raid) {
        return raidRepository.save(raid);
    }

    @Transactional(readOnly = true)
    public Raid findRaidById(Long id) {
        return raidRepository.findById(id).orElse(null);
    }

    @Transactional
    public void deleteRaid(Raid raid) {
        raidRepository.delete(raid);
    }

    @Transactional
    public MemberDTO findOrCreateMember(Member member) {
        if (member == null) {
            return null;
        }

        return memberRepository.findByMemberId(member.getIdLong())
                .orElseGet(() -> {
                    Guild guild = member.getGuild();
                    Long guildId = guild.getIdLong();

                    // Guild
                    GuildDTO guildDTO = guildRepository.findByGuildId(guildId)
                            .orElseGet(() -> {
                                GuildDTO newGuildDTO = new GuildDTO(guild);
                                logger.info("Creating new GuildDTO for guildId: {}", guildId);
                                return guildRepository.save(newGuildDTO);
                            });

                    // User
                    User user = member.getUser();
                    Long userId = user.getIdLong();

                    UserDTO userDTO = userRepository.findByUserId(userId)
                            .orElseGet(() -> {
                                UserDTO newUserDTO = new UserDTO(user);
                                logger.info("Creating new UserDTO for userId: {}", userId);
                                return userRepository.save(newUserDTO);
                            });
                    MemberDTO newMemberDTO = new MemberDTO(member, guildDTO, userDTO);
                    logger.info("Creating new MemberDTO for memberId: {}", member.getIdLong());
                    return memberRepository.save(newMemberDTO);
                });
    }
}