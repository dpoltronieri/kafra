package com.dpoltronieri.kafra.command;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dpoltronieri.kafra.service.DataPersistenceService;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.components.ItemComponent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;

@Service
public class Pizza implements Command {

    @Autowired
    private DataPersistenceService dataPersistenceService;

    @Override
    public String getName() {
        return "pizza"; // Changed command name to "pizza"
    }

    @Override
    public List<String> getButtons() {
        return List.of("yes-button", "no-button");
    }

    @Override
    public List<String> getModals() {
        return Collections.emptyList(); // No modals for this command
    }

    @Override
    public String getDescription() {
        return "Asks you if you like pizza"; // Updated description
    }

    @Override
    public List<OptionData> getOptions() {
        return null; // No options for this command
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        try {
            if (!event.getName().equals(getName())) {
                return; // Not our command, ignore
            }
            System.out.println("Received slash command interaction for /" + getName());
            dataPersistenceService.saveDataFromInteraction(event);

            EmbedBuilder embedBuilder = new EmbedBuilder();
            embedBuilder.setTitle("Pizza?");
            embedBuilder.setDescription("Do you like pizza?");

            // Using different button styles for variety and adding emojis
            Button yesButton = Button.success("yes-button", "Yes").withEmoji(Emoji.fromUnicode("U+1F355")); // Pizza emoji
            Button noButton = Button.secondary("no-button", "No").withEmoji(Emoji.fromUnicode("U+1F615")); // Confused face emoji

            final List<ItemComponent> list = new ArrayList<>();
            list.add(yesButton);
            list.add(noButton);

            MessageCreateData message = new MessageCreateBuilder()
                    .setEmbeds(embedBuilder.build())
                    .setActionRow(list)
                    .build();

            event.reply(message).queue(
                success -> System.out.println("Successfully replied to slash command /" + getName()),
                error -> System.err.println("Failed to reply to slash command /" + getName() + ". Error: " + error.getMessage())
            );

        } catch (Exception e) {
            System.err.println("Error handling slash command interaction for /" + getName() + ". Error: " + e.getMessage());
            e.printStackTrace();
            // Consider sending a generic error message to the user
            if (!event.isAcknowledged()) {
                 event.reply("An unexpected error occurred.").setEphemeral(true).queue();
            } else {
                 event.getHook().sendMessage("An unexpected error occurred.").setEphemeral(true).queue();
            }
           
        }
    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        try {
            System.out.println("Received button interaction for button: " + event.getButton().getId());
            if (event.getButton().getId() == null) {
                return;
            }
            switch (event.getButton().getId()) {
                case "yes-button":
                    event.reply("Nice, so do I!  \uD83C\uDF55") // Pizza emoji
                            .setEphemeral(true) // Only the user sees this
                            .queue(
                                success -> System.out.println("Replied to yes-button interaction"),
                                error -> System.err.println("Failed to reply to yes-button interaction. Error: " + error.getMessage())
                            );
                    break;
                case "no-button":
                    event.reply("What?! You monster! \uD83D\uDE21") // Angry face emoji
                            .setEphemeral(true)
                            .queue(
                                success -> System.out.println("Replied to no-button interaction"),
                                error -> System.err.println("Failed to reply to no-button interaction. Error: " + error.getMessage())
                            );
                    break;
                default:
                    System.out.println("Unknown button interaction: " + event.getButton().getId());
                    event.reply("I don't know what to do with that button.").setEphemeral(true).queue();
            }
        } catch (Exception e) {
            System.err.println("Error handling button interaction. Error: " + e.getMessage());
            e.printStackTrace();
            // Consider sending a generic error message to the user
             if (!event.isAcknowledged()) {
                 event.reply("An unexpected error occurred.").setEphemeral(true).queue();
            } else {
                 event.getHook().sendMessage("An unexpected error occurred.").setEphemeral(true).queue();
            }
        }
    }

    @Override
    public void onModalInteraction(ModalInteractionEvent event) {
        // No modals for this command, nothing to do here
        System.out.println("Received unexpected modal interaction for command: " + getName());
    }
}