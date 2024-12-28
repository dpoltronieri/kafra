package com.dpoltronieri.kafra.command;

import java.util.List;

import org.jetbrains.annotations.NotNull;

import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

public interface Command {

    String getName();

    List<String> getButtons();

    List<String> getModals();

    String getDescription();

    List<OptionData> getOptions();

    void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event);

    void onButtonInteraction(@NotNull ButtonInteractionEvent event);

    void onModalInteraction(@NotNull ModalInteractionEvent event);
    
}
