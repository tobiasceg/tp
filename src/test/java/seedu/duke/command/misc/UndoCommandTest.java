package seedu.duke.command.misc;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import seedu.duke.Executor;
import seedu.duke.command.CommandResult;
import seedu.duke.command.add.AddCommand;
import seedu.duke.data.Module;
import seedu.duke.data.ModuleManager;
import seedu.duke.data.StateManager;
import seedu.duke.data.Task;
import seedu.duke.data.TaskManager;
import seedu.duke.data.storage.InputOutputManager;
import seedu.duke.parser.Parser;
import seedu.duke.util.Message;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UndoCommandTest {
    @BeforeEach
    void setupModObjects() {
        InputOutputManager.start();
        StateManager.initialise();
        ModuleManager.clearModules();
        TaskManager.clear();
    }

    @Test
    void undo_addModule_Success() {
        CommandResult result1 = Executor.executeCommand("add -m cs3235");
        assertEquals(1, ModuleManager.getModCodeList().length);
        CommandResult result2 = Executor.executeCommand("add -m cs3230");
        assertEquals(2, ModuleManager.getModCodeList().length);
        new UndoCommand().execute();
        assertEquals(1, ModuleManager.getModCodeList().length);
        CommandResult result3 = Executor.executeCommand("add -m cs1010");
        assertEquals(2, ModuleManager.getModCodeList().length);
    }

    @Test
    void undo_deleteModule_Success() {
        CommandResult result1 = Executor.executeCommand("add -m cs3235");
        assertEquals(1, ModuleManager.getModCodeList().length);
        CommandResult result2 = Executor.executeCommand("add -m cs3230");
        assertEquals(2, ModuleManager.getModCodeList().length);
        new UndoCommand().execute();
        assertEquals(1, ModuleManager.getModCodeList().length);
        CommandResult result3 = Executor.executeCommand("add -m cs1010");
        assertEquals(2, ModuleManager.getModCodeList().length);
        //CommandResult result4 = Executor.executeCommand("del -m cs1010");
        //assertEquals(1, ModuleManager.getModCodeList().length);
        //new UndoCommand().execute();
        //assertEquals(2, ModuleManager.getModCodeList().length);
    }
}