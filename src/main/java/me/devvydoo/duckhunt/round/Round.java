package me.devvydoo.duckhunt.round;

public interface Round {

    RoundType getRoundType();
    void startRound();
    void endRound();
    RoundType getNextRoundType();

}
