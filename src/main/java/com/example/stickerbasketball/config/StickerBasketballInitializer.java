package com.example.stickerbasketball.config;

import com.example.stickerbasketball.service.StickerBasketball;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@Component
public class StickerBasketballInitializer {

    @Autowired
    StickerBasketball bot;

    @EventListener({ContextRefreshedEvent.class})
    public void init(){
        try {
            TelegramBotsApi api = new TelegramBotsApi(DefaultBotSession.class);
            api.registerBot(bot);
        } catch (TelegramApiException exception){

        }
    }

}