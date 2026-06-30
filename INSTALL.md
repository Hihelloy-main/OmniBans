# Setting up OmniBans

This walks you through getting OmniBans running, whether you have one server or a whole network behind a proxy. Take it slowly the first time and you will not have any trouble. <3

## What you will need

* Java 17 or newer to build the project. The right Java version to run the server itself depends on which Minecraft version you host: 1.18 through 1.20.4 need Java 17, 1.20.5 through 1.21.x need Java 21, and 26.1 or 26.2 need Java 25. All of those can load this plugin since it is compiled for Java 17 and Java is always backward compatible in that direction.
* A Paper, Spigot, or Folia server on Minecraft 1.21. Paper is recommended since it gives you a little extra reliability around startup, but plain Spigot works fine too.
* If you run more than one server, a Velocity or BungeeCord proxy in front of them.
* Nothing at all for the database if you only run one server. OmniBans uses SQLite by default, which means it just creates its own file and needs no separate software, no installing, no setup. MySQL or MariaDB only comes into play if you run more than one server and want a ban to follow a player between them, and even then it is the only piece on this whole list you would need to install yourself.
* LuckPerms is optional, but it is worth installing. Without it, OmniBans can still tell whether an online player is exempt from something, but it cannot check an offline player's permissions at all.
* Redis is optional too. It makes a ban or mute take effect within a second anywhere on your network rather than within about ten seconds, which is how often OmniBans checks anyway even without it.

## Building it

From the root of the project, run:

```
./gradlew build
```

The first build will take a little while since Gradle needs to pull down every dependency. When it finishes, you will find three jars:

```
paper/build/libs/OmniBans-Paper.jar
velocity/build/libs/OmniBans-Velocity.jar
bungee/build/libs/OmniBans-Bungee.jar
```

You only ever need the ones that match what you are actually running. A single server with no proxy only needs the Paper jar.

## Putting it on a single server

1. Copy `OmniBans-Paper.jar` into that server's `plugins` folder.
2. Start the server once so OmniBans can generate `config.yml` and `messages.yml` inside `plugins/OmniBans`.
3. That is genuinely it. SQLite is already selected by default, OmniBans already created its own database file in that same folder when it started, and there is nothing further to install or configure unless you want to change something like the prefix or the messages. Every command in the README works right away.

## Putting it on a network

1. Copy `OmniBans-Paper.jar` into the `plugins` folder of every backend server.
2. Copy `OmniBans-Velocity.jar` or `OmniBans-Bungee.jar` into your proxy's `plugins` folder, whichever one you actually run.
3. Start everything once so each one generates its own config.
4. Set every server's `config.yml`, and your proxy's `config.properties`, to point at the very same MySQL database. This is the part that actually makes a ban follow a player from one server to the next, so do not skip it or leave any server pointed at SQLite by accident.
5. If you want a ban to take effect within a second rather than within about ten seconds, turn `redis.enabled` to true on every server and your proxy, pointed at the same Redis instance.
6. Restart everything.

## Setting up your database, only if you run a network

If you only run one server, skip this section entirely, SQLite is already doing its job with nothing for you to do. If you do run a network, inside `config.yml`, under `storage`, set `type` to `MYSQL` and fill in `host`, `port`, `database`, `username`, and `password`. The database itself just needs to exist already, OmniBans creates its own tables inside it the first time it connects.

## Setting up permissions

Every command in OmniBans has its own permission, listed in the README, and most default to operators only. The two permissions worth knowing about right away are `omnibans.alerts.alts`, which lets a non operator staff member see the in game warning when a player with a known alt joins, and the `omnibans.exempt.*` permissions, which protect a specific player from being banned, muted, kicked, or warned by mistake. None of the exempt permissions are given to anyone automatically, you decide who gets them.

## Setting up Discord, if you want it

There are two separate ways to hear about bans on Discord, and you can use either, both, or neither.

The simpler one is `discord.webhook-url` in `config.yml`. Create a webhook in your Discord channel's settings, paste the url in, and turn `discord.enabled` to true. That is the whole setup.

The fuller one is `discord-bot`, which also tells you about a player's alt accounts and can ping a role. For this one you will need to create your own bot in Discord's developer portal, invite it to your server with permission to send messages and embeds, and fill in:

```yaml
discord-bot:
  enabled: true
  token: "your bot's token, from the developer portal"
  server-id: "your discord server's id"
  channel-id: "the channel you want these posted in"
  admin-role-id: "a role to ping, this one is optional"
```

To find a channel or server id, turn on Developer Mode in your own Discord settings, then right click the server or channel and choose Copy ID.

## Checking that it actually works

Once everything is running, try this on your server:

1. Run `/ban` on a test account, or on yourself from another account if you have one.
2. Run `/check` on that same name. You should see their ban status, how many addresses are on record for them, and how many warnings they have.
3. Run `/unban` on them to lift it again.

If all three of those work, your setup is good.

## If your build will not finish

The repositories OmniBans needs are already declared in the root `build.gradle.kts`, so a fresh checkout should just work. If you do hit a build failure, the README has a note about a `org.objectweb.asm.commons.Remapper` error some versions of the Shadow plugin produce, which is the only build issue we have actually run into ourselves, and which is why the build no longer asks Shadow to relocate any package at all.
