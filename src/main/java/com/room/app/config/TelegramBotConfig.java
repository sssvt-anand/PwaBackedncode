package com.room.app.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@Configuration
public class TelegramBotConfig {

    private final MyTelegramBot myTelegramBot;
    private TelegramBotsApi botsApi;

    public TelegramBotConfig(MyTelegramBot myTelegramBot) {
        this.myTelegramBot = myTelegramBot;
    }

    @Bean
    public TelegramBotsApi telegramBotsApi() throws TelegramApiException {
        // Singleton pattern
        if (botsApi == null) {
            synchronized (this) {
                if (botsApi == null) {
                    botsApi = new TelegramBotsApi(DefaultBotSession.class);
                    botsApi.registerBot(myTelegramBot);
                }
            }
        }
        return botsApi;
    }

}
