package com.company;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.ArrayList;

public class InputSensor {
    private ArrayList<Integer> temperatureSensor;
    private ArrayList<Integer> wetnessSensor;
    private int period;
    @JsonIgnore
    private int position = 0;

    private Flowerbed flowerbed;

    public InputSensor(ArrayList<Integer> temperatureSensor, ArrayList<Integer> wetnessSensor, int period) {
        this.temperatureSensor = temperatureSensor;
        this.wetnessSensor = wetnessSensor;
        this.period = period;
    }

    public void update(Flowerbed flowerbed, int time){
        if (time % period == 0){
            position++;
            if (position==temperatureSensor.size())
                position=0;
            flowerbed.setTemperatureSensor(temperatureSensor.get(position));
            flowerbed.setWetnessSensor(wetnessSensor.get(position));
        }
    }

    public InputSensor() {
    }

    public ArrayList<Integer> getTemperatureSensor() {
        return temperatureSensor;
    }

    public void setTemperatureSensor(ArrayList<Integer> temperatureSensor) {
        this.temperatureSensor = temperatureSensor;
    }

    public ArrayList<Integer> getWetnessSensor() {
        return wetnessSensor;
    }

    public void setWetnessSensor(ArrayList<Integer> wetnessSensor) {
        this.wetnessSensor = wetnessSensor;
    }

    public int getPeriod() {
        return period;
    }

    public void setPeriod(int period) {
        this.period = period;
    }

    public Flowerbed getFlowerbed() {
        return flowerbed;
    }

    public void setFlowerbed(Flowerbed flowerbed) {
        this.flowerbed = flowerbed;
    }

    @Override
    public String toString() {
        return "InputSensor{" +
                "position=" + position +
                '}';
    }
}
