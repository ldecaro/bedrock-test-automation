package com.example.selenium.command;

//interface that implements the command pattern
public interface Command {

    public abstract Command execute() throws Exception;

    public abstract void tearDown() throws Exception;

    public abstract Command andThen(Command c) throws Exception;

}
