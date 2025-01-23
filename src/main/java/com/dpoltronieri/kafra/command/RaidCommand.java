package com.dpoltronieri.kafra.command;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dpoltronieri.kafra.data.MemberDTO;
import com.dpoltronieri.kafra.data.Raid;
import com.dpoltronieri.kafra.service.DataPersistenceService;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.List;

@Service
public class RaidCommand implements Command {

    @Autowired
    private DataPersistenceService dataPersistenceService;

    @Override
    public String getName() {
        return "raid";
    }

    @Override
    public String getDescription() {
        return "Creates a new raid event.";
    }

    @Override
    public List<OptionData> getOptions() {
        return List.of(
                new OptionData(OptionType.STRING, "name", "The name of the raid", true),
                new OptionData(OptionType.STRING, "date", "The date of the raid (e.g., 2023-12-25)", true),
                new OptionData(OptionType.STRING, "time", "The time of the raid (e.g., 18:00)", true),
                new OptionData(OptionType.STRING, "description", "A description of the raid", false),
                new OptionData(OptionType.INTEGER, "size", "The maximum number of participants", false)
        );
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (!event.getName().equals(getName())) return;

        // Save data from interaction at the beginning
        dataPersistenceService.saveDataFromInteraction(event);

        // Retrieve options
        String name = event.getOption("name").getAsString();
        String date = event.getOption("date").getAsString();
        String time = event.getOption("time").getAsString();
        String description = event.getOption("description") == null ? "" : event.getOption("description").getAsString();
        Integer size = event.getOption("size") == null ? null : event.getOption("size").getAsInt();

        // Validate data
        if (!isValidDate(date)) {
            event.reply("Invalid date format. Please use পৌঁ-MM-dd.").setEphemeral(true).queue();
            return;
        }
        if (!isValidTime(time)) {
            event.reply("Invalid time format. Please use HH:mm.").setEphemeral(true).queue();
            return;
        }
        if (size != null && size < 0) {
            event.reply("Invalid size. Please use a positive number.").setEphemeral(true).queue();
            return;
        }

        // Create and save the raid
        MemberDTO creator = dataPersistenceService.findOrCreateMember(event.getMember());
        Raid raid = new Raid(event.getIdLong(), name, date, time, description, size, creator);
        raid = dataPersistenceService.saveRaid(raid);

        // Create embed
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setTitle("Raid: " + raid.getName());
        embedBuilder.addField("Date", raid.getDate(), true);
        embedBuilder.addField("Time", raid.getTime(), true);
        if (raid.getSizeLimit() != null) {
            embedBuilder.addField("Size Limit", String.valueOf(raid.getSizeLimit()), true);
        }
        if (!raid.getDescription().isEmpty()) {
            embedBuilder.addField("Description", raid.getDescription(), false);
        }
        embedBuilder.addField("Confirmed Participants", " ", false);
        embedBuilder.addField("Unconfirmed Participants", " ", false);
        embedBuilder.addField("Benched Participants", " ", false);
        embedBuilder.setFooter("Created by " + event.getUser().getAsTag());

        // Create buttons with unique IDs based on event ID using "|" as separator
        Button signUpButton = Button.success("raid-signup|" + event.getId(), "Sign Up")
                .withEmoji(Emoji.fromUnicode("U+2705"));
        Button maybeButton = Button.secondary("raid-maybe|" + event.getId(), "Maybe")
                .withEmoji(Emoji.fromUnicode("U+2754"));
        Button benchButton = Button.secondary("raid-bench|" + event.getId(), "Bench")
                .withEmoji(Emoji.fromUnicode("U+1F44D"));
        Button withdrawButton = Button.danger("raid-withdraw|" + event.getId(), "Withdraw")
                .withEmoji(Emoji.fromUnicode("U+274C"));

        // Send message
        final Long raidId = raid.getId();
        event.replyEmbeds(embedBuilder.build())
                .addActionRow(signUpButton, maybeButton, benchButton, withdrawButton)
                .queue(hook -> {
                    // Update the raid's event ID using the message ID
                    Raid savedRaid = dataPersistenceService.findRaidById(raidId);
                    if (savedRaid != null) {
                        savedRaid.setEventId(hook.getInteraction().getIdLong());
                        dataPersistenceService.saveRaid(savedRaid);
                    }
                });
    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        // Save data from interaction at the beginning
        dataPersistenceService.saveDataFromInteraction(event);

        String componentId = event.getComponentId();
        String prefix = componentId.substring(0, componentId.indexOf("|"));
        Long eventId = Long.valueOf(componentId.substring(componentId.indexOf("|") + 1));

        switch (prefix) {
            case "raid-signup":
                handleSignUp(event, eventId);
                break;
            case "raid-maybe":
                handleMaybe(event, eventId);
                break;
            case "raid-bench":
                handleBench(event, eventId);
                break;
            case "raid-withdraw":
                handleWithdraw(event, eventId);
                break;
        }
    }

    @Transactional
    private void handleSignUp(ButtonInteractionEvent event, Long eventId) {
        Raid raid = dataPersistenceService.findRaidByEventId(eventId);
        if (raid == null) {
            event.reply("This raid event no longer exists.").setEphemeral(true).queue();
            return;
        }

        MemberDTO member = dataPersistenceService.findOrCreateMember(event.getMember());

        // Add participant as confirmed, remove from others, and update embed
        raid.addConfirmedParticipant(member);
        raid.removeParticipant(member); // Remove from other lists if present
        dataPersistenceService.saveRaid(raid);
        updateRaidEmbed(event, raid);

        event.reply(member.getMemberMention() + ", you've signed up for the raid!").setEphemeral(true).queue();
    }

