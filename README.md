## Terra Pack Dev Server

A "lite" server for Terra Pack development.

### Features

- Hot-reloading of Terra Packs: Swap your packs without restarting the server, see changes in a couple seconds.
- Automatic file change detection: Automatically detects changes in pack files and reloads them.
- Player position syncing: Allows you to join with multiple minecraft clients and see the same thing in different versions of the pack (before/after view) automatically teleports players to the same position so the views will be synced.

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
