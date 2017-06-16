package org.kingofgamesyami.ccgit;

import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.ILuaObject;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.peripheral.IComputerAccess;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.GitCommand;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.StoredConfig;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.luaj.vm2.LuaString;
import org.luaj.vm2.LuaTable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;

/**
 * Created by steve on 6/13/2017.
 */
public class CCGitRepo implements ILuaObject {
    private Git git;
    private final IComputerAccess computer;
    private final CCGit APIInstance;
    private UsernamePasswordCredentialsProvider credentials;

    public CCGitRepo( File gitDir, CCGit APIInstance ){
        this.APIInstance = APIInstance;
        this.computer = APIInstance.getComputer();
        FileRepositoryBuilder builder = new FileRepositoryBuilder();
        builder.setGitDir( gitDir );
        try{
            Repository repo = builder.build();
            git = new Git( repo );
        }catch( IOException e ){
            e.printStackTrace();
        }
    }

    @Nonnull
    @Override
    public String[] getMethodNames() {
        return new String[]{"commit", "pull", "push", "addRemote", "getRemoteNames", "addAll", "login", "status", "diff", "fetch", "merge", "stash"};
    }

    @Nullable
    @Override
    public Object[] callMethod(@Nonnull ILuaContext iLuaContext, int i, @Nonnull Object[] arguments) throws LuaException, InterruptedException {
        switch( i ){
            case 0: //commit <message>
                return sendToGitThread( iLuaContext, git.commit().setMessage( (String)arguments[0] ) );
            case 1: //pull <remote>
                if( credentials == null ){
                    throw new LuaException( "You must login to use this function" );
                }else if( arguments.length < 1 || !(arguments[0] instanceof String ) ){
                    throw new LuaException( "Expected string" );
                }
                return sendToGitThread( iLuaContext, git.pull().setRemote( (String)arguments[0]).setCredentialsProvider( credentials ) );
            case 2: //push <remote>
                if( arguments.length < 1 || !(arguments[0] instanceof String ) ){
                    throw new LuaException( "Expected string" );
                }
                return sendToGitThread( iLuaContext, git.push().setRemote( (String)arguments[0] ).setCredentialsProvider( credentials ) );
            case 3: //addRemote <name> <uri>
                if( arguments.length < 2 || !(arguments[0] instanceof String && arguments[ 1] instanceof String ) ){
                    throw new LuaException( "Expected string, string" );
                }
                StoredConfig config = git.getRepository().getConfig();
                config.setString( "remote", (String)arguments[0], "url", (String)arguments[2]);
                try {
                    config.save();
                    return new Object[]{true};
                } catch (IOException e) {
                    return new Object[]{false, e.getMessage()};
                }
            case 4: //getRemoteNames
                return git.getRepository().getRemoteNames().toArray();
            case 5: ///addAll
                try {
                    git.add().addFilepattern(".").call();
                } catch (GitAPIException e) {
                    return new Object[]{false,e.getMessage()};
                }
                return new Object[]{true};
            case 6: //login <username> <password>
                if( arguments.length < 2 || !(arguments[0] instanceof String && arguments[1] instanceof String ) ){
                    throw new LuaException( "Expected string, string" );
                }
                credentials = new UsernamePasswordCredentialsProvider( (String)arguments[0], (String)arguments[1] );
                return new Object[]{true};
            case 7: //status
                LuaTable tbl = new LuaTable();
                Status status;
                try {
                    status = git.status().call();
                } catch (GitAPIException e) {
                    return new Object[]{false, e.getMessage()};
                }
                tbl.set( "added", setToLuaTable( status.getAdded() ) );
                tbl.set( "changed", setToLuaTable( status.getChanged() ) );
                tbl.set( "conflicting", setToLuaTable( status.getConflicting() ) );
                tbl.set( "ignoredNotInIndex", setToLuaTable( status.getIgnoredNotInIndex() ) );
                tbl.set( "missing", setToLuaTable( status.getMissing() ) );
                tbl.set( "modified", setToLuaTable( status.getModified() ));
                tbl.set( "removed", setToLuaTable( status.getRemoved() ) );
                tbl.set( "untracked", setToLuaTable( status.getUntracked() ) );
                tbl.set( "untrackedFolders", setToLuaTable( status.getUntrackedFolders() ) );
                return new Object[]{true, tbl};
            case 8: //diff
                List<DiffEntry> diff;
                try {
                    diff = git.diff().call();
                } catch (GitAPIException e) {
                    return new Object[]{e.getMessage()};
                }
                return diff.toArray();
            case 9: //fetch
                return sendToGitThread( iLuaContext, git.fetch().setCheckFetchedObjects( true ) );
            case 10: //merge
                try {
                    return new Object[]{true, git.merge().call()};
                } catch (GitAPIException e) {
                    return new Object[]{false, e.getMessage()};
                }
        }
        return new Object[0];
    }


    private Object[] sendToGitThread( ILuaContext context, GitCommand command ) throws LuaException, InterruptedException {
        int thisRequest = APIInstance.getUniqueID();
        GitRunnable.instance.queue( new GitRequest( computer, thisRequest, command ) );
        while(true){
            Object[] event = context.pullEvent( "ccgit" );
            if( event.length > 2 && event[1] instanceof Double && (Double)event[ 1 ] == thisRequest ){
                return new Object[]{event[2], (event.length > 3) ? event[3] : null};
            }
        }
    }

    private LuaTable setToLuaTable(Set<String> set ){
        LuaTable tbl = new LuaTable();
        for( String s: set ){
            tbl.add( LuaString.valueOf( s ) );
        }
        return tbl;
    }
}

