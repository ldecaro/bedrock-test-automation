package com.example.selenium.command;

//interface that implements the command pattern
public interface Command {

    Command execute(CommandParams params) throws Exception;

    void tearDown() throws Exception;

    Command executeNext(Command c) throws Exception;
}
