package com.room.app.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@Configuration
public class TelegramBotConfig {

    @Bean
    public TelegramBotsApi telegramBotsApi(MyTelegramBot bot) throws TelegramApiException {
        TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
        try {
            botsApi.registerBot(bot);
            return botsApi;
        } catch (TelegramApiException e) {
            if (e.getMessage().contains("terminated by other getUpdates request")) {
                // Wait and retry once
                try {
                    Thread.sleep(2000);
                    botsApi.registerBot(bot);
                    return botsApi;
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
            }
            throw e;
        }
    }
}