package gg.luau.twilight.startup;

import net.dv8tion.jda.api.JDABuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.login.LoginException;

public class Main {
    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

    public static void main(String... args) throws LoginException, InterruptedException {
        final var token = System.getenv("TWILIGHT_TOKEN");
        final var jda = JDABuilder.createLight(token)
                .addEventListeners(null) // TODO: fix.
                .build();

        jda.awaitReady();
    }
}
