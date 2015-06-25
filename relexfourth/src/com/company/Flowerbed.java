package com.company;

public class Flowerbed {
    private int timeToMove;
    private int timeToShower;
    private int temperatureSensor;
    private int wetnessSensor;
    private int restTime;
    private boolean waiting;

    public Flowerbed() {
    }

    public Flowerbed(int timeToMove, int timeToShower, int temperatureSensor, int wetnessSensor, int restTime) {
        this.timeToMove = timeToMove;
        this.timeToShower = timeToShower;
        this.temperatureSensor = temperatureSensor;
        this.wetnessSensor = wetnessSensor;
        this.restTime = restTime;
        waiting = false;
    }

    public boolean isWaiting() {
        return waiting;
    }

    public void setWaiting(boolean waiting) {
        this.waiting = waiting;
    }

    public int getTimeToMove() {
        return timeToMove;
    }

    public void setTimeToMove(int timeToMove) {
        this.timeToMove = timeToMove;
    }

    public int getTimeToShower() {
        return timeToShower;
    }

    public void setTimeToShower(int timeToShower) {
        this.timeToShower = timeToShower;
    }

    public int getTemperatureSensor() {
        return temperatureSensor;
    }

    public void setTemperatureSensor(int temperatureSensor) {
        this.temperatureSensor = temperatureSensor;
    }

    public int getWetnessSensor() {
        return wetnessSensor;
    }

    public void setWetnessSensor(int wetnessSensor) {
        this.wetnessSensor = wetnessSensor;
    }

    public int getRestTime() {
        return restTime;
    }

    public void setRestTime(int restTime) {
        this.restTime = restTime;
    }

    @Override
    public String toString() {
        return Integer.toString(restTime);
    }
}
