package com.unikgpt;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.java.JavaPlugin;
import okhttp3.*;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.util.function.Consumer;

public class UnikGPT extends JavaPlugin implements Listener {
    private final OkHttpClient client = new OkHttpClient();
    private String apiKey;
    private String apiUrl;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        
        apiKey = getConfig().getString("api.key");
        apiUrl = getConfig().getString("api.url");
        
        if (apiKey == null || apiKey.equals("YOUR_GEMINI_API_KEY_HERE")) {
            getLogger().severe("API ключ не настроен! Пожалуйста, укажите API ключ в config.yml");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        
        getServer().getPluginManager().registerEvents(this, this);
        getLogger().info("UnikGPT успешно загружен!");
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        String message = event.getMessage();
        
        if (message.startsWith("?!")) {
            event.setCancelled(true);
            String question = message.substring(2).trim();
            
            getAsyncAIResponse(question, response -> {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "! " + response);
            });
            
        } else if (message.startsWith("?")) {
            event.setCancelled(true);
            String question = message.substring(1).trim();
            
            getAsyncAIResponse(question, response -> {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "w " + event.getPlayer().getName() + " " + response);
            });
        }
    }

    private void getAsyncAIResponse(String question, Consumer<String> callback) {
        Bukkit.getScheduler().runTaskAsynchronously(this, () -> {
            try {
                String response = getAIResponse(question);
                Bukkit.getScheduler().runTask(this, () -> callback.accept(response));
            } catch (Exception e) {
                Bukkit.getScheduler().runTask(this, () -> 
                    callback.accept(getConfig().getString("messages.error", "Произошла ошибка при получении ответа от ИИ.")));
                getLogger().warning("Ошибка при получении ответа от API: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    private String getAIResponse(String question) throws IOException {
        MediaType JSON = MediaType.get("application/json; charset=utf-8");
        
        JsonObject jsonBody = new JsonObject();
        JsonObject contents = new JsonObject();
        contents.addProperty("text", question);
        jsonBody.add("contents", contents);

        RequestBody body = RequestBody.create(jsonBody.toString(), JSON);
        Request request = new Request.Builder()
            .url(apiUrl + "?key=" + apiKey)
            .post(body)
            .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
            
            String responseBody = response.body().string();
            JsonObject jsonResponse = new Gson().fromJson(responseBody, JsonObject.class);
            return jsonResponse.getAsJsonObject("candidates")
                .getAsJsonArray("content")
                .get(0).getAsJsonObject()
                .get("text").getAsString().trim();
        }
    }
}
