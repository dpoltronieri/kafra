package com.dpoltronieri.kafra.command;

import java.util.List;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

public interface Command {

    String getName();

    String getDescription();

    List<OptionData> getOptions();

    void execute(SlashCommandInteractionEvent event);
    
}
