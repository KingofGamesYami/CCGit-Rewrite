package org.kingofgamesyami.ccgit;

import dan200.computercraft.ComputerCraft;
import dan200.computercraft.api.filesystem.IMount;
import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.peripheral.IComputerAccess;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.GitCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.squiddev.cctweaks.api.lua.IExtendedComputerAccess;
import org.squiddev.cctweaks.api.lua.ILuaAPI;
import org.squiddev.cctweaks.api.lua.IMethodDescriptor;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;

public class CCGit implements ILuaAPI, IMethodDescriptor
{
    public static final String MODID = "ccgit";
    public static final String VERSION = "2.1";
    public static final IMount resourceMount = ComputerCraft.createResourceMount( CCGit.class, "ccgit", "lua" );
    public static final String mountDir = ".ccgit";

    private final File computerDir;
    private final IComputerAccess computer;
    private int identifier = 0;

    public int getUniqueID(){
        return identifier++;
    }

    public IComputerAccess getComputer(){
        return computer;
    }

    public CCGit( IExtendedComputerAccess computer ){
        this.computer = computer;
        this.computerDir = computer.getRootMountPath();
    }

    @Override
    public void startup() {
        computer.mount( mountDir, resourceMount );
    }

    @Override
    public void shutdown() {
        computer.unmount( mountDir );
    }

    @Override
    public void advance(double v) {

    }

    @Nonnull
    @Override
    public String[] getMethodNames() {
        return new String[]{"open", "init", "clone"};
    }

    @Nullable
    @Override
    public Object[] callMethod(@Nonnull ILuaContext iLuaContext, int i, @Nonnull Object[] arguments) throws LuaException, InterruptedException {
        switch( i ){
            case 0: //open <gitDir>
                if(arguments.length < 1 || !(arguments[0] instanceof String)){
                    throw new LuaException("Expected string");
                }
                return new Object[]{ new CCGitRepo( getAbsoluteDir( (String)arguments[0] ), this ) };
            case 1: //init <directory>
                if( arguments.length < 1 || !(arguments[0] instanceof String) ){
                    throw new LuaException("Expected string" );
                }
                try {
                    Git.init().setDirectory( getAbsoluteDir( (String)arguments[0] ) ).call();
                } catch (GitAPIException e) {
                    e.printStackTrace();
                }
                return new Object[]{ new CCGitRepo( getAbsoluteDir( (String)arguments[0] ), this ) };
            case 2: //clone <directory> <URL>
                if( arguments.length < 2 || !(arguments[0] instanceof String && arguments[1] instanceof String) ){
                    throw new LuaException( "Expected string, string" );
                }
                return sendToGitThread( iLuaContext, Git.cloneRepository()
                        .setDirectory( getAbsoluteDir( (String)arguments[0] ) )
                        .setURI( (String)arguments[1] ) );
        }

        return new Object[0];
    }

    @Override
    public boolean willYield(int i) {
        return false;
    }

    private File getAbsoluteDir(String localDir ) throws LuaException {
        File result = new File( this.computerDir, localDir );
        File temp = result;
        while( !temp.getAbsolutePath().equals( this.computerDir.getAbsolutePath() ) ){
            temp = temp.getParentFile();
            if( temp.equals( null ) ){
                throw new LuaException( "Attempt to break sandbox with path " + result.getAbsolutePath() );
            }
        }
        return result;
    }


    private Object[] sendToGitThread( ILuaContext context, GitCommand command ) throws LuaException, InterruptedException {
        int thisRequest = this.getUniqueID();
        GitRunnable.instance.queue( new GitRequest( computer, thisRequest, command ) );
        while(true){
            Object[] event = context.pullEvent( "ccgit" );
            if( event.length > 2 && event[1] instanceof Double && (Double)event[ 1 ] == thisRequest ){
                return new Object[]{event[2], (event.length > 3) ? event[3] : null};
            }
        }
    }
}
