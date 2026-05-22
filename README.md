# Blub-LLM: Private AI Workspace

Blub-LLM is a private AI assistant that bridges your local file system directly with a secure GitHub repository workspace. By mounting a virtual repository path, any text file, script, or document you drop inside is instantly read as context by the AI and used to guide its responses.

---

## 🚀 Features

* **Zero-Manual Cloning:** The app automatically clones your target cloud repository workspace down into your user profile directory on its very first run.
* **Persistent Memory Bank:** Automatically reads local text structures (`.txt`, `.md`, `.json`) from your repository root and your dedicated `ai stuff` folder to answer context questions instantly.
* **One-Time Credential Setup:** Automatically secures your API configuration after the first launch so you never have to re-enter your tokens.
---

## 🛠️ Installation & Setup

### 1. Prerequisites
Before firing up the engine, ensure you have the following components installed on your local host system:
* **Java SDK 17** or higher.
* Your compiled release package (`BlubLLM-1.0-SNAPSHOT.jar`).

### 2. Generate a Hugging Face Token
To connect your assistant to the free cloud inference router:
1. Go directly to your [Hugging Face Tokens Settings Page](https://huggingface.co/settings/tokens).
2. Click **Create new token** (Fine-grained).
3. Under the **Inference** section, enable permissions for `Make calls to Inference Providers`.
4. Generate and copy the token string (it will start with `hf_...`).

### 3. Running the Program
Open your terminal or command prompt in the directory containing your JAR file and execute:

```bash
java -jar BlubLLM-1.0-SNAPSHOT.jar

```

or run the run.bat file