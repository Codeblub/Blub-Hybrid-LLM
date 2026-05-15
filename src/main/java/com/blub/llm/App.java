package com.blub.llm;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ImageContent;
import dev.langchain4j.data.message.TextContent;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.model.output.Response;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.file.*;
import java.time.Duration;
import java.util.Base64;
import java.util.Scanner;

public class App {
    private static String GITHUB_TOKEN;
    private static final String REPO_URL = "https://github.com/Codeblub/Blub-LLM.git";
    private static final String APP_DATA_DIR = System.getenv("LOCALAPPDATA") + "\\Blub-LLM";
    private static final String CONFIG_FILE = APP_DATA_DIR + "\\token.txt";
    private static final String LOCAL_REPO_DIR = System.getProperty("user.home") + "\\BlubRepo";

    public static void main(String[] args) throws Exception {
        Scanner scanner = new Scanner(System.in);
        handleToken(scanner);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                Runtime.getRuntime().exec("subst B: /d");
            } catch (Exception ignored) {}
        }));

        File repoDir = new File(LOCAL_REPO_DIR);
        if (!repoDir.exists()) {
            System.out.println(">> Cloning GitHub Repository...");
            Git.cloneRepository().setURI(REPO_URL).setDirectory(repoDir)
                .setCredentialsProvider(new UsernamePasswordCredentialsProvider(GITHUB_TOKEN, "")).call();
        }

        File brainDriveFolder = new File(repoDir, "BrainDrive");
        if (!brainDriveFolder.exists()) brainDriveFolder.mkdirs();

        if (System.getProperty("os.name").contains("Windows")) {
            Runtime.getRuntime().exec("subst B: /d");
            Thread.sleep(500);
            Runtime.getRuntime().exec("subst B: \"" + brainDriveFolder.getAbsolutePath() + "\"");
            System.out.println(">> Virtual Drive B: Mounted.");
            Thread.sleep(500);
            Runtime.getRuntime().exec("explorer.exe B:\\");
        }

        OllamaChatModel textModel = OllamaChatModel.builder()
                .baseUrl("http://localhost:11434")
                .modelName("llama3")
                .timeout(Duration.ofSeconds(60))
                .build();

        OllamaChatModel visionModel = OllamaChatModel.builder()
                .baseUrl("http://localhost:11434")
                .modelName("llava")
                .timeout(Duration.ofSeconds(90))
                .build();

        System.out.println("\n--- 🐳 Blub-LLM: GPU-Vision Active ---");

        while (true) {
            System.out.print("\nYOU: ");
            String userPrompt = scanner.nextLine();
            
            if (userPrompt.equalsIgnoreCase("exit")) {
                Runtime.getRuntime().exec("subst B: /d");
                System.exit(0);
            }

            if (userPrompt.toLowerCase().contains("blubllm-1.0-snapshot.jar")) {
                System.out.println("AI: That's me! I'm the JAR file running this whole operation.");
                continue;
            }

            File targetImage = checkForImageQuery(brainDriveFolder, userPrompt);
            System.out.println("AI is thinking...");
            String responseText;

            try {
                if (targetImage != null && targetImage.exists()) {
                    String base64Image = compressImageToBase64(targetImage);
                    
                    UserMessage userMessage = UserMessage.from(
                        TextContent.from(userPrompt),
                        ImageContent.from(base64Image, "image/jpeg")
                    );
                    
                    Response<AiMessage> response = visionModel.generate(userMessage);
                    responseText = response.content().text();
                } else {
                    String context = readBrainDrive(brainDriveFolder);
                    responseText = textModel.generate("You have access to the user's files inside the B: drive folder. Here is the true file content:\n" + context + "\n\nUser Question: " + userPrompt);
                }
            } catch (Exception e) {
                responseText = "Processing Error: " + e.getMessage();
            }

            System.out.println("AI: " + responseText);
            syncToGithub(repoDir, "AI Sync: " + responseText.substring(0, Math.min(responseText.length(), 30)));
        }
    }

    private static String compressImageToBase64(File imageFile) throws Exception {
        BufferedImage originalImage = ImageIO.read(imageFile);
        if (originalImage == null) throw new Exception("Could not read image file.");
        
        int targetWidth = 800;
        double ratio = (double) targetWidth / originalImage.getWidth();
        int targetHeight = (int) (originalImage.getHeight() * ratio);
        
        BufferedImage resizedImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = resizedImage.createGraphics();
        g.drawImage(originalImage, 0, 0, targetWidth, targetHeight, null);
        g.dispose();
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(resizedImage, "jpg", baos);
        
        // FIX: Use standard encoder and strip all newline artifacts to prevent token corruption
        return Base64.getEncoder().encodeToString(baos.toByteArray()).replaceAll("\\r|\\n", "");
    }

    private static File checkForImageQuery(File folder, String prompt) {
        String cleanPrompt = prompt.toLowerCase().replace("b:\\", "").replace("b:/", "");
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(folder.toPath())) {
            for (Path entry : stream) {
                String name = entry.getFileName().toString().toLowerCase();
                if ((name.endsWith(".png") || name.endsWith(".jpg") || name.endsWith(".jpeg")) 
                     && cleanPrompt.contains(name)) {
                    return entry.toFile();
                }
            }
        } catch (Exception ignored) {}
        return null;
    }

    private static String readBrainDrive(File folder) throws Exception {
        StringBuilder content = new StringBuilder();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(folder.toPath())) {
            for (Path entry : stream) {
                String name = entry.getFileName().toString().toLowerCase();
                // FIX: Added .html and .htm support explicitly so it reads the source file
                if (name.endsWith(".txt") || name.endsWith(".md") || name.endsWith(".java") || 
                    name.endsWith(".py") || name.endsWith(".bat") || name.endsWith(".html") || name.endsWith(".htm")) {
                    content.append("[File: ").append(entry.getFileName().toString()).append("]\nContent:\n")
                           .append(Files.readString(entry)).append("\n---\n");
                }
            }
        } catch (Exception e) { return "Empty drive."; }
        return content.toString();
    }

    private static void handleToken(Scanner scanner) throws Exception {
        File dir = new File(APP_DATA_DIR);
        if (!dir.exists()) dir.mkdirs();
        File tokenFile = new File(CONFIG_FILE);
        if (tokenFile.exists()) {
            GITHUB_TOKEN = Files.readString(tokenFile.toPath()).trim();
        } else {
            System.out.print("Enter GitHub Fine-grained Token: ");
            GITHUB_TOKEN = scanner.nextLine();
            Files.writeString(tokenFile.toPath(), GITHUB_TOKEN);
        }
    }

    private static void syncToGithub(File repoDir, String message) {
        try (Git git = Git.open(repoDir)) {
            git.add().addFilepattern(".").call();
            git.commit().setMessage(message).call();
            git.push().setCredentialsProvider(new UsernamePasswordCredentialsProvider(GITHUB_TOKEN, "")).call();
        } catch (Exception ignored) {}
    }
}