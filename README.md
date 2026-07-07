# OmniBans

OmniBans is a moderation suite for Minecraft networks. It runs as a plugin on Paper, Spigot, or Folia servers and as a server-side mod on Fabric, Forge, and NeoForge, and it comes with two small bridge plugins for Velocity and BungeeCord proxies so a ban or mute issued on one server takes effect everywhere on your network. Thanks for trying it out. <3

If this is your first time setting it up, `INSTALL.md` walks through the whole process step by step.

## License

OmniBans is released under the [MIT License](LICENSE).

## Supported versions

| What | Supported range |
|---|---|
| Minecraft (Paper / Spigot / Folia) | 1.8 through 26.2 |
| Minecraft (Fabric / Forge / NeoForge mods) | 1.21.x |
| Minecraft (Sponge mod) | 1.21.x (SpongeAPI 12) |
| Java | 17 or newer (see note below) |

Folia support only applies to Minecraft versions that Folia itself covers (1.19.4 and later). On older versions the standard Bukkit scheduler is used automatically.

**A note on Minecraft version numbers:** Mojang switched to a year-based versioning format in 2026. Everything in 2026 is numbered 26.1, 26.2, and so on. The last version in the old 1.x format was in the 1.21.x series.

**A note on Java:** The Paper plugin compiles for Java 17, so any server running Java 17 or newer can load it. Servers on 1.18 through 1.20.4 need at least Java 17. Servers on 1.20.5 through 1.21.x need at least Java 21. Servers on 26.1 or 26.2 need Java 25. The Fabric, Forge, NeoForge, and Sponge mod modules target Java 21, which all those loaders require for 1.21.x.

## Getting it running

The project uses Gradle 8.10.2 via the included wrapper. The mod-loader Gradle plugins (fabric-loom, ForgeGradle, NeoGradle, SpongeGradle) are not compatible with Gradle composite builds, so the mod modules are standalone Gradle projects that share this same git repository.

Build the main project with Gradle:

```
./gradlew build
```

When it finishes you will have these jars:

```
paper/build/libs/OmniBans-Paper.jar        → plugins/ folder on every Paper, Spigot, or Folia server
velocity/build/libs/OmniBans-Velocity.jar  → plugins/ folder on Velocity proxy
bungee/build/libs/OmniBans-Bungee.jar      → plugins/ folder on BungeeCord proxy
```

To build a mod jar, `cd` into that module's directory and run `./gradlew build` from there:

```
cd fabric   && ./gradlew build   → fabric/build/libs/OmniBans-Fabric-1.21.x.jar
cd forge    && ./gradlew build   → forge/build/libs/OmniBans-Forge-1.21.x.jar
cd neoforge && ./gradlew build   → neoforge/build/libs/OmniBans-NeoForge-1.21.x.jar
cd sponge   && ./gradlew build   → sponge/build/libs/OmniBans-Sponge.jar
```

Each mod jar goes in your modded server's `mods/` folder.

**Working on mod modules in IntelliJ IDEA:** The `fabric/`, `forge/`, `neoforge/`, and `sponge/` directories are visible in the project tree and fully editable. To get proper Gradle support (code completion, error highlighting) for a specific mod module, open that directory as a separate Gradle project: File > Open > select the module folder. You can have both the main project and a mod module open at the same time in different IntelliJ windows. Alternatively, in the Gradle panel click the `+` button and add the module directory as an additional Gradle project to the current window.

If you only run one Paper server and no proxy, you only need the Paper jar.

## Sponge

The Sponge module targets SpongeAPI 12 for Minecraft 1.21.x servers running SpongeForge or SpongeVanilla. It provides the full set of moderation commands and enforcement, identical to the other mod platforms.

**Installation:** Drop `OmniBans-Sponge.jar` into your Sponge server's `mods/` folder. On first start it creates `config/omnibans/config.properties`. Edit this file to configure your storage backend.

**Commands on Sponge:** All the same commands as Paper are available. Players need the matching permission to use them (e.g. `omnibans.ban`, `omnibans.mute`, `omnibans.admin`). Sponge's permission system (LuckPerms, PermissionsEx, etc.) handles permission assignment the same way as on Paper.

**What it does:**
- Blocks banned players at login with the configured ban screen
- Silences muted players in chat with a notification
- Records the connecting IP address on every join for alt detection
- Full command set: ban, tempban, unban, banip, unbanip, mute, tempmute, unmute, kick, warn, note, alts, history, check, banlist, mutelist, and `/omnibans` reload/version/spy

