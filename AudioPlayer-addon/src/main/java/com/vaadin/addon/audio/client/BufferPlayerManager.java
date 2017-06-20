package com.vaadin.addon.audio.client;

import com.google.gwt.user.client.Timer;


import java.util.logging.Logger;

/**
 * Class used to keep track of multiple BufferPlayer objects. Each BufferPlayer
 * is in charge of playing a single chunk of the entire audio buffer. This class
 * simply helps in tracking which BufferPlayer is the current and rotating between
 * them.
 */
public class BufferPlayerManager {

    private static final Logger logger = Logger.getLogger("BufferPlayerManager");

    private static int MAX_PLAYERS_DEFAULT = 2;

    private int currentPlayer = 0;
    private BufferPlayer[] players;


    public BufferPlayerManager() {
        this(MAX_PLAYERS_DEFAULT);
    }

    public BufferPlayerManager(int maxPlayers) {
        players = new BufferPlayer[maxPlayers];
    }

    public BufferPlayer getCurrentPlayer() {
        return players[currentPlayer];
    }

    public void setCurrentPlayer(BufferPlayer player) {
        players[currentPlayer] = player;
    }

    public BufferPlayer getNextPlayer() {
        int i = (currentPlayer + 1) % players.length;
        return players[i];
    }

    public void setNextPlayer(BufferPlayer player) {
        int i = (currentPlayer + 1) % players.length;
        players[i] = player;
    }

    public BufferPlayer getPrevPlayer() {
        int i = (currentPlayer == 0 ? players.length - 1 : currentPlayer - 1);
        return players[i];
    }

    public void moveToNextPlayer() {
        currentPlayer = (currentPlayer + 1) % players.length;
    }

    public void moveToPrevPlayer() {
        currentPlayer = (currentPlayer == 0 ? players.length - 1 : currentPlayer - 1);
    }

    public BufferPlayer[] getPlayers() {
        return players;
    }
}
