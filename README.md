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

### Usage

1. Download the latest release jar from the [releases page](https://github.com/Sybsuper/TerraDevServer/releases);
2. Put this jar in an empty folder for your project;
3. Run the jar in a console `java -jar TerraDevServer-<version>-all.jar`;
4. Put your Terra Pack (unzipped) in the `dev` folder that is generated in the same folder as the jar file;
5. Join the server at address `localhost` in Minecraft;
6. Configuration of the server is done in the `config.json` file (a reboot of the server is required).
7. Use your favourite IDE to open the `dev` folder. You can now edit your pack files and see the changes in-game.

### Commands

Currently, the only available command is to type `reload` in chat. This will manually reload the pack.
More commands (actual commands) will be added in the future. 