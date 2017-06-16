package org.kingofgamesyami.ccgit;

import dan200.computercraft.api.peripheral.IComputerAccess;
import org.eclipse.jgit.api.CommitCommand;
import org.eclipse.jgit.api.GitCommand;
import org.eclipse.jgit.api.errors.GitAPIException;

/**
 * Created by Steven on 11/27/2016.
 */
public class GitRequest {
    private GitCommand gitCommand;
    private IComputerAccess computer;
    private int identifier;

    private GitRequest( IComputerAccess computer, int identifier ){
        this.computer = computer;
        this.identifier = identifier;
    }

    public GitRequest( IComputerAccess computer, int identifier, GitCommand gitCommand ){
        this( computer, identifier );
        this.gitCommand = gitCommand;
    }

    public void call() throws GitAPIException {
        gitCommand.call();
    }

    public IComputerAccess getComputer() {
        return computer;
    }

    public int getIdentifier() {
        return identifier;
    }
}