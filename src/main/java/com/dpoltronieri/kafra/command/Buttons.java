package com.dpoltronieri.kafra.command;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.components.ItemComponent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;

@Service
public class Buttons implements Command {

    @Override
    public String getName() {
        return "button";
    }

    @Override
    public String getDescription() {
        return "Buttons";
    }

    @Override
    public List<OptionData> getOptions() {
        return null;
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setTitle("Pizza?");
        embedBuilder.setDescription("Do you like pizza?");

        Button yesButton = Button.danger("yes-button", "Yes");
        Button noButton = Button.danger("no-button", "No");
        final List<ItemComponent> list = new ArrayList<>();
        list.add(yesButton);
        list.add(noButton);

        MessageCreateData message = new MessageCreateBuilder()
        .setEmbeds(embedBuilder.build())
        .setActionRow(list)
        .build();

        event.reply(message).queue();
    }

}
