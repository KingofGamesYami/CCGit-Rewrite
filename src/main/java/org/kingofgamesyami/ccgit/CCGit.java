package org.kingofgamesyami.ccgit;

import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.peripheral.IComputerAccess;
import org.eclipse.jgit.api.Git;
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
    public static final String VERSION = "2.0";
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

    }

    @Override
    public void shutdown() {

    }

    @Override
    public void advance(double v) {

    }

    @Nonnull
    @Override
    public String[] getMethodNames() {
        return new String[]{"open", "init"};
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

}
