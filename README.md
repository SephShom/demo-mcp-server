# demo-mcp-server

A demo **MCP (Model Context Protocol) server** built with **Spring Boot 4.1** and **Spring AI 2.0**.
All data is mocked and in-memory, so every tool is safe to call.

| | |
|---|---|
| Java | 21 |
| Spring Boot | 4.1.2 |
| Spring AI | 2.0.2 (`spring-ai-starter-mcp-server-webmvc`) |
| Build | Maven (wrapper) |

## Exposed MCP tools

Tools are declared with `@McpTool` / `@McpToolParam` annotations and auto-registered by Spring AI.

### `UserService`
| Tool | Input | Description |
|---|---|---|
| `getAllUserList` | – | Full list of the 8 mocked users |
| `getUsers` | `query` (string) | Case-insensitive search over name, email, department, job title |
| `getUserById` | `id` (string) | Single user, errors when the id is unknown |

### `ProductService`
| Tool | Input | Description |
|---|---|---|
| `getAllProducts` | – | Full product catalogue |
| `getProductsByCategory` | `category` (string) | Products of one category (partial match) |
| `getLowStockProducts` | `threshold` (int) | Products with stock ≤ threshold, lowest first |

### `DashboardService`
| Tool | Input | Description |
|---|---|---|
| `getDashboardSummary` | – | Counts, categories and total inventory value |
| `getServerInfo` | – | Server name/version, Java version, transports, tool count |

## Running

```bash
./mvnw spring-boot:run                    # Streamable-HTTP on http://localhost:8080/mcp
./mvnw spring-boot:run -Dspring-boot.run.profiles=sse     # legacy SSE on /sse + /mcp/message
./mvnw spring-boot:run -Dspring-boot.run.profiles=stdio   # STDIO transport (JSON-RPC on stdin/stdout)
```

Without the wrapper, use a locally installed Maven: `mvn spring-boot:run`.

> Spring AI serves **one transport at a time**: `spring.ai.mcp.server.protocol` selects the HTTP
> protocol (SSE / STREAMABLE) and `spring.ai.mcp.server.stdio=true` switches the server to STDIO,
> which disables the HTTP endpoints. That is why each transport has its own run command above.
>
> The `stdio` profile also disables the banner and logs to `logs/demo-mcp-server.log`
> (`logback-stdio.xml`), because stdout is reserved for MCP messages.

## Trying it out

With the server running over HTTP, connect any MCP client, for example the official inspector:

```bash
npx @modelcontextprotocol/inspector http
# transport: Streamable HTTP, URL: http://localhost:8080/mcp
```

Or poke it with plain HTTP:

```bash
curl -sS http://localhost:8080/mcp \
  -H 'Content-Type: application/json' \
  -H 'Accept: application/json, text/event-stream' \
  -d '{"jsonrpc":"2.0","id":1,"method":"initialize","params":{"protocolVersion":"2025-06-18","capabilities":{},"clientInfo":{"name":"curl","version":"1.0"}}}'
```

The response includes the `Mcp-Session-Id` header; send it back (as `Mcp-Session-Id`) on the
following requests:

```bash
# notification first
curl -sS http://localhost:8080/mcp -H 'Content-Type: application/json' \
  -H 'Accept: application/json, text/event-stream' -H 'Mcp-Session-Id: <id>' \
  -d '{"jsonrpc":"2.0","method":"notifications/initialized"}'

# list tools
curl -sS http://localhost:8080/mcp -H 'Content-Type: application/json' \
  -H 'Accept: application/json, text/event-stream' -H 'Mcp-Session-Id: <id>' \
  -d '{"jsonrpc":"2.0","id":2,"method":"tools/list"}'

# call a tool
curl -sS http://localhost:8080/mcp -H 'Content-Type: application/json' \
  -H 'Accept: application/json, text/event-stream' -H 'Mcp-Session-Id: <id>' \
  -d '{"jsonrpc":"2.0","id":3,"method":"tools/call","params":{"name":"getUsers","arguments":{"query":"engineering"}}}'
```

## Connecting from a desktop client (STDIO)

```json
{
  "mcpServers": {
    "demo-mcp-server": {
      "command": "java",
      "args": ["-jar", "/absolute/path/to/demo-mcp-server/target/demo-mcp-server-0.0.1-SNAPSHOT.jar"],
      "env": { "SPRING_PROFILES_ACTIVE": "stdio" }
    }
  }
}
```

Build the jar first: `./mvnw -DskipTests package`.

## Tests

```bash
./mvnw test
```

Unit tests cover the mocked services (search behaviour, unknown ids, empty results).

## Project layout

```
src/main/java/com/example/demo
├── DemoMcpServerApplication.java   # Spring Boot entry point
├── model/                          # User, Product, DashboardSummary, ServerInfo records
└── service/                        # @McpTool methods: UserService, ProductService, DashboardService
src/main/resources
├── application.yml                 # default: Streamable-HTTP
├── application-sse.yml             # profile 'sse'
├── application-stdio.yml           # profile 'stdio'
└── logback-stdio.xml               # file-only logging for the stdio transport
```
