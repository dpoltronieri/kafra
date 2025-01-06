package com.dpoltronieri.kafra.service;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.dpoltronieri.kafra.data.GuildDTO;
import com.dpoltronieri.kafra.data.GuildDTORepository;
import com.dpoltronieri.kafra.data.MemberDTO;
import com.dpoltronieri.kafra.data.MemberDTORepository;
import com.dpoltronieri.kafra.data.RoleDTORepository;
import com.dpoltronieri.kafra.data.TextChannelDTORepository;
import com.dpoltronieri.kafra.data.UserDTO;
import com.dpoltronieri.kafra.data.UserDTORepository;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

@Service
public class DataPersistenceService {

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

        Guild guild = event.getGuild();
        Long guildId = guild.getIdLong();
    
        // Check if guild exists in the database
        Optional<GuildDTO> existingGuildDTO = guildRepository.findByGuildId(guildId);
    
        if (existingGuildDTO.isPresent()) {
            // Guild exists, update data
            existingGuildDTO.get().updateGuildDTO(guild);
            // Update relevant fields in guildDTOToUpdate based on the event data
            guildRepository.save(existingGuildDTO.get());
        } else {
            // Guild doesn't exist, create a new GuildDTO and save
            GuildDTO newGuildDTO = new GuildDTO(guild);
            guildRepository.save(newGuildDTO);
        }

    // User
    User user = event.getUser();
    Long userId = user.getIdLong();

    Optional<UserDTO> existingUserDTO = userRepository.findByUserId(userId);

    if (existingUserDTO.isPresent()) {
        // User exists, update data (optional, based on your use case)
        // existingUserDTO.get().updateUserDTO(user);
        existingUserDTO.get().updateUserDTO(user);
        userRepository.save(existingUserDTO.get());
    } else {
        // User doesn't exist, create a new UserDTO and save
        UserDTO newUserDTO = new UserDTO(user);
        userRepository.save(newUserDTO);
    }

    // Member
    Member member = event.getMember();
    Long memberId = member.getIdLong();

    Optional<MemberDTO> existingMemberDTO = memberRepository.findByMemberId(memberId);

    if (existingMemberDTO.isPresent()) {
        // Member exists, update data (optional, based on your use case)
        // existingMemberDTO.get().updateMemberDTO(member);
        memberRepository.save(existingMemberDTO.get());
    } else {
        // Member doesn't exist, create a new MemberDTO and save
        MemberDTO newMemberDTO = new MemberDTO(member); // Link to existing GuildDTO
        memberRepository.save(newMemberDTO);
    }

    }
}
