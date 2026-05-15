#  Blub-LLM: Private AI Workspace

Blub-LLM is a local, private AI assistant that bridges your local file system directly with a secure GitHub repository workspace. By mounting a virtual `B:\` drive onto your machine, any text file, script, or document you drop inside is instantly read as context by the AI and pushed safely to your cloud cloud layout.

---

##  Features
* **Zero-Manual Cloning:** The app automatically clones your target cloud repository workspace down into your user profile directory on its very first run.
* **The Virtual Drive (`B:\`):** Auto-mounts a custom local drive map linked to your workspace. Closing or exiting the app securely drops the drive map instantly.
* **Hybrid Code Interpreter:** Automatically reads local text structures (`.txt`, `.md`, `.java`, `.py`, `.bat`, `.html`) to answer context questions instantly.
* **Automatic Cloud Backup:** Every thought, statement, or update processed by the engine is automatically pushed and version-tracked to your remote repository.
* **GPU Ready (Beta):** Includes an experimental vision router built for multimodal processing pipelines.

---

##  Installation & Setup

### 1. Prerequisites
Before firing up the engine, ensure you have the following components installed on your local host system:
* **Java SDK 17** or higher.
* **The Release Package** https://github.com/Codeblub/Blub-Hybrid-LLM/releases

### 2. Prepare the Models 
Open your terminal or command prompt and pull the necessary lightweight models to your local device:
```bash
./ollama pull llama3
./ollama pull llava

```

### 3. Generate a GitHub Token
First clone this repo, then go to your GitHub Settings -> Developer Settings -> Personal Access Tokens (Fine-grained).

Generate a new token with the permissions Contents and selected to the cloned repo. Set the experation date to never (optional). Then copy the token.

### 3. Running the program

Run the run.bat file and give it your token. It will remember and store your token in Appdata/Local/Blub-LLM.

Either you can use the Image/video reader(BETA)

Or

Use the Non GPU one
