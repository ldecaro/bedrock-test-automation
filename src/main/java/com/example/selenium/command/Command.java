package com.example.selenium.command;

//interface that implements the command pattern
public interface Command {

    void execute(CommandParams params) throws Exception;
}
