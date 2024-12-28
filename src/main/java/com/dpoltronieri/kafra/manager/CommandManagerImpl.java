package com.dpoltronieri.kafra.manager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import com.dpoltronieri.kafra.command.Command;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

@Service
public class CommandManagerImpl extends ListenerAdapter {

    private List<Command> commands = new ArrayList<>();
    private Map<String, Command> commandMap = new HashMap<>();

    @Value("${spring.jda.token}")
    private String token;

    private JDA jda = JDABuilder.createDefault(token).build();


    @Override
    public void onReady(@NotNull ReadyEvent event) {
        for(Guild guild : event.getJDA().getGuilds()) {
            for(Command command : commands) {
                if(command.getOptions() == null) {
                    guild.upsertCommand(command.getName(), command.getDescription()).queue();
                } else {
                    guild.upsertCommand(command.getName(), command.getDescription()).addOptions(command.getOptions()).queue();
                }
            }
        }
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        try{
        commandMap.get(event.getName()).onSlashCommandInteraction(event);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void add(Command command) {
        commands.add(command);
    }

    @Autowired
    public void configureCommands(List<Command> commands) {
        commands.forEach((command) -> {
            this.commands.add(command);
            commandMap.put(command.getName(), command);
            command.getButtons().forEach((button) -> {
                commandMap.put(button, command);
            });
            command.getModals().forEach((modal) -> {
                commandMap.put(modal, command);
            });
        });

        jda.addEventListener(this);
    }

    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        try{
        commandMap.get(event.getButton().getId()).onButtonInteraction(event);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onModalInteraction(@NotNull ModalInteractionEvent event){
        try{
        commandMap.get(event.getModalId()).onModalInteraction(event);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
