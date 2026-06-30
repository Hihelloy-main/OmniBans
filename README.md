# OmniBans

Welcome! OmniBans is a moderation suite for your Minecraft network. It runs as a plugin on your Paper, Spigot, or Folia server, and it comes with two small bridge plugins for Velocity and BungeeCord so a ban or mute you issue on one server takes effect everywhere on your network. Thanks for trying it out, and we hope it makes running your server a little easier. <3

If this is your first time setting it up, `INSTALL.md` walks through the whole process step by step. Everything below assumes it is already running and is more about what OmniBans can do for you day to day.

## Supported versions

| What | Supported range |
|---|---|
| Minecraft | 1.8 through 26.2 |
| Server software | Spigot, Paper, Folia |
| Proxy software | Velocity, BungeeCord |
| Java | 17 or newer (see note below) |

Folia support only applies to versions of Minecraft that Folia itself covers (1.19.4 and later). On older versions the standard Bukkit scheduler is used automatically, no configuration needed.

**A note on Minecraft version numbers:** Mojang switched to a year-based versioning format starting in 2026. Versions released in 2026 are numbered 26.1, 26.2, and so on, rather than continuing the old 1.x format. The last release in the old format was in the 1.21.x series. So the supported range above goes from 1.8 through the whole 1.x era and on into the new 26.x era.

**A note on Java:** The plugin is compiled targeting Java 17, meaning any server running Java 17 or newer can load it. The right Java version for your server depends on which Minecraft version you run. Servers on 1.18 through 1.20.4 need at least Java 17. Servers on 1.20.5 through 1.21.x need at least Java 21. Servers on 26.1 and 26.2 need Java 25, which can still load plugins compiled for Java 17 since Java is always backward compatible in that direction.

## Getting it running

Build the project with Gradle (`./gradlew build`). You will end up with three jars:

* `paper/build/libs/OmniBans-Paper.jar` goes into the `plugins` folder of every Paper, Spigot, or Folia server you run.
* `velocity/build/libs/OmniBans-Velocity.jar` goes into your Velocity proxy's `plugins` folder.
* `bungee/build/libs/OmniBans-Bungee.jar` goes into your BungeeCord proxy's `plugins` folder.

If you only have one server and no proxy, you only need the Paper jar.

