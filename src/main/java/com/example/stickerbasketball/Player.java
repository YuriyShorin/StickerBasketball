package com.example.stickerbasketball;

public class Player {

    private String telegramUserName;
    private int scored = 0;
    private boolean isWinner = false;

    public Player(String telegramUserName) {
        this.telegramUserName = telegramUserName;
    }

    public String getTelegramUserName() {
        return telegramUserName;
    }

    public void setTelegramUserName(String telegramUserName) {
        this.telegramUserName = telegramUserName;
    }

    public void incrementScore() {
        scored++;
    }

    public int getScore() {
        return scored;
    }

    public void setWinner(boolean winner) {
        isWinner = winner;
    }

    public boolean isWinner() {
        return isWinner;
    }
}