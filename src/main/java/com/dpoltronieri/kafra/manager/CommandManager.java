package com.dpoltronieri.kafra.manager;

import com.dpoltronieri.kafra.command.Command;

import java.util.List;

public interface CommandManager {

    void configureCommands(List<Command> commands);
}
