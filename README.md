# MinecraftServerExceptionDiscordSender

Handle exceptions thrown by your Minecraft server and notify them using Discord webhook.

![](assets/discord.png)

## Support platform

- Bukkit
- BungeeCord
- Velocity

## Configuration

### Bukkit / BungeeCord

#### `plugins/MinecraftServerExceptionDiscordSender/config.yml`

```yaml
# Discord Webhook URL
# https://support.discord.com/hc/en-us/articles/228383668-Intro-to-Webhooks
webhook_url: ""
```

### Velocity

#### `plugins/minecraft-server-exception-discord-sender/config.toml`

```toml
# Discord Webhook URL
# https://support.discord.com/hc/en-us/articles/228383668-Intro-to-Webhooks
webhook_url = ""
```

## Releases

### [v1.0.0 (latest)](https://github.com/wanko-zushi/MinecraftServerExceptionDiscordSender/releases/tag/1.0.0)

- `MinecraftServerExceptionDiscordSender.jar` : Support all platforms
- `MinecraftServerExceptionDiscordSender-bukkit.jar` : Support Bukkit only
- `MinecraftServerExceptionDiscordSender-bungee.jar` : Support BungeeCord only
- `MinecraftServerExceptionDiscordSender-velocity.jar` : Support Velocity only
