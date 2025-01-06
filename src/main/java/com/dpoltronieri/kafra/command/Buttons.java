package com.dpoltronieri.kafra.command;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dpoltronieri.kafra.service.DataPersistenceService;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.components.ItemComponent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;

@Service
public class Buttons implements Command {

    // @Autowired
    // GuildDTORepository guildDTORepository;

    // @Autowired
    // MemberDTORepository memberDTORepository;

    // @Autowired
    // RoleDTORepository roleDTORepository;

    // @Autowired
    // TextChannelDTORepository textChannelDTORepository;

    // @Autowired
    // UserDTORepository userDTORepository;

    @Autowired
    DataPersistenceService dataPersistenceService;

    @Override
    public String getName() {
        return "button";
    }

    @Override
    public List<String> getButtons() {
        return List.of("yes-button", "no-button");
    }

    @Override
    public List<String> getModals() {
        return List.of();
    }

    @Override
    public String getDescription() {
        return "Buttons";
    }

    @Override
    public List<OptionData> getOptions() {
        return null;
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {

        dataPersistenceService.saveDataFromInteraction(event);

        // User user = event.getUser();
        // try{
        // UserDTO userDTO = new UserDTO(user);
        // userDTORepository.save(userDTO);
        // } catch (Exception e) {
        //     e.printStackTrace();
        // }
        // Guild guild = event.getGuild();
        // try{
        // guildDTORepository.save(new GuildDTO(guild));
        // } catch (Exception e) {
        //     e.printStackTrace();
        // }
        // Member member = event.getMember();
        // try{
        // memberDTORepository.save(new MemberDTO(member));
        // } catch (Exception e) {
        //     e.printStackTrace();



        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setTitle("Pizza?");
        embedBuilder.setDescription("Do you like pizza?");

        Button yesButton = Button.danger("yes-button", "Yes");
        Button noButton = Button.danger("no-button", "No");
        final List<ItemComponent> list = new ArrayList<>();
        list.add(yesButton);
        list.add(noButton);

        MessageCreateData message = new MessageCreateBuilder()
        .setEmbeds(embedBuilder.build())
        .setActionRow(list)
        .build();

        event.reply(message).queue();
    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        // TODO Auto-generated method stub
        // throw new UnsupportedOperationException("Unimplemented method 'onButtonInteraction'");
        if(event.getButton().getId().equals("yes-button")) {
            event.reply("Nice, so do I").queue();
        } else if(event.getButton().getId().equals("no-button")) {
            event.reply("What! you monster").queue();
        }
        event.getMessage().delete().queue();
    }

    @Override
    public void onModalInteraction(ModalInteractionEvent event) {
        // TODO Auto-generated method stub
        // throw new UnsupportedOperationException("Unimplemented method 'onModalInteraction'");
    }

}
