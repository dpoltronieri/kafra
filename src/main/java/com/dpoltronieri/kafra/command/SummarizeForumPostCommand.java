package com.dpoltronieri.kafra.command;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.dpoltronieri.kafra.service.GeminiAIService;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections; // Added import
import java.util.List;

@Service
public class SummarizeForumPostCommand implements Command {

    private static final Logger log = LoggerFactory.getLogger(SummarizeForumPostCommand.class);
    private final GeminiAIService geminiAIService;

    public SummarizeForumPostCommand(GeminiAIService geminiAIService) {
        this.geminiAIService = geminiAIService;
    }

    @Override
    public String getName() {
        return "summarize-forum-post";
    }

    @Override
    public String getDescription() {
        return "Summarize the messages in the current forum post using Gemini AI, with optional header and tailer.";
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        log.info("Received /summarize-forum-post command from user {} in channel {}", event.getUser().getName(), event.getChannel().getId());
        if (!(event.getChannel() instanceof ThreadChannel)) {
            log.warn("Command /summarize-forum-post used outside a ThreadChannel by user {}", event.getUser().getName());
            event.reply("This command must be used within a forum post thread.").setEphemeral(true).queue();
            return;
        }

        log.debug("Building modal for summarize options for user {}", event.getUser().getName());
        // Build Modal for Header and Tailer
        TextInput headerInput = TextInput.create("header-input", "Optional Header for Summary", TextInputStyle.PARAGRAPH)
                .setPlaceholder("e.g., Summarize key discussion points")
                .setRequired(false) // Optional input
                .build();

        TextInput tailerInput = TextInput.create("tailer-input", "Optional Tailer for Summary", TextInputStyle.PARAGRAPH)
                .setPlaceholder("e.g., Focus on action items")
                .setRequired(false) // Optional input
                .build();

        Modal modal = Modal.create("summarize-options-modal", "Summarize Options")
                .addComponents(ActionRow.of(headerInput), ActionRow.of(tailerInput)) // Add text inputs in ActionRows
                .build();

        event.replyModal(modal).queue(); // Send the modal to the user
    }

    @Override
    public List<String> getButtons() {
        return List.of("agree-summary", "disagree-summary");
    }

    @Override
    public List<String> getModals() {
        return List.of("summarize-options-modal"); // Return the modal ID
    }

    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        String buttonId = event.getButton().getId();
        log.debug("Received button interaction '{}' from user {}", buttonId, event.getUser().getName());

        if (buttonId != null && buttonId.equals("agree-summary")) {
            log.info("User {} agreed with summary.", event.getUser().getName());
            event.reply("Thank you for agreeing with the summary! 👍").setEphemeral(true).queue();
            // Consider disabling buttons after interaction if desired
            // event.editComponents().queue(); 
        } else if (buttonId != null && buttonId.equals("disagree-summary")) {
             log.info("User {} disagreed with summary.", event.getUser().getName());
            event.reply("Thank you for your feedback. We'll work to improve summaries. 👎").setEphemeral(true).queue();
             // Consider disabling buttons after interaction if desired
             // event.editComponents().queue();
        } else {
            log.warn("Received unhandled button interaction ID: {} from user {}", buttonId, event.getUser().getName());
            // Do not reply here, as it might be handled by another command or listener
            return;
        }
    }

    @Override
    public void onModalInteraction(@NotNull ModalInteractionEvent event) {
        if (!event.getModalId().equals("summarize-options-modal")) {
            log.debug("Ignoring modal interaction with ID '{}' as it doesn't match.", event.getModalId());
            return; // Ignore modals from other commands
        }

        log.info("Processing modal 'summarize-options-modal' submitted by user {}", event.getUser().getName());
        ThreadChannel threadChannel = (ThreadChannel) event.getChannel(); // Channel is guaranteed to be a ThreadChannel due to slash command check

        String header = event.getValue("header-input").getAsString(); // Get header input
        String tailer = event.getValue("tailer-input").getAsString(); // Get tailer input

        if (header == null) header = ""; // Handle null if optional input is not filled
        if (tailer == null) tailer = ""; // Handle null if optional input is not filled

        final String finalHeader = header;
        final String finalTailer = tailer;


        event.deferReply(false).queue(); // Defer reply - public
        // Send an ephemeral thinking message
        event.getHook().sendMessage("Fetching messages and preparing summary...").setEphemeral(true).queue();

        log.debug("Fetching history for thread channel {}", threadChannel.getId());
        threadChannel.getIterableHistory().takeAsync(100).thenAccept(messages -> {
            log.info("Fetched {} messages from thread {}", messages.size(), threadChannel.getId());
            List<String> messageContents = new ArrayList<>();
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z"); // Added timezone

            for (Message message : messages) {
                String formattedMessage = String.format(
                        "[%s] %s: %s",
                        dateFormat.format(java.util.Date.from(message.getTimeCreated().toInstant())),
                        message.getAuthor().getName(),
                        message.getContentDisplay()
                );
                messageContents.add(formattedMessage);
            }
            String combinedMessages = String.join("\n", messageContents);

            // Avoid logging potentially large message content unless debugging
            log.debug("Combined messages for summarization (first 100 chars): {}",
                      combinedMessages.substring(0, Math.min(combinedMessages.length(), 100)).replace("\n", " "));

            String summary;
            try {
                log.info("Requesting summary from GeminiAIService for thread {}", threadChannel.getId());
                summary = geminiAIService.summarizeText(finalHeader, finalTailer, combinedMessages); // Call overloaded summarizeText
                log.info("Summary received from GeminiAIService for thread {}", threadChannel.getId());
            } catch (Exception e) {
                // The service itself now logs the error, so we just need user feedback
                log.error("GeminiAIService failed to provide summary for thread {}", threadChannel.getId(), e); // Log context
                event.getHook().editOriginal("Sorry, an error occurred while generating the summary.").setEmbeds(Collections.emptyList()).setComponents(Collections.emptyList()).queue(); // Edit original deferred reply
                return;
            }

            MessageEmbed embed = new EmbedBuilder()
                    .setTitle("Forum Post Summary")
                    .setDescription(summary)
                    .addField("Feedback", "Was this summary helpful?", false)
                    .setFooter("Summarized by Kafra Bot using Gemini AI") // Slightly more specific footer
                    .setTimestamp(event.getTimeCreated()) // Add timestamp of request
                    .build();

            Button agree = Button.success("agree-summary", "👍 Helpful"); // Slightly clearer text
            Button disagree = Button.danger("disagree-summary", "👎 Not Helpful");

            log.debug("Sending summary embed with feedback buttons for thread {}", threadChannel.getId());
            event.getHook().editOriginal("Here is the summary:") // Edit the original deferred reply
                    .setEmbeds(embed)
                    .setComponents(ActionRow.of(agree, disagree)) // Use setComponents with ActionRow
                    .queue();

        }).exceptionally(error -> {
            log.error("Error fetching messages from forum post {}: {}", threadChannel.getId(), error.getMessage(), error);
            event.getHook().editOriginal("Sorry, an error occurred while fetching messages to generate the summary.").setEmbeds(Collections.emptyList()).setComponents(Collections.emptyList()).queue();
            return null;
        });
    }

    @Override
    public List<OptionData> getOptions() {
        return List.of();
    }
}