**What it does not yet do:** The in-game inventory GUI (`/omnibans config`, `/omnibans messages`) is Paper-specific and not available on Sponge. Edit `config.properties` directly for configuration changes.

**Velocity proxy with Sponge backend:** If your Sponge server is behind a Velocity proxy, install the OmniBans Velocity bridge as well. Both the Sponge mod and the Velocity bridge must point at the same MySQL database for cross-server bans to work.

## Storage

By default OmniBans stores everything in a small SQLite file right inside its own folder, which needs nothing installed and nothing set up, it just works the moment you start the server. That is the right choice if you only run one server.

If you run more than one server and want a ban to follow a player between them, every server and your proxy need to point at the same MySQL database instead, set under `storage` in `config.yml` (and the matching section of each proxy's `config.properties` and each mod's `config/omnibans/config.properties`). A MySQL or MariaDB server with that database already created is the only thing you would ever need to install yourself, and only for that network case.

## Talking to your players

Every message OmniBans sends comes from `messages.yml`. You can write them in MiniMessage, plain `&c` ampersand codes, legacy section sign codes, hex codes, or raw chat JSON. The Velocity proxy module also uses MiniMessage. The BungeeCord proxy module uses `&` codes. The mod versions use plain text with newline support for ban screen messages.

## Commands

**Paper / Spigot / Folia:**

`/ban`, `/tempban`, `/unban`, `/banip`, `/unbanip`, `/mute`, `/tempmute`, `/unmute`, `/kick`, `/warn`, `/note`, `/history`, `/check`, `/banlist`, `/mutelist`, `/alts`, `/omnibans reload`, `/omnibans version`, `/omnibans config`, `/omnibans messages`, `/omnibans spy`.

Every command tab-completes properly and only shows suggestions to players who hold the matching permission. Players who do not have permission for a command see nothing when they press tab.

**Fabric / Forge / NeoForge:**

`/ban`, `/tempban`, `/mute`, `/tempmute`, `/unmute`, `/kick`, `/warn`, `/omnibans version`.

All mod commands require at least operator level 2. Commands on the mod platforms do not yet have the full feature set of the Paper plugin (no `/history`, `/banlist`, etc.), since those require the full Paper-specific GUI and cache infrastructure. Bans and mutes applied from mod commands are stored in the same database and are enforced across the whole network if you are using MySQL.

## Editing your configuration without leaving the game

`/omnibans config` and `/omnibans messages` open `config.yml` and `messages.yml` as an in-game inventory menu. A toggle like `redis.enabled` is a single click. Anything else, a number, a piece of text, or a list, closes the menu and asks you to type the new value in chat. Type `cancel` to back out. Every change saves to disk and reloads immediately.

**On the mod platforms** configuration lives at `config/omnibans/config.properties` relative to your server root. There is no in-game GUI there; edit the file directly and restart.

## Keeping config files up to date across updates

On startup OmniBans checks every config file against the bundled defaults. Any key that a new version added is merged in automatically using its default value. Any key that a new version explicitly removed is deleted with a console log. Nothing else is touched, so every custom value you set and any extra key you added yourself are always left alone. A `.bak` backup of the file as it was before the change is written alongside it.

## Protecting players from accidental punishment

These permissions stop anyone from punishing a specific player, regardless of who tries:

- `omnibans.exempt.ban`
- `omnibans.exempt.mute`
- `omnibans.exempt.kick`
- `omnibans.exempt.warn`
- `omnibans.exempt.banip`

None are granted by default. You decide who gets them.

## Taking over from Essentials and vanilla commands

OmniBans claims every moderation command label that Essentials or Minecraft itself would also register, including alternate names like `ban-ip`, `ipban`, `ip-ban`, `pardon`, `pardon-ip`, `silence`, `tban`, and `tmute`. If Essentials is installed OmniBans detects it on startup and re-claims all of them regardless of load order, logging a line to console confirming it did so.

## Staff spy alerts

Whenever a muted player tries to chat, or a banned player tries to join, every operator and every player holding the `*` LuckPerms permission receives a staff alert in chat showing the player's name and, for mute attempts, the message they tried to send. Toggle this with `/omnibans spy` (default: enabled). If DiscordSRV is installed, muted players' chat attempts are also suppressed from the Discord bridge channel so the message never reaches Discord.

## A note about your players' privacy

OmniBans needs to know which address a player connects from in order to enforce IP bans and detect alt accounts, but it will never show that address in any command output, broadcast, or Discord embed. If you run `/check` or `/alts`, you see how many addresses are on record and their ban status, never the addresses themselves.

## Telling your administrators on Discord

There are two separate Discord integrations, and you can use either, both, or neither.

