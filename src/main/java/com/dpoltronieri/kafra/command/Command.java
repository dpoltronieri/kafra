package com.dpoltronieri.kafra.command;

import java.util.List;

import org.jetbrains.annotations.NotNull;

import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

public interface Command {

    String getName();
    String getDescription();
    List<OptionData> getOptions();
    void onSlashCommandInteraction(SlashCommandInteractionEvent event);
    void onButtonInteraction(ButtonInteractionEvent event);
    void onModalInteraction(ModalInteractionEvent event);
    List<String> getButtons();
    List<String> getModals();
    
}
