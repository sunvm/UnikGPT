package com.unikgpt;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.java.JavaPlugin;
import okhttp3.*;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import java.io.IOException;
import java.util.UUID;
import okhttp3.Credentials;
import okhttp3.FormBody;
import javax.net.ssl.*;
import java.security.cert.X509Certificate;
import okhttp3.OkHttpClient.Builder;
import java.util.concurrent.TimeUnit;
import java.util.Base64;
import java.nio.charset.StandardCharsets;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.file.FileConfiguration;
import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.Random;
import java.util.Arrays;
import java.util.Collections;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;

public class UnikGPT extends JavaPlugin implements Listener {
    private OkHttpClient client;
    private String apiKey;
    private String apiUrl;
    private String model;
    private List<Map<String, Object>> proxyList;
    private Random random = new Random();

    @Override
    public void onEnable() {
        saveDefaultConfig();
        
        File proxyFile = new File(getDataFolder(), "proxies.yml");
        if (!proxyFile.exists()) {
            saveResource("proxies.yml", false);
        }
        
        apiKey = getConfig().getString("api.key");
        apiUrl = getConfig().getString("api.url");
        model = getConfig().getString("api.model");
        
        if (apiKey == null || apiKey.equals("YOUR_GOOGLE_API_KEY_HERE")) {
            getLogger().severe("(╯°□°）︵ ┻━┻ API клю не настроен! Укажите его в config.yml");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        
        loadProxies();
        
        client = createHttpClient();
        
        getServer().getPluginManager().registerEvents(this, this);
        getLogger().info("(ﾉ◕ヮ◕)ﾉ*:･ﾟ✧ UnikGPT успешно загружен!");
    }

    private void loadProxies() {
        File proxyFile = new File(getDataFolder(), "proxies.yml");
        if (!proxyFile.exists()) {
            getLogger().warning("(×﹏×) Файл proxies.yml не найден! Создаем новый...");
            try {
                proxyFile.getParentFile().mkdirs();
                proxyFile.createNewFile();
                FileConfiguration proxyConfig = YamlConfiguration.loadConfiguration(proxyFile);
                List<Map<String, Object>> defaultProxies = new ArrayList<>();
                
                // Добавляем несколько рабочих прокси по умолчанию
                Map<String, Object> proxy1 = new HashMap<>();
                proxy1.put("server", "proxy1.example.com");
                proxy1.put("port", 443);
                proxy1.put("secret", "your_secret_here");
                defaultProxies.add(proxy1);
                
                proxyConfig.set("proxies", defaultProxies);
                proxyConfig.save(proxyFile);
            } catch (IOException e) {
                getLogger().severe("(╯°□°）╯︵ ┻━┻ Не удалось создать proxies.yml: " + e.getMessage());
                return;
            }
        }
        
        FileConfiguration proxyConfig = YamlConfiguration.loadConfiguration(proxyFile);
        proxyList = new ArrayList<>();
        
        List<?> proxies = proxyConfig.getList("proxies");
        if (proxies != null) {
            for (Object obj : proxies) {
                if (obj instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> proxy = (Map<String, Object>) obj;
                    proxyList.add(proxy);
                }
            }
        }
        
        if (proxyList.isEmpty()) {
            getLogger().warning("(×﹏×) Список прокси пуст! Проверьте файл proxies.yml");
        } else {
            getLogger().info("(｀・ω・´) Загружено " + proxyList.size() + " прокси серверов");
        }
    }

    private Map<String, Object> getRandomProxy() {
        if (proxyList.isEmpty()) {
            return null;
        }
        return proxyList.get(random.nextInt(proxyList.size()));
    }

    private OkHttpClient createHttpClient() {
        Builder builder = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .callTimeout(45, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .protocols(Collections.singletonList(Protocol.HTTP_1_1))
            .followRedirects(true)
            .followSslRedirects(true);

        Map<String, Object> proxy = getRandomProxy();
        if (proxy != null) {
            String proxyHost = (String) proxy.get("server");
            int proxyPort = ((Number) proxy.get("port")).intValue();
            String secret = (String) proxy.get("secret");

            if (proxyHost != null && !proxyHost.isEmpty() && proxyPort > 0) {
                try {
                    TrustManager[] trustAllCerts = new TrustManager[] {
                        new X509TrustManager() {
                            public void checkClientTrusted(X509Certificate[] chain, String authType) {}
                            public void checkServerTrusted(X509Certificate[] chain, String authType) {}
                            public X509Certificate[] getAcceptedIssuers() { return new X509Certificate[]{}; }
                        }
                    };

                    SSLContext sslContext = SSLContext.getInstance("TLS");
                    sslContext.init(null, trustAllCerts, new SecureRandom());

                    Proxy proxyConfig = new Proxy(Proxy.Type.SOCKS, new InetSocketAddress(proxyHost, proxyPort));
                    builder.proxy(proxyConfig)
                        .sslSocketFactory(sslContext.getSocketFactory(), (X509TrustManager) trustAllCerts[0])
                        .hostnameVerifier((hostname, session) -> true);

                    if (secret != null && !secret.isEmpty()) {
                        builder.proxyAuthenticator((route, response) -> {
                            String encodedSecret = Base64.getEncoder().encodeToString(secret.getBytes(StandardCharsets.UTF_8));
                            return response.request().newBuilder()
                                .header("Proxy-Authorization", "Basic " + encodedSecret)
                                .header("User-Agent", "Mozilla/5.0")
                                .header("Accept", "*/*")
                                .header("Connection", "keep-alive")
                                .build();
                        });
                    }

                    getLogger().info("(｀・ω・´) Используется прокси: " + proxyHost + ":" + proxyPort);
                } catch (Exception e) {
                    getLogger().warning("(×﹏×) Ошибка настройки прокси: " + e.getMessage());
                    return new OkHttpClient.Builder()
                        .connectTimeout(30, TimeUnit.SECONDS)
                        .readTimeout(30, TimeUnit.SECONDS)
                        .writeTimeout(30, TimeUnit.SECONDS)
                        .protocols(Collections.singletonList(Protocol.HTTP_1_1))
                        .build();
                }
            }
        }

        return builder.build();
    }

    private String getAIResponse(String question) throws IOException {
        MediaType JSON = MediaType.get("application/json; charset=utf-8");
        
        JsonObject jsonBody = new JsonObject();
        JsonArray contents = new JsonArray();
        JsonObject content = new JsonObject();
        JsonArray parts = new JsonArray();
        
        JsonObject part = new JsonObject();
        part.addProperty("text", question);
        parts.add(part);
        
        content.add("parts", parts);
        contents.add(content);
        jsonBody.add("contents", contents);

        int maxRetries = 4;
        int currentTry = 0;
        Exception lastException = null;
        boolean useProxy = false;

        while (currentTry < maxRetries) {
            Response response = null;
            try {
                if (currentTry == 0) {
                    client = new OkHttpClient.Builder()
                        .connectTimeout(30, TimeUnit.SECONDS)
                        .readTimeout(30, TimeUnit.SECONDS)
                        .writeTimeout(30, TimeUnit.SECONDS)
                        .protocols(Collections.singletonList(Protocol.HTTP_1_1))
                        .build();
                    getLogger().info("(｀・ω・´) Пробуем прямое подключение...");
                } else {
                    useProxy = true;
                    client = createHttpClient();
                }

                RequestBody body = RequestBody.create(jsonBody.toString(), JSON);
                Request request = new Request.Builder()
                    .url(apiUrl + "?key=" + apiKey)
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Accept", "application/json")
                    .addHeader("User-Agent", "Mozilla/5.0")
                    .addHeader("Connection", "close")
                    .post(body)
                    .build();

                getLogger().info("(｀・ω・´) Попытка " + (currentTry + 1) + " отправки запроса к API" + 
                    (useProxy ? " через прокси..." : " напрямую..."));

                response = client.newCall(request).execute();
                
                if (!response.isSuccessful()) {
                    String errorBody = response.body() != null ? response.body().string() : "No error body";
                    throw new IOException("API request failed with code: " + response.code() + ", body: " + errorBody);
                }
                
                String responseBody = response.body().string();
                getLogger().info("(◕‿◕) Получен ответ от API" + (useProxy ? " через прокси!" : " напрямую!"));
                
                JsonObject jsonResponse = new Gson().fromJson(responseBody, JsonObject.class);
                return jsonResponse
                    .getAsJsonArray("candidates")
                    .get(0).getAsJsonObject()
                    .getAsJsonObject("content")
                    .getAsJsonArray("parts")
                    .get(0).getAsJsonObject()
                    .get("text")
                    .getAsString()
                    .trim();
                    
            } catch (Exception e) {
                lastException = e;
                String connectionType = useProxy ? "через прокси" : "напрямую";
                getLogger().warning("(×﹏×) Ошибка при попытке " + (currentTry + 1) + " " + connectionType + ": " + e.getMessage());
                
                if (response != null) {
                    response.close();
                }
                
                if (currentTry < maxRetries - 1) {
                    currentTry++;
                    try {
                        int sleepTime = useProxy ? 5000 * currentTry : 2000;
                        Thread.sleep(sleepTime);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new IOException("Операция была прервана", ie);
                    }
                    continue;
                }
                break;
            }
        }
        
        throw new IOException(lastException != null ? 
            "Все попытки подключения не удались (" + (useProxy ? "с прокси" : "напрямую") + "): " + lastException.getMessage() : 
            "Превышено количество попыток подключения");
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        String message = event.getMessage();
        String playerName = event.getPlayer().getName();
        
        if (message.startsWith("?!")) {
            event.setCancelled(true);
            String question = message.substring(2).trim();
            
            Bukkit.getScheduler().runTaskAsynchronously(this, () -> {
                try {
                    String response = getAIResponse(question);
                    Bukkit.getScheduler().runTask(this, () -> {
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "! " + response);
                    });
                } catch (Exception e) {
                    String errorMessage = "§c(╥﹏╥) Произошла ошибка: " + e.getMessage();
                    getLogger().warning(errorMessage);
                    Bukkit.getScheduler().runTask(this, () -> {
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "! " + errorMessage);
                    });
                }
            });
        } else if (message.startsWith("?")) {
            event.setCancelled(true);
            String question = message.substring(1).trim();
            
            Bukkit.getScheduler().runTaskAsynchronously(this, () -> {
                try {
                    String response = getAIResponse(question);
                    Bukkit.getScheduler().runTask(this, () -> {
                        sendMessage(playerName, response);
                    });
                } catch (Exception e) {
                    String errorMessage = "§c(╥﹏╥) Произошла ошибка: " + e.getMessage();
                    getLogger().warning(errorMessage);
                    Bukkit.getScheduler().runTask(this, () -> {
                        sendMessage(playerName, errorMessage);
                    });
                }
            });
        }
    }

    private void sendMessage(String playerName, String message) {
        final int MAX_LENGTH = 256;
        
        if (message.length() <= MAX_LENGTH) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "w " + playerName + " " + message);
            return;
        }

        String[] words = message.split(" ");
        StringBuilder currentMessage = new StringBuilder();

        for (String word : words) {
            if (currentMessage.length() + word.length() + 1 > MAX_LENGTH) {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), 
                    "w " + playerName + " " + currentMessage.toString().trim());
                currentMessage = new StringBuilder();
            }
            currentMessage.append(word).append(" ");
        }

        if (currentMessage.length() > 0) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), 
                "w " + playerName + " " + currentMessage.toString().trim());
        }
    }
} 