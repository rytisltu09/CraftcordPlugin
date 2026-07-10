# CraftCordPlugin

CraftCordPlugin is a **Paper-only** Minecraft server plugin that exposes authenticated HTTP and WebSocket APIs for the CraftCord Python SDK.

It is the Minecraft-side bridge in this architecture:

Paper Server -> CraftCordPlugin -> HTTP/WebSocket API -> CraftCord Python SDK -> Discord bots and automation apps

## Features

- Paper-native plugin targeting Java 21 and modern Paper APIs
- Authenticated HTTP and WebSocket endpoints for the existing Python SDK
- Action RPC layer for Minecraft server operations
- Live event streaming to authenticated WebSocket clients
- Main-thread safe execution for all Paper API access
- Clean separation between transport, auth, routing, and Minecraft service logic

## Requirements

- Paper 1.21.x
- Java 21+

## Installation

1. Build the plugin jar:

```bash
./gradlew clean shadowJar
```

2. Copy `build/libs/CraftCordPlugin-<version>.jar` into your Paper server `plugins/` directory.
3. Start the server once to generate `plugins/CraftCordPlugin/config.yml`.
4. Edit `apiToken` in the config to a strong secret value.
5. Restart the server.

## Configuration

`config.yml`:

```yaml
# local = localhost only, global = network-accessible on all interfaces
bindMode: local
# Optional explicit host override (for example: 192.168.1.50)
host: ""
port: 8080
websocketPath: /ws
httpBasePath: /api/v1
apiToken: change-me
enableHttp: true
enableWebSocket: true
logRequests: false
logEvents: false
```

- Set `bindMode: local` to keep the API reachable only from the same machine.
- Set `bindMode: global` to allow LAN/WAN access (use firewall/reverse proxy rules).
- Use `host` only when you want to bind to a specific interface IP.

### Security notes

- Always set a strong `apiToken` in production.
- Use a firewall or reverse proxy to restrict API exposure.
- Never share bearer tokens in logs or screenshots.

## HTTP API

### Validate token

`GET /api/v1/auth/validate`

Header:

```text
Authorization: Bearer <token>
```

- `200` when token is valid
- `401` when token is invalid

### RPC endpoint

`POST /api/v1/rpc`

Header:

```text
Authorization: Bearer <token>
```

Request:

```json
{
  "action": "minecraft.execute",
  "payload": {
    "command": "say hello"
  }
}
```

Success:

```json
{
  "status": "ok",
  "data": {
    "success": true
  }
}
```

Failure:

```json
{
  "status": "error",
  "code": "unsupported_action",
  "error": "Unknown action"
}
```

## WebSocket API

Endpoint: `/ws`

Authentication:

- Preferred: `Authorization: Bearer <token>` header on connect
- Also supported: `auth.validate` action after connection

Request envelope:

```json
{
  "type": "request",
  "id": "uuid",
  "action": "minecraft.get_players",
  "payload": {}
}
```

Success envelope:

```json
{
  "type": "response",
  "id": "same-request-id",
  "status": "ok",
  "data": {}
}
```

Error envelope:

```json
{
  "type": "response",
  "id": "same-request-id",
  "status": "error",
  "code": "auth_failed",
  "error": "Invalid token"
}
```

Event envelope:

```json
{
  "type": "event",
  "event": "player_join",
  "data": {
    "player": {
      "uuid": "uuid-string",
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

## Supported actions

- `auth.validate`
- `minecraft.send_message`
- `minecraft.execute`
- `minecraft.get_players`
- `minecraft.get_server_info`
- `minecraft.kick`
- `minecraft.ban`

## Supported events

- `player_join`
- `player_leave`
- `player_chat`
- `player_death`
- `server_start`
- `server_stop`

## Development

Run tests:

```bash
./gradlew test
```

Build plugin:

```bash
./gradlew clean build
```

Run local Paper test server:

```bash
./gradlew runServer
```

