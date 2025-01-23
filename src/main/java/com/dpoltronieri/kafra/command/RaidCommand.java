package com.dpoltronieri.kafra.command;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import com.dpoltronieri.kafra.data.MemberDTO;
import com.dpoltronieri.kafra.data.Raid;
import com.dpoltronieri.kafra.service.DataPersistenceService;
import com.dpoltronieri.kafra.event.RaidSignUpEvent;
import com.dpoltronieri.kafra.event.RaidMaybeEvent;
import com.dpoltronieri.kafra.event.RaidBenchEvent;
import com.dpoltronieri.kafra.event.RaidWithdrawEvent;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.List;

@Service
public class RaidCommand implements Command {

    @Autowired
    private DataPersistenceService dataPersistenceService;

    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

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
        dataPersistenceService.saveDataFromInteraction(event);

        String componentId = event.getComponentId();
        if (componentId == null) return;

        String[] parts = componentId.split("\\|");
        if (parts.length != 2) {
            System.err.println("Invalid button ID format: " + componentId);
            return;
        }

        String prefix = parts[0];
        Long eventId = Long.valueOf(parts[1]);

        switch (prefix) {
            case "raid-signup":
                applicationEventPublisher.publishEvent(new RaidSignUpEvent(event, eventId));
                break;
            case "raid-maybe":
                applicationEventPublisher.publishEvent(new RaidMaybeEvent(event, eventId));
                break;
            case "raid-bench":
                applicationEventPublisher.publishEvent(new RaidBenchEvent(event, eventId));
                break;
            case "raid-withdraw":
                applicationEventPublisher.publishEvent(new RaidWithdrawEvent(event, eventId));
                break;
            default:
                System.err.println("Unknown button interaction: " + componentId);
        }
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