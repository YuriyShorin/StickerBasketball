package com.example.stickerbasketball.service;

import com.example.stickerbasketball.Player;
import com.example.stickerbasketball.config.StickerBasketballConfig;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;


@Component
public class StickerBasketball extends TelegramLongPollingBot {

    final StickerBasketballConfig config;
    HashSet<String> playersSet = new HashSet<>();
    ArrayList<Player> playersTable = new ArrayList<>();
    ArrayList<String> playersList = new ArrayList<>();
    private boolean wasGameStarted = false;
    private boolean wasGameFinished = false;
    private int round = 1;
    private int indexOfCurrentPlayer = 0;

    StickerBasketball(StickerBasketballConfig config) {
        this.config = config;
    }

    @Override
    public String getBotUsername() {
        return config.getBotName();
    }

    @Override
    public String getBotToken() {
        return config.getToken();
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasDice() && wasGameFinished) {
            throwingBallAfterGame(update);
            return;
        }
        if (update.hasMessage() && update.getMessage().hasDice() && wasGameStarted) {
            processGame(update);
            return;
        }
        if (update.hasMessage() && update.getMessage().hasText()) {
            checkCommand(update);
        }
    }

    private void startCommandReceived(long chatId, String username) {
        sendMessage(chatId, "Здарова " + username + "!");
    }

    private void playCommandReceived(long chatId, String username) {
        if (playersSet.contains(username)) {
            return;
        }
        playersSet.add(username);
        playersList.add(username);
        sendMessage(chatId, username + " регнулся!");
    }

    private void startGameCommandReceived(long chatId) {
        wasGameStarted = true;
        wasGameFinished = false;
        Random random = new Random();
        int numbersOfNotAddedPlayers = playersList.size();
        int playersNumber = playersList.size();
        for (int i = 0; i < playersNumber; ++i) {
            int index = random.nextInt(numbersOfNotAddedPlayers--);
            playersTable.add(new Player(playersList.get(index)));
            playersList.remove(index);
        }
        createTable(chatId);
    }

    private void throwingBallAfterGame(Update update) {
        long chatId = update.getMessage().getChatId();
        sendMessage(chatId, "Нахуй ты бросаешь, огузок?");
    }

    private void processGame(Update update) {
        long chatId = update.getMessage().getChatId();
        if (!update.getMessage().getFrom().getFirstName().equals(playersTable.get(indexOfCurrentPlayer).getTelegramUserName())) {
            sendMessage(chatId, "Долбоеб, бросает: " + playersTable.get(indexOfCurrentPlayer).getTelegramUserName());
            return;
        }
        int ballValue = update.getMessage().getDice().getValue();
        processBallThrow(chatId, ballValue);
        IsGoal(chatId, ballValue);
        if (isWin(chatId)) {
            return;
        }
        changeIndexOfCurrentPlayer();
    }

    private void checkCommand(Update update) {
        long chatId = update.getMessage().getChatId();
        String messageText = update.getMessage().getText();
        if (!messageText.startsWith("/")) {
            return;
        }
        switch (messageText) {
            case "/start" -> startCommandReceived(chatId, update.getMessage().getFrom().getFirstName());
            case "/startGame" -> startGameCommandReceived(chatId);
            case "/play" -> playCommandReceived(chatId, update.getMessage().getFrom().getFirstName());
            default -> sendMessage(chatId, "Хуйню написал, бро");
        }
    }

    private void createTable(long chatId) {
        StringBuilder tableToSend = new StringBuilder();
        tableToSend.append("Раунд №").append(round).append(":\n\n");
        for (int i = 0; i < playersTable.size(); ++i) {
            tableToSend.append(playersTable.get(i).getTelegramUserName()).append("\n");
            if (i % 2 == 1) {
                tableToSend.append("\n");
            }
        }
        sendMessage(chatId, tableToSend.toString());
    }

    private void processBallThrow(long chatId, int ballValue) {
        switch (ballValue) {
            case 1 -> answerToThrow(chatId, 4200, "Косой еблан");
            case 2 -> answerToThrow(chatId, 4200, "бля почти сука.");
            case 3 -> answerToThrow(chatId, 4200, "сука застрял.");
            case 4 -> answerToThrow(chatId, 3000, "фонарь.");
            case 5 -> answerToThrow(chatId, 4200, "Наааа нахуй");
        }
    }

    private void answerToThrow(long chatId, int timeToSleep, String answerToThrow) {
        try {
            Thread.sleep(timeToSleep);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        sendMessage(chatId, answerToThrow);
    }

    private void IsGoal(long chatId, int ballValue) {
        if (ballValue > 3) {
            playersTable.get(indexOfCurrentPlayer).incrementScore();
            if (indexOfCurrentPlayer % 2 == 0) {
                sendMessage(chatId, playersTable.get(indexOfCurrentPlayer).getScore() + ":" + playersTable.get(indexOfCurrentPlayer + 1).getScore());
            } else {
                sendMessage(chatId, playersTable.get(indexOfCurrentPlayer - 1).getScore() + ":" + playersTable.get(indexOfCurrentPlayer).getScore());
            }
        }
    }

    private boolean isWin(long chatId) {
        if (indexOfCurrentPlayer % 2 == 1) {
            if (playersTable.get(indexOfCurrentPlayer).getScore() >= 3 && playersTable.get(indexOfCurrentPlayer).getScore() - playersTable.get(indexOfCurrentPlayer - 1).getScore() >= 2) {
                playersTable.get(indexOfCurrentPlayer).setWinner(true);
                sendMessage(chatId, playersTable.get(indexOfCurrentPlayer).getTelegramUserName() + " обоссал.");
                indexOfCurrentPlayer++;
                isGameFinished(chatId);
                return true;
            }
            if (playersTable.get(indexOfCurrentPlayer - 1).getScore() >= 3 && playersTable.get(indexOfCurrentPlayer - 1).getScore() - playersTable.get(indexOfCurrentPlayer).getScore() >= 2) {
                playersTable.get(indexOfCurrentPlayer - 1).setWinner(true);
                sendMessage(chatId, playersTable.get(indexOfCurrentPlayer - 1).getTelegramUserName() + " обоссал!");
                indexOfCurrentPlayer++;
                isGameFinished(chatId);
                return true;
            }
        }
        return false;
    }

    private void isGameFinished(long chatId) {
        if (indexOfCurrentPlayer >= playersTable.size()) {
            sendMessage(chatId, "Игра закончена епта");
            wasGameStarted = false;
            wasGameFinished = true;
            playersList.clear();
            playersTable.clear();
            playersSet.clear();
            indexOfCurrentPlayer = 0;
        }
    }

    private void changeIndexOfCurrentPlayer() {
        if (indexOfCurrentPlayer % 2 == 0) {
            indexOfCurrentPlayer++;
        } else {
            indexOfCurrentPlayer--;
        }
    }

    private void sendMessage(long charId, String textToSend) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(charId));
        message.setText(textToSend);
        try {
            execute(message);
        } catch (TelegramApiException exception) {
            exception.printStackTrace();
        }
    }
}