package com.company;

import java.util.ArrayList;
import java.util.List;

public class InputAllSensors {
    private List<InputSensor> inputSensors;


    public InputAllSensors() {
        inputSensors = new ArrayList<InputSensor>();
    }

    public List<InputSensor> getInputSensors() {
        return inputSensors;
    }

    public void setInputSensors(ArrayList<InputSensor> inputSensors) {
        this.inputSensors = inputSensors;
    }

    public void add(InputSensor inputSensor){
        inputSensors.add(inputSensor);
    }
}
