# Multi-threaded Dictionary Server

## 1. Problem Context

This project implements a multi-threaded dictionary server that allows multiple clients to access and modify a shared dictionary concurrently. Built with a client-server architecture, it enables clients to search, add, remove, and update words via a graphical user interface (GUI). Concurrent operations by different clients are isolated, and updates are reflected across all users in real-time.

---

## 2. System Components

The system consists of two main parts:

- **Server Side**: Hosts the dictionary and manages client connections.
- **Client Side**: Provides GUI for users to interact with the dictionary server.

Communication between client and server uses TCP sockets to ensure reliable data transmission, with JSON as the data exchange format.

### 2.1 DictionaryServer

- **Dictionary Loading**: On startup, loads a dictionary text file provided via path. Each line is in the format:  
  `word:meaning1;meaning2`  
  where meanings are separated by semicolons.
  
- **Connection Handling**: Listens for incoming client connections via TCP socket. Uses a self-designed worker thread pool to handle each connection with a `ClientHandler` thread, ensuring client isolation and concurrency.

- **Request Processing**: Each `ClientHandler` reads JSON-formatted requests specifying operations (`add`, `search`, `update`, `delete`), target word, and meanings if applicable. It performs the operation on a `HashMap<String, List<String>>` dictionary and sends back JSON responses.

- **Shutdown Persistence**: When the server shuts down, it automatically saves the updated dictionary back to the original file using a JVM shutdown hook.

**Example JSON request:**
```json
{
  "operation": "add",
  "word": "example",
  "meaning": ["a representative form", "a sample"]
}

```

### 2.2 DictionaryClient

- **Connection Management**  
  The `ClientServer` class is responsible for establishing and managing the TCP socket connection with the server.

- **User Interface**  
  The client GUI is built using IntelliJ GUI Designer, providing a user-friendly interface with buttons and text fields that allow users to perform dictionary operations such as search, add, update, and delete words.

- **Asynchronous Response Handling**  
  A dedicated thread, `ClientReader`, runs separately to listen for responses from the server asynchronously. This design prevents the GUI from freezing and allows smooth user interaction.

- **Retry Mechanism**  
  When sending a request, the client will retry up to 5 times if the initial sending fails, improving resilience to temporary network issues.
  
## 3. Overall Class Design and Interaction Diagram

Below is the diagram showing the main classes and how they interact with each other in the system:



