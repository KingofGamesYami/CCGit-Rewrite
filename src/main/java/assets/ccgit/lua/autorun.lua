local str = [[--taken from bios.lua
local function completeMultipleChoice( sText, tOptions, bAddSpaces )
    local tResults = {}
    for n=1,#tOptions do
        local sOption = tOptions[n]
        if #sOption + (bAddSpaces and 1 or 0) > #sText and string.sub( sOption, 1, #sText ) == sText then
            local sResult = string.sub( sOption, #sText + 1 )
            if bAddSpaces then
                table.insert( tResults, sResult .. " " )
            else
                table.insert( tResults, sResult )
            end
        end
    end
    return tResults
end
--

shell.setAlias( "git", "ccgit" )
shell.setPath( shell.path() .. ":/.ccgit/programs" )
local options = { "clone", "pull", "push", "commit", "init", "addRemote", "addAll" }
shell.setCompletionFunction( ".ccgit/programs/ccgit.lua", function( shell, nIndex, sText, sPreviousText )
    if nIndex == 1 then
        return completeMultipleChoice( sText, options )
    else
        return {}
    end
end)
]]

if not fs.exists( "/startup/ccgit" ) then
    local file = fs.open( "/startup/ccgit", "w" )
    file.write( str )
    file.close()
end