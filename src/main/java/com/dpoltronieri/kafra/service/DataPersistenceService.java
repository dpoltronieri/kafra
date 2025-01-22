package com.dpoltronieri.kafra.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.dpoltronieri.kafra.data.GuildDTO;
import com.dpoltronieri.kafra.data.GuildDTORepository;
import com.dpoltronieri.kafra.data.MemberDTO;
import com.dpoltronieri.kafra.data.MemberDTORepository;
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
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

@Service
public class DataPersistenceService {

    private static final Logger logger = LoggerFactory.getLogger(DataPersistenceService.class);

    private final GuildDTORepository guildRepository;
    private final UserDTORepository userRepository;
    private final MemberDTORepository memberRepository;
    private final RoleDTORepository roleRepository;
    private final TextChannelDTORepository textChannelRepository;

    public DataPersistenceService(GuildDTORepository guildRepository,
                                    UserDTORepository userRepository,
                                    MemberDTORepository memberRepository,
                                    RoleDTORepository roleRepository,
                                    TextChannelDTORepository textChannelRepository) {
        this.guildRepository = guildRepository;
        this.userRepository = userRepository;
        this.memberRepository = memberRepository;
        this.roleRepository = roleRepository;
        this.textChannelRepository = textChannelRepository;
    }

    public void saveDataFromInteraction(SlashCommandInteractionEvent event) {
        try {
            Guild guild = event.getGuild();
            if (guild == null) {
                logger.warn("Guild is null in interaction event. Skipping data persistence.");
                return;
            }
            Long guildId = guild.getIdLong();

            // Guild
            GuildDTO guildDTO = guildRepository.findByGuildId(guildId)
                    .orElseGet(() -> {
                        GuildDTO newGuildDTO = new GuildDTO(guild);
                        logger.info("Creating new GuildDTO for guildId: {}", guildId);
                        return guildRepository.save(newGuildDTO);
                    });

            // Update Guild Information if changed
            if (guildDTO.hasChanged(guild)) {
                guildDTO.updateGuildDTO(guild);
                guildRepository.save(guildDTO);
                logger.info("Updated GuildDTO for guildId: {}", guildId);
            }

            // User
            User user = event.getUser();
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
            Member member = event.getMember();
            if (member == null) {
                logger.warn("Member is null in interaction event. Skipping member data persistence.");
                return;
            }
            Long memberId = member.getIdLong();

            MemberDTO memberDTO = memberRepository.findByMemberIdWithRoles(memberId)
                    .orElseGet(() -> {
                        MemberDTO newMemberDTO = new MemberDTO(member, guildDTO, userDTO);
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
            RoleDTO roleDTO = roleRepository.findByRoleId(roleId)
                    .orElseGet(() -> {
                        RoleDTO newRoleDTO = new RoleDTO(role, guildDTO);
                        logger.info("Creating new RoleDTO for roleId: {}", roleId);
                        return roleRepository.save(newRoleDTO);
                    });

            // Update Role Information if changed
            if (roleDTO.hasChanged(role)) {
                roleDTO.updateRoleDTO(role);
                roleRepository.save(roleDTO);
                logger.info("Updated RoleDTO for roleId: {}", roleId);
            }
        }
    }

    private void saveTextChannels(Guild guild, GuildDTO guildDTO) {
        for (TextChannel textChannel : guild.getTextChannels()) {
            Long textChannelId = textChannel.getIdLong();
            TextChannelDTO textChannelDTO = textChannelRepository.findByChannelId(textChannelId)
                    .orElseGet(() -> {
                        TextChannelDTO newTextChannelDTO = new TextChannelDTO(textChannel, guildDTO);
                        logger.info("Creating new TextChannelDTO for textChannelId: {}", textChannelId);
                        return textChannelRepository.save(newTextChannelDTO);
                    });

            // Update TextChannel Information if changed
            if (textChannelDTO.hasChanged(textChannel)) {
                textChannelDTO.updateTextChannelDTO(textChannel);
                textChannelRepository.save(textChannelDTO);
                logger.info("Updated TextChannelDTO for textChannelId: {}", textChannelId);
            }
        }
    }
}