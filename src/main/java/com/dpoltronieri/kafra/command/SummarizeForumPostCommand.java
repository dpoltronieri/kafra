package com.dpoltronieri.kafra.command;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

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
        return "summarize-forum-post"; // Command name
    }

    @Override
    public String getDescription() {
        return "Summarize the messages in the current forum post using Gemini AI."; // Updated description
    }

    // @Override
    // public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
    //     ThreadChannel threadChannel = null; // Declare threadChannel OUTSIDE the if

    //     // Check if the command is used in a ThreadChannel (Forum Post)
    //     if (event.getChannel() instanceof ThreadChannel) {
    //         threadChannel = (ThreadChannel) event.getChannel(); // Assign if it IS a ThreadChannel
    //     } else {
    //         event.reply("This command must be used within a forum post.").setEphemeral(true).queue();
    //         return;
    //     }

    //     if (threadChannel == null) { // Add a null check for extra safety
    //         event.reply("Error: Could not access forum post thread.").setEphemeral(true).queue();
    //         return;
    //     }


    //     event.deferReply(true).queue(); // Defer reply as message fetching might take time
    //     event.getHook().sendMessage("Fetching messages from forum post...").setEphemeral(true).queue(); // Inform user

    //     // Get message history of the thread
    //     threadChannel.getIterableHistory().takeAsync(100).thenAccept(messages -> { // Limit to 100 messages for now
    //         List<String> messageContents = new ArrayList<>();
    //         SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); // For timestamp formatting

    //         System.out.println("--- Forum Post Messages ---");
    //         for (net.dv8tion.jda.api.entities.Message message : messages) {
    //             String formattedMessage = String.format(
    //                     "[%s] %s: %s", // Format: [Timestamp] Author: Message Content
    //                     dateFormat.format(java.util.Date.from(message.getTimeCreated().toInstant())), // Format timestamp
    //                     message.getAuthor().getAsMention(), // Author mention
    //                     message.getContentDisplay()      // Message content
    //             );
    //             messageContents.add(formattedMessage); // Add the formatted message to the list
    //             System.out.println(formattedMessage);    // Print formatted message to console
    //         }
    //         System.out.println("--- End of Messages ---");

    //         event.getHook().sendMessage("Messages fetched. Proceeding to summarization... (Next step)").setEphemeral(true).queue(); // Inform user messages fetched
    //          // In the next steps, we will send messageContents to Gemini API and then reply with summary
    //     }).exceptionally(error -> {
    //         event.getHook().sendMessage("Error fetching messages from forum post.").setEphemeral(true).queue();
    //         System.err.println("Error fetching messages from forum post: " + error.getMessage()); // Basic error output
    //         return null; // Complete exceptionally
    //     });
    // }


        @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        ThreadChannel threadChannel = null;

        if (event.getChannel() instanceof ThreadChannel) {
            threadChannel = (ThreadChannel) event.getChannel();
        } else {
            event.reply("This command must be used within a forum post.").setEphemeral(true).queue();
            return;
        }

        if (threadChannel == null) {
            event.reply("Error: Could not access forum post thread.").setEphemeral(true).queue();
            return;
        }

        event.deferReply(false).queue(); // Defer reply - set ephemeral to false to show summary to everyone
        event.getHook().sendMessage("Fetching messages and summarizing forum post...").setEphemeral(true).queue(); // Inform user - keep ephemeral

        ThreadChannel finalThreadChannel = threadChannel; // Need to be final for lambda
        threadChannel.getIterableHistory().takeAsync(100).thenAccept(messages -> {
            List<String> messageContents = new ArrayList<>();
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            for (net.dv8tion.jda.api.entities.Message message : messages) {
                String formattedMessage = String.format(
                        "[%s] %s: %s",
                        dateFormat.format(java.util.Date.from(message.getTimeCreated().toInstant())),
                        message.getAuthor().getName(), // Use getName() for summary, mention might be too verbose
                        message.getContentDisplay()
                );
                messageContents.add(formattedMessage);
            }
            String combinedMessages = String.join("\n", messageContents); // Combine messages into one string

            // --- Gemini API Call ---
            String summary;
            try {
                summary = geminiAIService.summarizeText(combinedMessages); // Call Gemini API service
            } catch (Exception e) {
                System.err.println("Gemini API Error: " + e.getMessage()); // Error logging
                event.getHook().sendMessage("Error summarizing forum post. Failed to contact Gemini API.").setEphemeral(false).queue(); // Inform user - not ephemeral now
                return; // Exit in case of error
            }


            // --- Create Embed ---
            MessageEmbed embed = new EmbedBuilder()
                    .setTitle("Forum Post Summary")
                    .setDescription(summary)
                    .addField("Feedback", "Was this summary helpful?", false) // Feedback field
                    .setFooter("Summarized by Gemini AI")
                    .build();

            // --- Action Row with Buttons ---
            // ActionRow buttonsActionRow = ActionRow.of(
                    Button agree = Button.success("agree-summary", "👍 Agree"); // Use button IDs from getButtons()
                    Button disagree = Button.danger("disagree-summary", "👎 Disagree");
            // );


            // // --- Reply with Embed and Buttons ---
            // event.replyEmbeds(embed)
            // .addActionRow(agree, disagree)
            // .setEphemeral(false) // Make summary visible to everyone
            // .queue();

            // --- Reply with Embed and Buttons ---
            event.getHook().sendMessageEmbeds(embed)
                    .addActionRow(agree, disagree)
                    .setEphemeral(false) // Make summary visible to everyone
                    .queue();


        }).exceptionally(error -> {
            event.getHook().sendMessage("Error fetching messages from forum post.").setEphemeral(false).queue(); // Not ephemeral on error
            System.err.println("Error fetching messages from forum post: " + error.getMessage());
            return null;
        });
    }

    @Override
    public List<String> getButtons() {
        return List.of("agree-summary", "disagree-summary"); // Button IDs for feedback
    }

    @Override
    public List<String> getModals() {
        return List.of(); // No modals for this command (yet)
    }

    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        // Button interaction handling will be implemented later
    }

    @Override
    public void onModalInteraction(@NotNull ModalInteractionEvent event) {
        // No modal interactions for this command
    }

    @Override
    public List<OptionData> getOptions() {
        return List.of();
    }
}