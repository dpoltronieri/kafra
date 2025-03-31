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
import org.springframework.stereotype.Service;

import com.dpoltronieri.kafra.service.GeminiAIService;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

@Service
public class SummarizeForumPostCommand implements Command {

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
        if (!(event.getChannel() instanceof ThreadChannel)) {
            event.reply("This command must be used within a forum post.").setEphemeral(true).queue();
            return;
        }

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

        if (buttonId.equals("agree-summary")) {
            event.reply("Thank you for agreeing with the summary! 👍").setEphemeral(true).queue();
            // In the future, we could store this feedback
        } else if (buttonId.equals("disagree-summary")) {
            event.reply("Thank you for your feedback. We'll work to improve summaries. 👎").setEphemeral(true).queue();
            // In the future, we could ask for more feedback or offer options to regenerate
        } else {
            // Ignore button clicks from other buttons or commands
            return;
        }
    }

    @Override
    public void onModalInteraction(@NotNull ModalInteractionEvent event) {
        if (!event.getModalId().equals("summarize-options-modal")) {
            return; // Ignore modals from other commands
        }

        ThreadChannel threadChannel = (ThreadChannel) event.getChannel(); // Channel is guaranteed to be a ThreadChannel due to slash command check

        String header = event.getValue("header-input").getAsString(); // Get header input
        String tailer = event.getValue("tailer-input").getAsString(); // Get tailer input

        if (header == null) header = ""; // Handle null if optional input is not filled
        if (tailer == null) tailer = ""; // Handle null if optional input is not filled

        final String finalHeader = header;
        final String finalTailer = tailer;


        event.deferReply(false).queue(); // Defer reply - not ephemeral
        event.getHook().sendMessage("Fetching messages and summarizing forum post...").setEphemeral(true).queue(); // Inform user - ephemeral

        threadChannel.getIterableHistory().takeAsync(100).thenAccept(messages -> {
            List<String> messageContents = new ArrayList<>();
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

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

            System.out.println("--- Forum Post Messages ---");
            System.out.println(combinedMessages);
            System.out.println("--- End of Messages ---");

            String summary;
            try {
                summary = geminiAIService.summarizeText(finalHeader, finalTailer, combinedMessages); // Call overloaded summarizeText
            } catch (Exception e) {
                System.err.println("Gemini API Error: " + e.getMessage());
                event.getHook().sendMessage("Error summarizing forum post. Failed to contact Gemini API.").setEphemeral(false).queue();
                return;
            }

            MessageEmbed embed = new EmbedBuilder()
                    .setTitle("Forum Post Summary")
                    .setDescription(summary)
                    .addField("Feedback", "Was this summary helpful?", false)
                    .setFooter("Summarized by Gemini AI")
                    .build();

            Button agree = Button.success("agree-summary", "👍 Agree");
            Button disagree = Button.danger("disagree-summary", "👎 Disagree");

            event.getHook().sendMessageEmbeds(embed)
                    .addActionRow(agree, disagree) // Use addActionRow here (as per user's working setup)
                    .setEphemeral(false)
                    .queue();


        }).exceptionally(error -> {
            event.getHook().sendMessage("Error fetching messages from forum post.").setEphemeral(false).queue();
            System.err.println("Error fetching messages from forum post: " + error.getMessage());
            return null;
        });
    }

    @Override
    public List<OptionData> getOptions() {
        return List.of();
    }
}