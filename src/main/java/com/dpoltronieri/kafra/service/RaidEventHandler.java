package com.dpoltronieri.kafra.service;

import com.dpoltronieri.kafra.data.MemberDTO;
import com.dpoltronieri.kafra.data.Raid;
import com.dpoltronieri.kafra.event.RaidBenchEvent;
import com.dpoltronieri.kafra.event.RaidMaybeEvent;
import com.dpoltronieri.kafra.event.RaidSignUpEvent;
import com.dpoltronieri.kafra.event.RaidWithdrawEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;

import java.time.format.DateTimeFormatter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RaidEventHandler {

    @Autowired
    private DataPersistenceService dataPersistenceService;

    @EventListener
    @Transactional
    public void handleRaidSignUpEvent(RaidSignUpEvent event) {
        handleSignUp(event.getEvent());
    }

    @EventListener
    @Transactional
    public void handleRaidMaybeEvent(RaidMaybeEvent event) {
        handleMaybe(event.getEvent());
    }

    @EventListener
    @Transactional
    public void handleRaidBenchEvent(RaidBenchEvent event) {
        handleBench(event.getEvent());
    }

    @EventListener
    @Transactional
    public void handleRaidWithdrawEvent(RaidWithdrawEvent event) {
        handleWithdraw(event.getEvent());
    }

    private void handleSignUp(ButtonInteractionEvent event) {
        String componentId = event.getComponentId();
        if (componentId == null) return;
    
        String[] parts = componentId.split("\\|");
        if (parts.length != 2) {
            System.err.println("Invalid button ID format: " + componentId);
            return;
        }
    
        Long eventId = Long.valueOf(parts[1]);
        Raid raid = dataPersistenceService.findRaidByEventId(eventId);
        if (raid == null) {
            // You can use deferReply here if you want to handle the error with a message
            event.deferReply(true).setContent("This raid event no longer exists.").queue();
            return;
        }
    
        MemberDTO member = dataPersistenceService.findOrCreateMember(event.getMember());
    
        // Add participant as confirmed, remove from others
        raid.addConfirmedParticipant(member);
        dataPersistenceService.saveRaid(raid);
    
        // Update the embed without acknowledging the event
        updateRaidEmbed(event, raid);
    }
    
    private void handleMaybe(ButtonInteractionEvent event) {
        String componentId = event.getComponentId();
        if (componentId == null) return;
    
        String[] parts = componentId.split("\\|");
        if (parts.length != 2) {
            System.err.println("Invalid button ID format: " + componentId);
            return;
        }
    
        Long eventId = Long.valueOf(parts[1]);
        Raid raid = dataPersistenceService.findRaidByEventId(eventId);
        if (raid == null) {
            event.deferReply(true).setContent("This raid event no longer exists.").queue();
            return;
        }
    
        MemberDTO member = dataPersistenceService.findOrCreateMember(event.getMember());
    
        // Add participant as unconfirmed, remove from others
        raid.addUnconfirmedParticipant(member);
        dataPersistenceService.saveRaid(raid);
    
        // Update the embed without acknowledging the event
        updateRaidEmbed(event, raid);
    }
    
    private void handleBench(ButtonInteractionEvent event) {
        String componentId = event.getComponentId();
        if (componentId == null) return;
    
        String[] parts = componentId.split("\\|");
        if (parts.length != 2) {
            System.err.println("Invalid button ID format: " + componentId);
            return;
        }
    
        Long eventId = Long.valueOf(parts[1]);
        Raid raid = dataPersistenceService.findRaidByEventId(eventId);
        if (raid == null) {
            event.deferReply(true).setContent("This raid event no longer exists.").queue();
            return;
        }
    
        MemberDTO member = dataPersistenceService.findOrCreateMember(event.getMember());
    
        // Add participant as benched, remove from others
        raid.addBenchedParticipant(member);
        dataPersistenceService.saveRaid(raid);
    
        // Update the embed without acknowledging the event
        updateRaidEmbed(event, raid);
    }
    
    private void handleWithdraw(ButtonInteractionEvent event) {
        String componentId = event.getComponentId();
        if (componentId == null) return;
    
        String[] parts = componentId.split("\\|");
        if (parts.length != 2) {
            System.err.println("Invalid button ID format: " + componentId);
            return;
        }
    
        Long eventId = Long.valueOf(parts[1]);
        Raid raid = dataPersistenceService.findRaidByEventId(eventId);
        if (raid == null) {
            event.deferReply(true).setContent("This raid event no longer exists.").queue();
            return;
        }
    
        MemberDTO member = dataPersistenceService.findOrCreateMember(event.getMember());
    
        // Remove participant
        raid.removeParticipant(member);
        dataPersistenceService.saveRaid(raid);
    
        // Update the embed without acknowledging the event
        updateRaidEmbed(event, raid);
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
        // Use the combined dateTime field and format it
        updatedEmbed.addField("Date and Time", raid.getDateTime().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")), true);
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
}