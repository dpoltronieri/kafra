package com.dpoltronieri.kafra.command;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;
import net.dv8tion.jda.api.interactions.modals.ModalMapping;

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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Year;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoField;
import java.util.Collections;
import java.util.List;

@Service
public class RaidCommand implements Command {

    @Autowired
    private DataPersistenceService dataPersistenceService;

    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

    public static final String RAID_MODAL_ID = "raid-modal";
    public static final String RAID_MODAL_NAME = "raid-modal-name";
    public static final String RAID_MODAL_DATE = "raid-modal-date";
    public static final String RAID_MODAL_TIME = "raid-modal-time";
    public static final String RAID_MODAL_DESCRIPTION = "raid-modal-description";
    public static final String RAID_MODAL_SIZE = "raid-modal-size";

    private DateTimeFormatter dateFormatter;

    public RaidCommand() {
        dateFormatter = new DateTimeFormatterBuilder()
            .appendPattern("dd/MM")
            .optionalStart()
            .appendPattern("/yyyy")
            .optionalEnd()
            .parseDefaulting(ChronoField.YEAR, Year.now().getValue())
            .toFormatter();
    }

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
        return Collections.emptyList();
        // return List.of(
        //         new OptionData(OptionType.STRING, "name", "The name of the raid", true),
        //         new OptionData(OptionType.STRING, "date", "The date of the raid (e.g., 2023-12-25)", true),
        //         new OptionData(OptionType.STRING, "time", "The time of the raid (e.g., 18:00)", true),
        //         new OptionData(OptionType.STRING, "description", "A description of the raid", false),
        //         new OptionData(OptionType.INTEGER, "size", "The maximum number of participants", false)
        // );
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (!event.getName().equals(getName())) return;

        // Create the modal
        Modal modal = Modal.create(RAID_MODAL_ID, "Create Raid")
                .addComponents(
                        ActionRow.of(TextInput.create(RAID_MODAL_NAME, "Raid Name", TextInputStyle.SHORT)
                                .setPlaceholder("Enter the name of the raid")
                                .setRequired(true)
                                .build()),
                        ActionRow.of(TextInput.create(RAID_MODAL_DATE, "Date", TextInputStyle.SHORT)
                                .setPlaceholder("Enter the date (dd/MM/yyyy or dd/MM)") // Updated placeholder
                                .setRequired(true)
                                .build()),
                        ActionRow.of(TextInput.create(RAID_MODAL_TIME, "Time", TextInputStyle.SHORT)
                                .setPlaceholder("Enter the time (HH:mm)")
                                .setRequired(true)
                                .build()),
                        ActionRow.of(TextInput.create(RAID_MODAL_DESCRIPTION, "Description", TextInputStyle.PARAGRAPH)
                                .setPlaceholder("Enter a description (optional)")
                                .setRequired(false)
                                .build()),
                        ActionRow.of(TextInput.create(RAID_MODAL_SIZE, "Size Limit", TextInputStyle.SHORT)
                                .setPlaceholder("Enter the size limit (optional)")
                                .setRequired(false)
                                .build())
                )
                .build();

        event.replyModal(modal).queue();
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

    @Override
    public void onModalInteraction(ModalInteractionEvent event) {
        if (!event.getModalId().equals(RAID_MODAL_ID)) return;

        dataPersistenceService.saveDataFromInteraction(event);

        // Retrieve values from the modal
        ModalMapping nameMapping = event.getValue(RAID_MODAL_NAME);
        ModalMapping dateMapping = event.getValue(RAID_MODAL_DATE);
        ModalMapping timeMapping = event.getValue(RAID_MODAL_TIME);
        ModalMapping descriptionMapping = event.getValue(RAID_MODAL_DESCRIPTION);
        ModalMapping sizeMapping = event.getValue(RAID_MODAL_SIZE);

        // Check if required values are present
        if (nameMapping == null || dateMapping == null || timeMapping == null) {
            event.reply("Missing required fields.").setEphemeral(true).queue();
            return;
        }

        String name = nameMapping.getAsString();
        String date = dateMapping.getAsString();
        String time = timeMapping.getAsString();
        String description = descriptionMapping == null ? "" : descriptionMapping.getAsString();
        Integer size = null;
        if (sizeMapping != null && !sizeMapping.getAsString().isEmpty()) {
            try {
                size = Integer.parseInt(sizeMapping.getAsString());
            } catch (NumberFormatException e) {
                event.reply("Invalid size format. Please use a number.").setEphemeral(true).queue();
                return;
            }
        }

        // Validate data
        LocalDateTime raidDateTime = parseDateTime(date, time);
        if (raidDateTime == null) {
            event.reply("Invalid date or time format. Please use dd/MM/yyyy or dd/MM for date and HH:mm for time.").setEphemeral(true).queue();
            return;
        }

        // Create and save the raid
        MemberDTO creator = dataPersistenceService.findOrCreateMember(event.getMember());
        Raid raid = new Raid(event.getIdLong(), name, raidDateTime, description, size, creator);
        raid = dataPersistenceService.saveRaid(raid);

        // Create embed
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setTitle("Raid: " + raid.getName());
        embedBuilder.addField("Date and Time", raid.getDateTime().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")), true);
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

        // Create buttons
        Button signUpButton = Button.success("raid-signup|" + event.getId(), "Sign Up").withEmoji(Emoji.fromUnicode("U+2705"));
        Button maybeButton = Button.secondary("raid-maybe|" + event.getId(), "Maybe").withEmoji(Emoji.fromUnicode("U+2754"));
        Button benchButton = Button.secondary("raid-bench|" + event.getId(), "Bench").withEmoji(Emoji.fromUnicode("U+1F44D"));
        Button withdrawButton = Button.danger("raid-withdraw|" + event.getId(), "Withdraw").withEmoji(Emoji.fromUnicode("U+274C"));

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

    // Helper method to parse date and time
    private LocalDateTime parseDateTime(String date, String time) {
        try {
            LocalDate parsedDate = LocalDate.parse(date, dateFormatter);
            LocalTime parsedTime = LocalTime.parse(time, DateTimeFormatter.ofPattern("HH:mm"));
            return LocalDateTime.of(parsedDate, parsedTime);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    @Override
    public List<String> getButtons() {
        return List.of("raid-signup|", "raid-maybe|", "raid-bench|", "raid-withdraw|");
    }

    @Override
    public List<String> getModals() {
        return List.of(RAID_MODAL_ID);
    }
}