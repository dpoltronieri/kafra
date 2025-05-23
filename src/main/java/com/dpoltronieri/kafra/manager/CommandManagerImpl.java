package com.dpoltronieri.kafra.manager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.dpoltronieri.kafra.command.Command;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;

@Service
public class CommandManagerImpl extends ListenerAdapter implements CommandManager {

    private static final Logger log = LoggerFactory.getLogger(CommandManagerImpl.class);

    private List<Command> commands = new ArrayList<>();
    private Map<String, Command> commandMap = new HashMap<>();
    private JDA jda;

    public CommandManagerImpl(@Value("${spring.jda.token}") String token) {
        log.info("Initializing JDA...");
        try {
            this.jda = JDABuilder.createDefault(token)
                    .enableIntents(GatewayIntent.MESSAGE_CONTENT) // Ensure necessary intents are enabled
                    .addEventListeners(this) // Add this class as an event listener
                    .build();
            log.info("JDA Initialization successful. Waiting for Ready event...");
        } catch (Exception e) {
            log.error("Failed to initialize JDA!", e);
            // Consider re-throwing or handling this critical failure appropriately
            throw new RuntimeException("JDA could not be initialized", e);
        }
    }

    @Override
    public void onReady(@NotNull ReadyEvent event) {
        log.info("JDA is Ready! Registering commands for {} guilds.", event.getGuildTotalCount());
        for (Guild guild : event.getJDA().getGuilds()) {
            log.info("Registering commands for guild: {} ({})", guild.getName(), guild.getId());
            for (Command command : commands) {
                try {
                    if (command.getOptions() == null) {
                        guild.upsertCommand(command.getName(), command.getDescription()).queue(
                            success -> log.debug("Upserted command '{}' in guild {}", command.getName(), guild.getId()),
                            error -> log.error("Failed to upsert command '{}' in guild {}", command.getName(), guild.getId(), error)
                        );
                    } else {
                        guild.upsertCommand(command.getName(), command.getDescription()).addOptions(command.getOptions()).queue(
                            success -> log.debug("Upserted command '{}' with options in guild {}", command.getName(), guild.getId()),
                            error -> log.error("Failed to upsert command '{}' with options in guild {}", command.getName(), guild.getId(), error)
                        );
                    }
                } catch (Exception e) {
                    log.error("Error preparing upsert for command '{}' in guild {}", command.getName(), guild.getId(), e);
                }
            }
        }
        log.info("Command registration process initiated for all guilds.");
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        String commandName = event.getName();
        Command command = commandMap.get(commandName);
        if (command != null) {
            log.info("Received slash command interaction: /{} from user {} ({}) in guild {}",
                     commandName, event.getUser().getName(), event.getUser().getId(), event.getGuild() != null ? event.getGuild().getId() : "DM");
            try {
                command.onSlashCommandInteraction(event);
            } catch (Exception e) {
                log.error("Error executing slash command /{}", commandName, e);
                // Provide user feedback about the error
                if (!event.isAcknowledged()) {
                    event.reply("An error occurred while processing your command. Please try again later.").setEphemeral(true).queue();
                } else {
                    event.getHook().sendMessage("An error occurred while processing your command. Please try again later.").setEphemeral(true).queue();
                }
            }
        } else {
            log.warn("Received unknown slash command interaction: /{}", commandName);
            if (!event.isAcknowledged()) {
                event.reply("Unknown command.").setEphemeral(true).queue();
            }
        }
    }

    @Autowired
    public void configureCommands(List<Command> commands) {
        commands.forEach((command) -> {
            this.commands.add(command);
            commandMap.put(command.getName(), command);
            command.getButtons().forEach((button) -> {
                commandMap.put(button, command);
            });
            command.getModals().forEach((modal) -> {
                log.debug("Mapping modal ID '{}' to command '{}'", modal, command.getName());
                commandMap.put(modal, command);
            });
        });
        log.info("Configured and mapped {} commands.", this.commands.size());

        // JDA listener is now added in the constructor
        // jda.addEventListener(this);
    }

    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        try {
            String componentId = event.getComponentId();
            if (componentId == null) return;

            Command command = null;
            log.debug("Received button interaction with component ID: {}", componentId);
            if (componentId.contains("|")) { // Check if componentId contains "|" - indicates complex ID
                String[] parts = componentId.split("\\|"); // Split using |
                if (parts.length >= 2) { // Expecting at least two parts for complex ID format (e.g., command|data or command|action|data)
                    String commandPrefix = parts[0] + "|"; // Reconstruct command prefix (assuming first part identifies the command handler)
                    command = commandMap.get(commandPrefix); // Lookup command by prefix
                    if (command == null) {
                         // Fallback: Maybe the full prefix including action is the key? e.g., "raid|signup|"
                         commandPrefix = parts[0] + "|" + parts[1] + "|";
                         command = commandMap.get(commandPrefix);
                    }
                } else {
                    log.warn("Invalid complex button ID format: {}", componentId); // Log warning for invalid complex ID
                    event.reply("Invalid button interaction.").setEphemeral(true).queue();
                    return;
                }
            } else { // Assume it's a simple button ID if no "|" is present
                command = commandMap.get(componentId); // Directly lookup command by componentId (simple ID)
            }

            if (command != null) {
                log.info("Dispatching button interaction '{}' to command '{}' for user {}", componentId, command.getName(), event.getUser().getName());
                command.onButtonInteraction(event); // Dispatch button interaction to the command
            } else {
                log.warn("Unknown button interaction or command not found for ID: {}", componentId); // Log if command not found
                if (!event.isAcknowledged()) {
                    event.reply("This button seems to be outdated or invalid.").setEphemeral(true).queue();
                }
            }
        } catch (Exception e) {
            log.error("Error processing button interaction with ID: {}", event.getComponentId(), e); // Generic error handling
            if (!event.isAcknowledged()) {
                event.reply("An error occurred while handling this button click.").setEphemeral(true).queue();
            } else {
                 event.getHook().sendMessage("An error occurred while handling this button click.").setEphemeral(true).queue();
            }
        }
    }

    @Override
    public void onModalInteraction(@NotNull ModalInteractionEvent event) {
        String modalId = event.getModalId();
        Command command = commandMap.get(modalId);
        if (command != null) {
             log.info("Received modal interaction with ID: {} from user {} ({})",
                     modalId, event.getUser().getName(), event.getUser().getId());
            try {
                command.onModalInteraction(event);
            } catch (Exception e) {
                log.error("Error processing modal interaction with ID: {}", modalId, e);
                 if (!event.isAcknowledged()) {
                    event.reply("An error occurred while processing your submission.").setEphemeral(true).queue();
                } else {
                     event.getHook().sendMessage("An error occurred while processing your submission.").setEphemeral(true).queue();
                }
            }
        } else {
            log.warn("Received unknown modal interaction with ID: {}", modalId);
             if (!event.isAcknowledged()) {
                 event.reply("Unknown or outdated form submission.").setEphemeral(true).queue();
             }
        }
    }
}
