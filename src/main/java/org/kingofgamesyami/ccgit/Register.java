package org.kingofgamesyami.ccgit;


import dan200.computercraft.api.lua.ILuaAPI;
import dan200.computercraft.api.lua.ILuaAPIProvider;
import dan200.computercraft.api.peripheral.IComputerAccess;

/**
 * Created by Steven on 11/25/2016.
 */
public class Register implements ILuaAPIProvider {
    @Override
    public ILuaAPI getLuaAPI(IComputerAccess iComputerAccess) {
        return new CCGit( iComputerAccess );
    }
}