package com.dpoltronieri.kafra.manager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
import net.dv8tion.jda.api.requests.GatewayIntent;

@Service
public class CommandManagerImpl extends ListenerAdapter {

    private List<Command> commands = new ArrayList<>();
    private Map<String, Command> commandMap = new HashMap<>();
    private JDA jda;

    public CommandManagerImpl(@Value("${spring.jda.token}") String token) {
        this.jda = JDABuilder.createDefault(token).enableIntents(GatewayIntent.MESSAGE_CONTENT).build();
    }

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
        try {
            String componentId = event.getComponentId();
            if (componentId == null) return;

            Command command = null;
            if (componentId.contains("|")) { // Check if componentId contains "|" - indicates complex ID
                String[] parts = componentId.split("\\|"); // Split using |
                if (parts.length == 2) { // Expecting two parts for complex ID format
                    String commandPrefix = parts[0] + "|"; // Reconstruct command prefix
                    command = commandMap.get(commandPrefix); // Lookup command by prefix
                } else {
                    System.err.println("Invalid complex button ID format: " + componentId); // Log error for invalid complex ID
                    return;
                }
            } else { // Assume it's a simple button ID if no "|" is present
                command = commandMap.get(componentId); // Directly lookup command by componentId (simple ID)
            }

            if (command != null) {
                command.onButtonInteraction(event); // Dispatch button interaction to the command
            } else {
                System.err.println("Unknown button interaction or command not found for ID: " + componentId); // Log if command not found
            }
        } catch (Exception e) {
            e.printStackTrace(); // Generic error handling
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
