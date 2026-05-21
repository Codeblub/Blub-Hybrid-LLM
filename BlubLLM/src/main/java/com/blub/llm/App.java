package com.blub.llm;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ImageContent;
import dev.langchain4j.data.message.TextContent;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.openai.OpenAiChatModel;
import org.eclipse.jgit.api.Git;

import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.file.*;
import java.time.Duration;
import java.util.Base64;
import javax.imageio.ImageIO;
import java.util.Scanner;

public class App {
    private static final String REPO_URL = "https://github.com/Codeblub/Blub-Hybrid-LLM.git";
    private static final String LOCAL_REPO_DIR = System.getProperty("user.home") + "/BlubRepo";
    // Stores the credentials globally in the same user home location as Git config files
    private static final Path TOKEN_FILE = Paths.get(System.getProperty("user.home"), ".hf_token");

    public static void main(String[] args) throws Exception {
        Scanner scanner = new Scanner(System.in);
        String apiKey = "";

        // Check if the credentials file exists
        if (Files.exists(TOKEN_FILE)) {
            apiKey = Files.readString(TOKEN_FILE).trim();
            System.out.println(">> Loaded saved Hugging Face token automatically.");
        } else {
            System.out.print("Enter your Free Hugging Face Token ( if you dont have a token, go to https://huggingface.co/settings/tokens ): ");
            apiKey = scanner.nextLine().trim();
            Files.writeString(TOKEN_FILE, apiKey);
            System.out.println(">> Token secured and saved for future sessions.");
        }

        File repoDir = new File(LOCAL_REPO_DIR);
        if (!repoDir.exists()) {
            System.out.println(">> Syncing repository knowledge base...");
            Git.cloneRepository().setURI(REPO_URL).setDirectory(repoDir).call();
        }

OpenAiChatModel model = OpenAiChatModel.builder()
                .baseUrl("https://router.huggingface.co/v1")
                .apiKey(apiKey)
                .modelName("Qwen/Qwen2.5-7B-Instruct") // Swapped to the free open-access engine
                .timeout(Duration.ofSeconds(45))
                .build();

        System.out.println("\n--- Blub-LLM Engine Active ---");
        System.out.println("(Type 'screen' to analyze your desktop display, or pass a raw file path to an image)");

        while (true) {
            System.out.print("\nYOU: ");
            String userPrompt = scanner.nextLine();
            
            if (userPrompt.equalsIgnoreCase("exit")) break;

            String repoKnowledge = readRepositoryKnowledge(repoDir);
            String masterContext = "You are Blub-LLM. Here is context from the user's brain drive:\n" + repoKnowledge;

            try {
                UserMessage message;

                if (userPrompt.toLowerCase().trim().equals("screen")) {
                    System.out.println("AI is capturing your screen layout...");
                    String base64Image = captureScreenAsBase64();
                    message = UserMessage.from(
                        TextContent.from(masterContext + "\nAnalyze this screen capture display:"),
                        ImageContent.from(base64Image, "image/png")
                    );
                } 
                else if (userPrompt.toLowerCase().endsWith(".png") || userPrompt.toLowerCase().endsWith(".jpg") || userPrompt.toLowerCase().endsWith(".jpeg")) {
                    System.out.println("AI is processing file data...");
                    String base64Image = convertFileToBase64(userPrompt.trim());
                    message = UserMessage.from(
                        TextContent.from(masterContext + "\nAnalyze this attached image file:"),
                        ImageContent.from(base64Image, "image/png")
                    );
                } 
                else {
                    message = UserMessage.from(masterContext + "\nUser Question: " + userPrompt);
                }

                System.out.println("AI is processing...");
                AiMessage response = model.generate(message).content();
                System.out.println("AI: " + response.text());

            } catch (Exception e) {
                System.out.println("AI Error: " + e.getMessage());
            }
        }
    }

    private static String captureScreenAsBase64() throws Exception {
        Rectangle screenRect = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
        BufferedImage capture = new Robot().createScreenCapture(screenRect);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(capture, "png", baos);
        return Base64.getEncoder().encodeToString(baos.toByteArray());
    }

    private static String convertFileToBase64(String filePath) throws Exception {
        byte[] fileContent = Files.readAllBytes(Paths.get(filePath));
        return Base64.getEncoder().encodeToString(fileContent);
    }

    private static String readRepositoryKnowledge(File repoDir) {
        StringBuilder sb = new StringBuilder();
        Path[] searchPaths = { repoDir.toPath(), Paths.get(repoDir.getAbsolutePath(), "ai stuff") };
        for (Path path : searchPaths) {
            if (!Files.exists(path)) continue;
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(path)) {
                for (Path entry : stream) {
                    String name = entry.getFileName().toString().toLowerCase();
                    if (name.endsWith(".md") || name.endsWith(".txt") || name.endsWith(".json")) {
                        sb.append("\n[Brain Drive Source: ").append(entry.getFileName().toString()).append("]\n")
                          .append(Files.readString(entry)).append("\n---\n");
                    }
                }
            } catch (Exception ignored) {}
        }
        return sb.toString();
    }
}