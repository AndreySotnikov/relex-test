package com.company;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.*;

public class Gardener {
    private Machine machine;
    private int flowerbedCount;
    private int criticalTemperatureValue;
    private int criticalWetnessValue;
    private int restTime;
    private Flowerbed[] flowerbeds;

    public Gardener(InputInfo inputInfo) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        final InputAllSensors inputAllSensors = mapper.readValue(Main.read(Main.inputSensorsValue), InputAllSensors.class);
        machine = new Machine(0, 0);
        flowerbedCount = inputInfo.getFlowerbedCount();
        restTime = inputInfo.getRestTime();
        criticalTemperatureValue = inputInfo.getCriticalTemperatureValue();
        criticalWetnessValue = inputInfo.getCriticalWetnessValue();
        flowerbeds = new Flowerbed[flowerbedCount];

        for (int i = 0; i < flowerbedCount; i++) {
            InputSensor inputSensor = inputAllSensors.getInputSensors().get(i);
            flowerbeds[i] = new Flowerbed(inputInfo.getMoveTime()[i], inputInfo.getShowerTime()[i],inputSensor.getTemperatureSensor().get(0), inputSensor.getWetnessSensor().get(0), 0);
        }

        //запуск потока, меняющего значения датчиков у клумб, через определенное время
        new Thread(){
            private int time = 0;
            List<InputSensor> inputSensors = inputAllSensors.getInputSensors();
            public void update(){
                for (int i=0; i<flowerbeds.length; i++){
                    inputSensors.get(i).update(flowerbeds[i], time);
                }
            }

            @Override
            public void run(){
                //for (int i = 0; i < inputSensors.size(); i++)
                  //  inputSensors.get(i).setFlowerbed(flowerbeds[i]);
                while(true){
                    try {
                        update();
                        time++;
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();
    }


    public void work() throws InterruptedException, IOException {
        Timer timer = new Timer();
        Worker worker = new Worker();
        timer.schedule(worker, 0, 1000);
    }

    //выбор клумбы для полива
    public int pickFlowerbed() {
        List<Integer> readyToWash = new ArrayList<Integer>();
        for (int i = 0; i < flowerbedCount; i++) {
            if ((flowerbeds[i].getWetnessSensor() < criticalWetnessValue
                    || flowerbeds[i].getTemperatureSensor() > criticalTemperatureValue) &&
                    flowerbeds[i].getRestTime() == 0) {
                readyToWash.add(i);
            }
        }
        if (readyToWash.isEmpty())
            return -1;
        return readyToWash.get(new Random().nextInt(readyToWash.size()));
    }

    //обратный отсчет до следующего полива
    public void updateFlowerbeds() {
        for (int i = 0; i < flowerbedCount; i++) {
            if (flowerbeds[i].getRestTime() != 0)
                flowerbeds[i].setRestTime(flowerbeds[i].getRestTime() - 1);
        }
    }

    class Worker extends TimerTask {
        private int currentFlowerbed = -1;
        private boolean isDone;
        private int wetnessValue;
        private int temperatureValue;

        private int time = 0;

        public void updateSensors(int flowerbed) {
            wetnessValue = flowerbeds[flowerbed].getWetnessSensor();
            temperatureValue = flowerbeds[flowerbed].getTemperatureSensor();
        }

        @Override
        public void run() {
            boolean ok = true;
            if (currentFlowerbed != -1) {
                updateSensors(currentFlowerbed);
            }
            //если значения температуры нормализовались, то ее не нужно поливать
            if (currentFlowerbed != -1 && criticalTemperatureValue >= temperatureValue && criticalWetnessValue <= wetnessValue) {
                machine.setFree();
                System.out.println();
                System.out.println("Задание отменено [Температура: " + temperatureValue + ", " + "Влажность: " + wetnessValue + "]");
            }
            if (ok && machine.isFree()) {
                //выдача нового задания
                currentFlowerbed = pickFlowerbed();
                if (currentFlowerbed == -1) {
                    ok = false;
                }
                if (ok) {
                    System.out.println();
                    System.out.println("Текущее время: " + time + " минут");
                    System.out.println("Машина отправлена к клумбе " + currentFlowerbed);
                    System.out.println("Температура: " + flowerbeds[currentFlowerbed].getTemperatureSensor() +
                            " Влажность: " + flowerbeds[currentFlowerbed].getWetnessSensor());
                    //переинициализация машины
                    machine.init(flowerbeds[currentFlowerbed].getTimeToMove(), flowerbeds[currentFlowerbed].getTimeToShower());
                    isDone = machine.doWork();
                }
            } else {
                //если машина занята, то продолжаем ее работу
                isDone = machine.doWork();
            }
            if (ok && currentFlowerbed!=-1 && isDone){
                System.out.println();
                System.out.println("Клумба " + currentFlowerbed + " полита");
                flowerbeds[currentFlowerbed].setRestTime(restTime);
                currentFlowerbed = -1;
            }
            updateFlowerbeds();
            time++;
        }
    }

}
