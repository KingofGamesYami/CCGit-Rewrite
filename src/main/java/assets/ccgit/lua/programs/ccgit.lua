local tArgs = {... }

local function getRepo()
    return ccgit.open( fs.combine( shell.dir(), ".git" ) )
end

local function getUsernameAndPassword()
    print( "This method requires authentication" )
    write( "Username: " )
    local user = read()
    write( "Password: " )
    return user, read("*")
end

if tArgs[1] == "clone" then
    --do cloning things
    ccgit.clone( shell.dir(), tArgs[ 2 ] );
elseif tArgs[1] == "commit" then
    --do committing things
    local repo = getRepo()
    repo.commit( tArgs[ 2 ] )
elseif tArgs[1] == "init" then
    --do init things
    ccgit.init( shell.dir() )
elseif tArgs[1] == "pull" then
    --do pulling things
    local repo = getRepo()
    repo.pull( tArgs[2] or "origin" )
elseif tArgs[1] == "push" then
    --do pushing things
    local repo = getRepo()
    repo.login( getUsernameAndPassword() )
    repo.push( tArgs[ 2 ] or "origin" )
elseif tArgs[1] == "addRemote" then
    local repo = getRepo()
    repo.addRemote( tArgs[ 2 ], tArgs[ 3 ] )
elseif tArgs[1] == "addAll" then
    local repo = getRepo()
    repo.addAll()
end