**Simple webhook** (`discord.webhook-url` and `discord.enabled` in `config.yml`): A one-way URL Discord gives you for a specific channel. No bot needed. Posts a plain embed whenever a ban, ip ban, mute, ip mute, kick, or warn happens. Does not include alt account information and cannot mention a role.

**Discord bot** (`discord-bot` section in `config.yml`): Posts the same events as the webhook plus an alt-join alert whenever a player with known alts joins. Every embed includes the player's known alts and which are banned. Can optionally ping a role. Requires you to create a bot in Discord's developer portal and invite it to your server.

```yaml
discord-bot:
  enabled: true
  token: "your bot token"
  server-id: "your discord server id"
  channel-id: "the channel to post in"
  admin-role-id: "role to ping, optional"
```

## The developer API

OmniBans ships an `api` module that third-party plugins and mods can compile against to issue and listen to punishments without depending on the full plugin internals.

**Add OmniBans to your `plugin.yml`:**
```yaml
depend: [OmniBans]
```

**Depend on the API jar at compile time** (add the `OmniBans-Paper.jar` to your compile classpath, or publish the `api` module to your local Maven and reference it).

**Use the API:**
```text
if (OmniBansProvider.isLoaded()) {
    OmniBansApi api = OmniBansProvider.get();

    api.getPunishmentManager().ban(
        PunishmentRequest.builder(ApiPunishmentType.BAN)
            .target(uuid, name)
            .staff(staffUuid, staffName)
            .reason("Hacking")
            .duration("7d")
            .build()
    ).thenAccept(punishment -> {
        if (punishment != null) {
            System.out.println("Banned with id " + punishment.getId());
        }
    });
}
```

**Duration strings** accepted by `.duration(String)`: `30s`, `10m`, `2h`, `7d`, `1w`, and any combination like `1d12h30m`. Duration strings are also accepted anywhere in the API that takes an expiry.

**Listen to punishments before and after they happen:**
```text
api.getEventBus().subscribe(PrePunishmentEvent.class, event -> {
    if (event.getReason().contains("bad word")) {
        event.setCancelled(true);
    }
});

api.getEventBus().subscribe(PostPunishmentEvent.class, event -> {
    System.out.println("Punishment stored: " + event.getPunishment().getId());
});
```

`PrePunishmentEvent` fires before storage and is cancellable. It also lets you change the reason and expiry. `PostPunishmentEvent` fires after storage and reflects the final committed record. Both events fire whether the punishment came from a command or from an API call.

**The API is available on every platform.** On Fabric, Forge, and NeoForge the same `OmniBansProvider.get()` call works from a mod after the OmniBans mod has initialized.

## A note on the async error and lag you may have seen

If your console ever showed a `Thread OmniBans-Worker failed main thread check: player kick` error after issuing a ban, that was a real bug where the player kick and the punishment broadcast were running on a background thread instead of Minecraft's main thread. It has been fixed. The punishments always applied correctly even when the error occurred, but the repeated check failures were genuine overhead, which is why you noticed slight lag. All Bukkit API calls now happen on the main thread.

## A note on the Gradle build

The main project (common, api, paper, velocity, bungee) uses Gradle 8.10.2 via the included wrapper and syncs cleanly. The mod modules (fabric, forge, neoforge, sponge) are standalone Gradle projects — mod-loader Gradle plugins (fabric-loom, ForgeGradle, NeoGradle, SpongeGradle) are not compatible with Gradle composite builds and cannot be included in the main sync. Open each mod module directory separately in IntelliJ to get full Gradle support for it.

Do not upgrade the wrapper to Gradle 9 — IntelliJ IDEA may suggest this via a "New Minor Gradle Version Available" prompt. The mod-loader plugins require Gradle 8.x.

If the build fails with a `org.objectweb.asm.commons.Remapper` error from the Shadow plugin, the fix is to remove `relocate()` calls from the failing `shadowJar {}` block. OmniBans does not use any relocation and the jars build correctly without it.

## A couple of things still on the list

The mod platforms (Fabric, Forge, NeoForge) do not yet have `/history`, `/banlist`, `/mutelist`, or `/alts`, since those depend on the Paper-side cache and GUI infrastructure. The API also does not yet have a publish step to a Maven repository, so depending on it from an external project currently requires manually adding the API jar to your classpath. Both are planned improvements.

## Where this lives

OmniBans is developed at [github.com/Hihelloy-main/OmniBans](https://github.com/Hihelloy-main/OmniBans). Issues and bug reports are welcome. Direct pull requests are not accepted on this repository.
