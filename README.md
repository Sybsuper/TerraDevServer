## Terra Pack Dev Server

![Build](https://github.com/Sybsuper/TerraDevServer/actions/workflows/build.yml/badge.svg)

A "lite" server for Terra Pack development.

### Features

- Hot-reloading of Terra Packs: Swap your packs without restarting the server, see changes in a couple seconds.
- Automatic file change detection: Automatically detects changes in pack files and reloads them.
- Player position syncing: Allows you to join with multiple Minecraft clients and see the same thing in different versions of the pack (before/after view) automatically teleports players to the same position so the views will be synced.
- New biome introduction support. Where other Terra development setups (such as testing on a Bukkit server) require a server restart or the creation of a new world (Fabric). This server can detect when a new biome is introduced, and allows for biome additions without disconnecting any clients.

### Demo

A short demo video is available on YouTube: https://youtu.be/UHtOWrxFmwo

A long demo video of someone going through the whole Terra "make a pack from scratch" tutorial using this dev server is available on YouTube: https://youtu.be/Obcb9-BPfhE

### Usage

1. Download the latest release jar from the [releases page](https://github.com/Sybsuper/TerraDevServer/releases);
2. Put this jar in an empty folder for your project;
3. Run the jar in a console `java -jar TerraDevServer-<version>-all.jar`;
4. Put your Terra Pack (unzipped) in the `dev` folder that is generated in the same folder as the jar file;
5. Join the server at address `localhost` in Minecraft;
6. Configuration of the server is done in the `config.json` file (a reboot of the server is required).
7. Use your favourite IDE to open the `dev` folder. You can now edit your pack files and see the changes in-game.

### Commands

Note: Currently, tab completion is not working as intended. This is a known issue and will be fixed in a future release.

Arguments between `<>` are required, arguments between `[]` are optional with a default value. E.g. `/tp <x> <y> <z>` or `/tp [player=me] <x> <y> <z>` would mean that the player is the one executing the command by default and coordinates are required.

- `/reload [target=me]`: manually reloads the pack in the dev folder.
- `/reloadbiomes`: reloads the biome registry for the server.
- `/locate biome <BIOME_ID> [search_radius=100] [step=16]`: finds the biome with the given name in the current world. Results are shown in the chat, and the coordinates will execute the `/tp` command when clicked on.
- `/tp <x> <y> <z>`: teleports the player to the given coordinates.

### Configuration

The server can be configured using the `config.yml` file which will be created automatically if it does not exist.
The default configuration is as follows:

```yaml
# The address to bind to
bindAddress: "0.0.0.0"
# The port to bind to
port: 25565
# The folder to look for dev packs in
devPackFolder: "dev"
# Whether to cycle through players when reload
# this allows for a side-by-side (before and after) view of changes.
cycleThroughPlayers: true
# Whether to sync player positions, when enabled
# all players will be teleported to the same position as the player that first joined the server
syncPlayerPositions: true
# Whether to watch the dev pack directory for changes
# this will automatically reload the dev pack when a file change is detected
watchDevPackDirectory: true
# Set an motd (server list name)
motd: "Terra Dev Server\nMade by: Sybsuper"
# Set a world seed
worldSeed: 0
# Select which commands to enable
enabledCommands:
- "reload"
- "locate"
- "teleport"
- "reregisterbiomes"
# When enabled, players will be automatically reconnected
# when a new biome is introduced after a pack reload.
# When disabled, clients will disconnect when a new biome is introduced.
# This is because the biome registry is not updated until the client reconnects.
fixBiomeDisconnects: true
# Debounce window in milliseconds for file-watch reloads.
# Burst saves within this window trigger only one reload.
watchDebounceMs: 500
# File name patterns to ignore when watching the dev pack directory.
# Supports glob wildcards: * matches within a segment.
watchExcludePatterns:
- "*.swp"
- ".DS_Store"
- "*.md"
- "*~"
```