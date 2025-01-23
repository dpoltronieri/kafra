package com.dpoltronieri.kafra.event;

import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;

public class RaidWithdrawEvent {
    private ButtonInteractionEvent event;
    private Long eventId;

    public RaidWithdrawEvent(ButtonInteractionEvent event, Long eventId) {
        this.event = event;
        this.eventId = eventId;
    }

    public ButtonInteractionEvent getEvent() {
        return event;
    }

    public Long getEventId() {
        return eventId;
    }
}