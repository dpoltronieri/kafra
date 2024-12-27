package com.dpoltronieri.kafra.manager;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.dpoltronieri.kafra.command.Command;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

@Service
public class CommandManagerImpl extends ListenerAdapter {

    private List<Command> commands = new ArrayList<>();

    @Value("${spring.jda.token}")
    private String token;

    private JDA jda = JDABuilder.createDefault("TOKEN").build();

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
        for(Command command : commands) {
            if(command.getName().equals(event.getName())) {
                command.execute(event);
                return;
            }
        }
    }

    public void add(Command command) {
        commands.add(command);
    }

    @Autowired
    public void configureSlashCommands(List<Command> commands) {
        this.commands = commands;
        jda.addEventListener(this);
    }
}