By default OmniBans stores everything in a small SQLite file right inside its own folder, which needs nothing installed and nothing set up, it just works the moment you start the server. That is the right choice if you only run one server. If you run more than one server and want a ban to follow a player between them, every server and your proxy need to point at the same MySQL database instead, set under `storage` in `config.yml` (and the matching section of each proxy's `config.properties`). A MySQL or MariaDB server with that database already created is the only thing you would ever need to install yourself, and only for that network case.

## Talking to your players

Every message OmniBans sends, whether it is a ban screen, a chat warning, or a broadcast, comes from `messages.yml`, and you can write it in whatever style you are most comfortable with. You can use plain ampersand color codes like `&c`, the old section sign codes, MiniMessage tags like `<red>` and `<gradient:#ff0000:#0000ff>`, hex codes written as `&#ff00aa`, or even raw chat JSON if you really want to. OmniBans figures out what you wrote and renders it correctly either way, and gradients work whether you use MiniMessage's own gradient tag or just hand write a run of hex codes.

If a player happens to be on plain Spigot rather than Paper, OmniBans still renders your formatting correctly. Spigot does not have any of Adventure's text system built in at all, so OmniBans quietly converts your message into Spigot's own chat component format before sending it, and the player sees exactly the same colors and styling either way.

## Commands

`/ban`, `/tempban`, `/unban`, `/banip`, `/unbanip`, `/mute`, `/tempmute`, `/unmute`, `/kick`, `/warn`, `/note`, `/history`, `/check`, `/banlist`, `/mutelist`, `/alts`, and `/omnibans reload`, `/omnibans version`, `/omnibans config`, or `/omnibans messages`. Every one of them tab completes properly, so typing `/ban Ste` and pressing tab will offer you online players whose name starts with that, `/tempban` will offer you sensible duration suggestions like `1d` or `7d`, and reasons can be picked from a quick list you control yourself in `config.yml` under `punishments.common-reasons`. A player who does not actually hold the permission for a given command will not see it offer any tab suggestions either, so staff commands stay invisible in that sense to everyone else too.

`/alts <player>` shows you a player's known alt accounts, and tells you whether each alt is currently banned or muted, and whether the address they share is itself banned or muted. If the player you check is an operator, or holds the `*` permission through LuckPerms, OmniBans will simply tell you they are exempt rather than showing anything about them, since staff accounts should not be cross referenced this way.

## Editing your configuration without leaving the game

`/omnibans config` and `/omnibans messages` open `config.yml` and `messages.yml` as an in game menu, one item per setting. A toggle like `redis.enabled` is just a click away from flipping, shown as a green or red item depending on its current state. Anything else, a number, a piece of text, or a list like `punishments.common-reasons`, closes the menu and asks you to type the new value in chat, where typing `cancel` backs out without changing anything. A list is typed as plain text separated by commas, and if you are editing a multi line message template you can type `\n` anywhere you want a line break. Every change saves to disk and takes effect immediately, no need to also run `/omnibans reload` afterward.

## Protecting players from accidental punishment

You can hand a player one of these permissions to make sure nobody, even a trusted moderator having a bad day, can ban, mute, kick, or warn them by mistake:

* `omnibans.exempt.ban`
* `omnibans.exempt.mute`
* `omnibans.exempt.kick`
* `omnibans.exempt.warn`
* `omnibans.exempt.banip`

None of these are given to anyone by default. You decide who gets them.

## Taking over from Essentials and vanilla commands

Minecraft itself ships with its own `/ban`, `/ban-ip`, `/kick`, `/pardon`, and `/pardon-ip`, and EssentialsX has historically provided its own `/mute` and friends too. OmniBans is built to be the one actually handling moderation on your server, so it claims all of these labels for itself, including the two spelled slightly differently than its own commands (`ban-ip` and `pardon`/`pardon-ip`), so a habit formed on another server or a muscle memory vanilla command still reaches OmniBans rather than something else. If Essentials is installed, OmniBans notices this during startup and explicitly re-claims every command the two of you would otherwise both want, regardless of which of you happened to load first, and logs a line to console confirming it did so.

## A note about your players' privacy

OmniBans needs to remember which address a player connects from in order to ban an address and to notice when two accounts share one, but it will never show that address to anyone, in a chat message, in a command's output, or in a Discord message. If you ask `/check` or `/alts` about a player, you will see how many addresses are on record and whether any of them are currently banned or muted, but never the address itself. The only time an address ever appears anywhere is if a staff member types it themselves into a command like `/banip`, and that is their own input, not something OmniBans hands back to them.

We want to be upfront with you here. While building this, a couple of spots slipped through where an address was being shown back to staff in a confirmation message (`/banip`'s success message, `/unbanip`'s success and failure messages, and `/check`'s summary line). Those have all been fixed, `/unbanip` now also accepts a player's name so you rarely need to type an address at all, and we are telling you about it rather than quietly patching it and saying nothing.

A second leak in the same spirit was found afterward. When you typed a bare address straight into `/banip` with no player attached to it, OmniBans was storing that literal address as the punishment's display name, which then surfaced in places meant for player names, the server wide ban broadcast, the Discord webhook, and `/banlist`. That has been fixed at the source, a bare address ban no longer keeps the address as its name at all, and every place that displays a punishment's name now falls back to a generic label instead of ever being able to show an address, even if some future change accidentally tried to. `/banlist` and `/mutelist` were also tightened to operators only by default while we were in there, since a full list of who is banned, even with addresses already kept out of it, is not something every player needs to see.

## Telling your administrators on Discord

OmniBans has two completely separate ways to post to Discord, and you can use either one, both, or neither.

The simple one is a webhook, set through `discord.webhook-url` and `discord.enabled` in `config.yml`. A webhook is just a one way url Discord gives you for a single channel, no bot needed at all. With this turned on, OmniBans posts a plain embed to that channel every time a ban, ip ban, mute, ip mute, kick, or warn happens, naming the player, the staff member, and the reason. A webhook has no concept of mentioning a role and OmniBans does not attempt it, and a webhook never knows anything about alt accounts, it only ever posts that one simple kind of message.

The fuller one is an actual Discord bot, set through the `discord-bot` section. It fires for the exact same events as the webhook, plus a second kind of message: whenever a player who is not an operator and does not hold LuckPerms' `*` permission joins and turns out to have a known alt account, the same situation that triggers the in game staff warning. Every embed the bot sends also tells you the player's known alts and which of those alts are themselves banned, the same way `/alts` does in game, which the plain webhook embed does not include, and it can optionally ping a role so your staff actually notice it. Set this up in `config.yml`:

```yaml
discord-bot:
  enabled: true
  token: "your bot's token"
  server-id: "your discord server's id"
  channel-id: "the channel you want these messages posted in"
  admin-role-id: "the role you want pinged, optional"
```

You will need to invite your own bot to your server and give it permission to send messages and embeds in that channel. As with everything else in OmniBans, neither the webhook nor the bot embed ever contains an address.

## A note on issuing a lot of punishments quickly

If you ever saw a server console error mentioning `AsyncCatcher` and `player kick` right after a ban, alongside a bit of noticeable lag when several punishments happened in quick succession, that was a real bug, not something on your end. A few things OmniBans does after saving a ban or mute, kicking the player and broadcasting the announcement, were running on a background thread instead of the server's main thread, which Minecraft does not allow for actions like that. The punishment itself was always still applied correctly even when this happened, but the repeated error was real overhead, which is exactly the slight lag you may have noticed. This has been fixed, that work now always happens on the main thread the way it is supposed to.

## A quick word on the Gradle build

If your build fails during `shadowJar` with a strange error mentioning `org.objectweb.asm.commons.Remapper`, that is Shadow's package relocation step itself running into trouble with certain Shadow versions, not anything about the code OmniBans actually contains. The build no longer asks Shadow to relocate any package at all, since that relocation step was exactly what kept failing in different ways. Every dependency OmniBans needs still gets bundled straight into each jar, it just keeps its original package name rather than being renamed, which is a perfectly normal way to ship a plugin and only matters if another plugin on your server happens to bundle a conflicting version of the very same library.

## A couple of things still on the list

`/history`, `/banlist`, and `/mutelist` print to chat rather than opening a menu, which works well enough but a proper interface would be nicer one day. There is also no PlaceholderAPI expansion yet for things like a player's ban count. Both would be welcome additions whenever there is time for them.

## Where this lives

OmniBans is developed at [github.com/Hihelloy-main/OmniBans](https://github.com/Hihelloy-main/OmniBans). If you run into something that does not work the way this document says it should, that is the right place to say so. Direct contributions through pull requests are not accepted on this repository, so please do not open one, but issues and bug reports are always genuinely welcome and read. Thanks again for using OmniBans. <3