    @Transactional
    private void handleMaybe(ButtonInteractionEvent event, Long eventId) {
        Raid raid = dataPersistenceService.findRaidByEventId(eventId);
        if (raid == null) {
            event.reply("This raid event no longer exists.").setEphemeral(true).queue();
            return;
        }

        MemberDTO member = dataPersistenceService.findOrCreateMember(event.getMember());

        // Add participant as unconfirmed, remove from others, and update embed
        raid.addUnconfirmedParticipant(member);
        raid.removeParticipant(member); // Remove from other lists if present
        dataPersistenceService.saveRaid(raid);
        updateRaidEmbed(event, raid);

        event.reply(member.getMemberMention() + ", you've marked yourself as 'Maybe' for the raid.").setEphemeral(true).queue();
    }

    @Transactional
    private void handleBench(ButtonInteractionEvent event, Long eventId) {
        Raid raid = dataPersistenceService.findRaidByEventId(eventId);
        if (raid == null) {
            event.reply("This raid event no longer exists.").setEphemeral(true).queue();
            return;
        }

        MemberDTO member = dataPersistenceService.findOrCreateMember(event.getMember());

        // Add participant as benched, remove from others, and update embed
        raid.addBenchedParticipant(member);
        raid.removeParticipant(member); // Remove from other lists if present
        dataPersistenceService.saveRaid(raid);
        updateRaidEmbed(event, raid);

        event.reply(member.getMemberMention() + ", you've been added to the bench for the raid.").setEphemeral(true).queue();
    }
    
    @Transactional
    private void handleWithdraw(ButtonInteractionEvent event, Long eventId) {
        Raid raid = dataPersistenceService.findRaidByEventId(eventId);
        if (raid == null) {
            event.reply("This raid event no longer exists.").setEphemeral(true).queue();
            return;
        }

        MemberDTO member = dataPersistenceService.findOrCreateMember(event.getMember());

        // Remove participant and update embed
        raid.removeParticipant(member);
        dataPersistenceService.saveRaid(raid);
        updateRaidEmbed(event, raid);

        event.reply(member.getMemberMention() + ", you've withdrawn from the raid.").setEphemeral(true).queue();
    }

    private void updateRaidEmbed(ButtonInteractionEvent event, Raid raid) {
        // Fetch the original message embed
        MessageEmbed originalEmbed = event.getMessage().getEmbeds().get(0);
        EmbedBuilder updatedEmbed = new EmbedBuilder(originalEmbed);

        // Update the participants fields
        StringBuilder confirmedParticipantsBuilder = new StringBuilder();
        if (raid.getConfirmedParticipants().isEmpty()) {
            confirmedParticipantsBuilder.append(" ");
        } else {
            for (MemberDTO participant : raid.getConfirmedParticipants()) {
                confirmedParticipantsBuilder.append(participant.getMemberMention()).append("\n");
            }
        }

        StringBuilder unconfirmedParticipantsBuilder = new StringBuilder();
        if (raid.getUnconfirmedParticipants().isEmpty()) {
            unconfirmedParticipantsBuilder.append(" ");
        } else {
            for (MemberDTO participant : raid.getUnconfirmedParticipants()) {
                unconfirmedParticipantsBuilder.append(participant.getMemberMention()).append("\n");
            }
        }

        StringBuilder benchedParticipantsBuilder = new StringBuilder();
        if (raid.getBenchedParticipants().isEmpty()) {
            benchedParticipantsBuilder.append(" ");
        } else {
            for (MemberDTO participant : raid.getBenchedParticipants()) {
                benchedParticipantsBuilder.append(participant.getMemberMention()).append("\n");
            }
        }

        // Update the embed fields
        updatedEmbed.getFields().clear(); // Clear existing fields
        updatedEmbed.addField("Date", raid.getDate(), true);
        updatedEmbed.addField("Time", raid.getTime(), true);
        if (raid.getSizeLimit() != null) {
            updatedEmbed.addField("Size Limit", String.valueOf(raid.getSizeLimit()), true);
        }
        if (!raid.getDescription().isEmpty()) {
            updatedEmbed.addField("Description", raid.getDescription(), false);
        }
        updatedEmbed.addField("Confirmed Participants", confirmedParticipantsBuilder.toString(), false);
        updatedEmbed.addField("Unconfirmed Participants", unconfirmedParticipantsBuilder.toString(), false);
        updatedEmbed.addField("Benched Participants", benchedParticipantsBuilder.toString(), false);

        // Edit the message
        event.editMessageEmbeds(updatedEmbed.build()).queue();
    }

    // Helper methods for validation
    private boolean isValidDate(String date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        dateFormat.setLenient(false); // Make the parsing strict
        try {
            dateFormat.parse(date);
            return true;
        } catch (ParseException e) {
            return false;
        }
    }

    private boolean isValidTime(String time) {
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
        timeFormat.setLenient(false);
        try {
            timeFormat.parse(time);
            return true;
        } catch (ParseException e) {
            return false;
        }
    }

    @Override
    public void onModalInteraction(ModalInteractionEvent event) {
        // Not used in this command
    }

    @Override
    public List<String> getButtons() {
        return List.of("raid-signup|", "raid-maybe|", "raid-bench|", "raid-withdraw|");
    }

    @Override
    public List<String> getModals() {
        return Collections.emptyList();
    }
}