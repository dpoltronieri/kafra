package com.dpoltronieri.kafra.command;

import net.dv8tion.jda.api.entities.channel.concrete.ForumChannel;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;

import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.util.List;

import static net.dv8tion.jda.api.interactions.commands.OptionType.CHANNEL;
import static net.dv8tion.jda.api.interactions.commands.OptionType.STRING;

@Service
public class CreateForumPostCommand implements Command {

    @Override
    public String getName() {
        return "create-forum-post"; // Command name as it will appear in Discord
    }

    @Override
    public String getDescription() {
        return "Create a new forum post in a specified forum."; // Command description
    }

    @Override
    public List<OptionData> getOptions() {
        return List.of(
                new OptionData(STRING, "title", "The title of the forum post", true), // Required title option
                new OptionData(STRING, "content", "The content of the first post (optional)", false), // Optional content
                new OptionData(CHANNEL, "forum-channel", "The forum channel to create the post in (optional, defaults to current channel)", false) // Optional forum channel
        );
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        String title = event.getOption("title", net.dv8tion.jda.api.interactions.commands.OptionMapping::getAsString);
        String content = event.getOption("content", net.dv8tion.jda.api.interactions.commands.OptionMapping::getAsString);
        ForumChannel forumChannel;

        // Get the ForumChannel. Prioritize channel ID option, then default to the channel command was used in
        if (event.getOption("forum-channel") != null) {
            forumChannel = (ForumChannel) event.getOption("forum-channel", net.dv8tion.jda.api.interactions.commands.OptionMapping::getAsChannel);
            if (forumChannel == null || !(forumChannel instanceof ForumChannel)) {
                event.reply("Invalid Forum Channel provided.").setEphemeral(true).queue();
                return;
            }
        } else {
            if (!(event.getChannel() instanceof ForumChannel)) {
                event.reply("This command must be used in a Forum Channel, or specify a Forum Channel using the 'forum-channel' option.").setEphemeral(true).queue();
                return;
            }
            forumChannel = (ForumChannel) event.getChannel();
        }

        MessageCreateBuilder messageCreateBuilder = new MessageCreateBuilder();
        if (content != null) {
            messageCreateBuilder.setContent(content);
        } else {
            messageCreateBuilder.setContent("New forum post created via bot command."); // Default content if none provided.
        }
        net.dv8tion.jda.api.utils.messages.MessageCreateData messageData = messageCreateBuilder.build();

        event.deferReply(true).queue(); // Defer reply for potentially longer processing

        forumChannel.createForumPost(title, messageData)
                .queue(
                        forumPost -> {
                            event.getHook().sendMessage("Forum post created successfully: " + forumPost.getThreadChannel().getAsMention()).setEphemeral(true).queue();
                        },
                        error -> {
                            event.getHook().sendMessage("Failed to create forum post. An error occurred.").setEphemeral(true).queue();
                            System.err.println("Error creating forum thread in channel " + forumChannel.getId() + ": " + error.getMessage()); //Basic error output
                        }
                );
    }

    @Override
    public List<String> getButtons() {
        return List.of(); // No buttons for this command
    }

    @Override
    public List<String> getModals() {
        return List.of(); // No modals for this command
    }

    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        // No button interactions for this command
    }

    @Override
    public void onModalInteraction(@NotNull ModalInteractionEvent event) {
        // No modal interactions for this command
    }
}