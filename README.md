# CraftCordPlugin

**CraftCordPlugin is a Paper plugin that exposes your Minecraft server through secure HTTP and WebSocket APIs.**

It allows external applications to communicate with your Minecraft server in real time, making it easy to build Discord bots, web dashboards, desktop applications, mobile apps, automation tools, and custom integrations.

CraftCordPlugin is the official backend for the **CraftCord** Python SDK, but its HTTP and WebSocket APIs can be used from **any programming language** capable of making HTTP requests or WebSocket connections.

---

# ✨ Features

- 🌐 HTTP & WebSocket APIs
- 🔐 Bearer Token authentication
- ⚡ Real-time Minecraft events
- 💬 Send chat messages remotely
- 🖥 Execute Minecraft commands
- 👥 Retrieve online player information
- 📊 Access server information
- 👢 Kick and ban players
- 🧵 Thread-safe Paper API integration
- ⚙️ Lightweight configuration
- 🔌 Language-agnostic protocol

---

# 🚀 Perfect For

CraftCordPlugin enables you to build:

- Discord bots
- Web dashboards
- Mobile applications
- Desktop applications
- Moderation tools
- Server management panels
- Monitoring services
- Economy integrations
- Automation systems
- Custom APIs
- Any software capable of making HTTP or WebSocket requests

---

# 📋 Requirements

- Paper **1.21.x**
- Java **21+**

---

# 📦 Installation (You may skip those steps, if you've install Craftcord through Modirinth)

### Modirinth Link: https://modrinth.com/plugin/craftcord-plugin

## 1. Build

```bash
./gradlew clean shadowJar
```

## 2. Install

Copy the generated JAR into your server's `plugins/` folder.

```
plugins/
└── CraftCordPlugin-x.x.x.jar
```

Start the server once.

The plugin will automatically generate:

```
plugins/CraftCordPlugin/config.yml
```

Edit the configuration and restart your server.

---

# ⚙️ Configuration

Example `config.yml`:

```yaml
host: 0.0.0.0
port: 8080

httpBasePath: /api/v1
websocketPath: /ws

apiToken: change-me

enableHttp: true
enableWebSocket: true

logRequests: false
logEvents: false
```

---

# 🔒 Security

CraftCordPlugin authenticates every HTTP request and WebSocket connection using **Bearer Tokens**.

For production environments it is recommended to:

- Generate a strong API token.
- Keep the API behind a firewall or reverse proxy.
- Never expose your token publicly.
- Disable transports you don't use.

---

# 🏗 Architecture

```text
        External Applications

 Discord Bot   Dashboard   Mobile App
        │           │            │
        └───────────┴────────────┘
                    │
            HTTP / WebSocket
                    │
            CraftCordPlugin
                    │
             Minecraft Server
```

---

# 🌐 HTTP API

## Validate Authentication

```
GET /api/v1/auth/validate
```

Headers:

```http
Authorization: Bearer <token>
```

Responses:

| Status | Description |
|---------|-------------|
| 200 | Token is valid |
| 401 | Invalid or missing token |

---

## Remote Procedure Call (RPC)

```
POST /api/v1/rpc
```

Example request:

```json
{
  "action": "minecraft.execute",
  "payload": {
    "command": "say Hello World!"
  }
}
```

Success response:

```json
{
  "status": "ok",
  "data": {
    "success": true
  }
}
```

Error response:

```json
{
  "status": "error",
  "code": "unsupported_action",
  "error": "Unknown action"
}
```

---

# 🔌 WebSocket API

Endpoint:

```
/ws
```

Authentication methods:

- `Authorization: Bearer <token>` during connection
- `auth.validate` after connecting

---

## Request Envelope

```json
{
  "type": "request",
  "id": "uuid",
  "action": "minecraft.get_players",
  "payload": {}
}
```

## Success Response

```json
{
  "type": "response",
  "id": "same-request-id",
  "status": "ok",
  "data": {}
}
```

## Error Response

```json
{
  "type": "response",
  "id": "same-request-id",
  "status": "error",
  "code": "auth_failed",
  "error": "Invalid token"
}
```

---

## Event Example

```json
{
  "type": "event",
  "event": "player_join",
  "data": {
    "player": {
      "uuid": "3b5e4f2d-8e34-4ad1-848f-b9d66fd07a4f",
      "username": "Alex",
      "health": 20.0,
      "world": "world",
      "location": {
        "x": 0.0,
        "y": 64.0,
        "z": 0.0,
        "yaw": 0.0,
        "pitch": 0.0
      }
    }
  }
}
```

---

# ⚡ Supported Actions

Current RPC actions include:

- `auth.validate`
- `minecraft.get_players`
- `minecraft.get_server_info`
- `minecraft.send_message`
- `minecraft.execute`
- `minecraft.kick`
- `minecraft.ban`

Additional actions may be added in future releases while maintaining backwards compatibility.

---

# 📡 Supported Events

CraftCordPlugin streams live Minecraft events over WebSockets.

Built-in events include:

- `player_join`
- `player_leave`
- `player_chat`
- `player_death`
- `server_start`
- `server_stop`

---

# 🐍 Official Python SDK

CraftCordPlugin is designed to work seamlessly with the official **CraftCord** Python SDK.

The SDK provides:

- Async Python client
- Typed Minecraft models
- Event system
- Command framework
- Plugin/extension system
- Built-in `discord.py` adapter

GitHub repositories:

- **CraftCord SDK:** https://github.com/rytisltu09/Craftcord
- **CraftCordPlugin:** https://github.com/rytisltu09/CraftcordPlugin

Although the Python SDK is the official client, **any language capable of making HTTP requests or WebSocket connections can integrate with CraftCordPlugin** by implementing the documented protocol.

---

# 🛠 Development

Run tests:

```bash
./gradlew test
```

Build:

```bash
./gradlew clean build
```

Run a local Paper development server:

```bash
./gradlew runServer
```

---

# 📄 License

MIT License.
