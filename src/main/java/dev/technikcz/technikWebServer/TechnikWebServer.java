package dev.technikcz.technikWebServer;

import io.javalin.Javalin;
import io.javalin.config.JavalinConfig;
import io.javalin.http.staticfiles.Location;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

public final class TechnikWebServer extends JavaPlugin {
    private static Javalin server;

    private File getConfigFile() {
        return new File(getDataFolder(), "config.yml");
    }

    private YamlConfiguration getConfiguration() {
        return YamlConfiguration.loadConfiguration(getConfigFile());
    }

    private void javalinConfig(JavalinConfig javalinConfig) {
        var config = getConfiguration();

        var dataFolder = new File(getDataFolder(), Objects.requireNonNull(config.getString("dataPath")));

        if(!dataFolder.exists()) {
            dataFolder.mkdirs();
        }

        javalinConfig.staticFiles.add((sfc) -> {
            sfc.directory = dataFolder.getAbsolutePath();
            sfc.location = Location.EXTERNAL;
            sfc.hostedPath = "/";
        });

        javalinConfig.jetty.defaultHost = config.getString("host");
        javalinConfig.jetty.defaultPort = config.getInt("port");
        javalinConfig.useVirtualThreads = true;
    }

    @Override
    public void onEnable() {
        var configFile = getConfigFile();

        if(!configFile.exists()) {
            var config = new YamlConfiguration();

            config.set("port", 80);
            config.set("host", "127.0.0.1");
            config.set("dataPath", "./data");

            try {
                config.save(configFile);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        server = Javalin
                .create(this::javalinConfig)
                .get("/", ctx -> ctx.result("OK"))
                .start();

        System.out.println("TechnikWebServer started!");
        System.out.println("Address: " + server.jettyServer().server().getURI().toString());
    }

    @Override
    public void onDisable() {
        final Javalin server = TechnikWebServer.server;

        if(server != null) {
            server.stop();
        }

        System.out.println("TechnikWebServer stoped!");
    }
}
