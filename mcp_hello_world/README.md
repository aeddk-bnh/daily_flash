# Hello World MCP Server

This is a minimal MCP server implementation in Python.

## Prerequisites

- Python 3.10+
- `mcp` library

## Installation

```bash
pip install mcp[cli]
```

## Running Manually

You can test the server using the MCP Inspector (a web-based debugger):

```bash
npx @modelcontextprotocol/inspector git-mcp-server-test.py
```
*Note: The above command is for the inspector. To run YOUR server:*

```bash
# Verify it runs (it will listen on stdio and might appear to hang, use Ctrl+C to exit)
python server.py
```

## IDE Configuration

To use this server in your IDE, add the following configuration to your MCP settings file.

### Windsurf
File: `~/.codeium/windsurf/mcp_config.json` (or `%USERPROFILE%\.codeium\windsurf\mcp_config.json` on Windows)

```json
{
  "mcpServers": {
    "hello-world": {
      "command": "python",
      "args": ["D:\\check_app\\mcp_hello_world\\server.py"]
    }
  }
}
```

### Cursor
1. Go to **Settings** > **Features** > **MCP**.
2. Click **Add New Server**.
3. Name: `hello-world`
4. Type: `stdio`
5. Command: `python`
6. Args: `D:\check_app\mcp_hello_world\server.py`

### VS Code
If using the MCP extension:
Edit your settings JSON or the extension's `mcp-server.config.json` (location varies by extension).

```json
{
  "mcpServers": {
    "hello-world": {
      "command": "python",
      "args": ["D:\\check_app\\mcp_hello_world\\server.py"]
    }
  }
}
```

## Usage
Once configured, restart your IDE or hit "Refresh MCP Servers".
Ask the AI Agent:
> "Please add 40 and 2 using the hello-world tool."
