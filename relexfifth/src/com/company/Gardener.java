package com.company;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.*;


public class Gardener {
    private List<Machine> machines;
    private int flowerbedCount;
    private int machineCount;
    private int criticalTemperatureValue;
    private int criticalWetnessValue;
    private int restTime;
    private int[][] distances;
    private Flowerbed[] flowerbeds;

    public Gardener(InputInfo inputInfo) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        final InputAllSensors inputAllSensors = mapper.readValue(Main.read(Main.inputSensorsValue), InputAllSensors.class);
        distances = inputInfo.getDistances();
        flowerbedCount = inputInfo.getFlowerbedCount();
        restTime = inputInfo.getRestTime();
        machines = new ArrayList<Machine>();
        machineCount = inputInfo.getMachineCount();
        for (int i = 0; i < machineCount; i++){
            machines.add(new Machine(i));
        }
        criticalTemperatureValue = inputInfo.getCriticalTemperatureValue();
        criticalWetnessValue = inputInfo.getCriticalWetnessValue();
        flowerbeds = new Flowerbed[flowerbedCount];
        for (int i = 0; i < flowerbedCount; i++) {
            InputSensor inputSensor = inputAllSensors.getInputSensors().get(i);
            flowerbeds[i] = new Flowerbed(inputInfo.getShowerTime()[i],inputSensor.getTemperatureSensor().get(0), inputSensor.getWetnessSensor().get(0), 0);
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
        timer.schedule(worker, 0, 1000);    //запуск задачи с интервалом 1 сек
    }

    //выбор клумбы для полива
    public int pickFlowerbed(int machineIndex, List<Integer> availables) {
        int curDistance;
        int minDistance = -1;
        int flowerbed = -1;
        for (int i=0; i<availables.size(); i++){
            if ((flowerbeds[availables.get(i)].getWetnessSensor() < criticalWetnessValue
                    || (flowerbeds[availables.get(i)].getTemperatureSensor() > criticalTemperatureValue) &&
                    flowerbeds[availables.get(i)].getRestTime() == 0)){
                curDistance = distances[machineIndex][availables.get(i)];
                if (minDistance == -1){
                    minDistance = curDistance;
                    flowerbed = availables.get(i);
                }else if (minDistance > curDistance){
                    minDistance = curDistance;
                    flowerbed = availables.get(i);
                }
            }
        }
        return flowerbed;
    }

    //обратный отсчет до следующего полива
    public void updateFlowerbeds() {
        for (int i = 0; i < flowerbedCount; i++) {
            if (flowerbeds[i].getRestTime() != 0)
                flowerbeds[i].setRestTime(flowerbeds[i].getRestTime() - 1);
        }
    }

    class Worker extends TimerTask{
        List<Machine> busyMachines = new ArrayList<Machine>();
        List<Machine> availableMachines = new ArrayList<Machine>();
        List<Integer> availableFlowerbeds = new ArrayList<Integer>();
        @Override
        public void run() {
            //System.out.println(flowerbeds);
            clear();
            groupMachines();
            flowerbedsForWashing();
            giveTasks();
            continueOldTasks();
            updateFlowerbeds();
        }

        private void clear(){
            busyMachines.clear();
            availableMachines.clear();
            availableFlowerbeds.clear();
        }

        private void groupMachines(){
            for (int i=0; i<machineCount; i++){
                //добавление свободных машин
                if (machines.get(i).getTarget() == -1)
                    availableMachines.add(machines.get(i));
                else {
                    //добавление машин, едущих к клумбе, датчики которой перестали превышать критическое значение
                    if ((criticalTemperatureValue>=flowerbeds[machines.get(i).getTarget()].getTemperatureSensor()
                            && criticalWetnessValue<=flowerbeds[machines.get(i).getTarget()].getWetnessSensor())){
                        System.out.println("Задание отменено [Температура: " +
                                flowerbeds[machines.get(i).getTarget()].getTemperatureSensor() + ", " + "Влажность: " +
                                flowerbeds[machines.get(i).getTarget()].getWetnessSensor() + "] " + ", машина " + machines.get(i).getIndex() + " стоит");
                        machines.get(i).free();
                        availableMachines.add(machines.get(i));
                    }
                    else
                        busyMachines.add(machines.get(i));
                }
            }
        }

        private void flowerbedsForWashing(){
            for (int i=0; i<flowerbedCount; i++){
                if (!flowerbeds[i].isWaiting())
                    availableFlowerbeds.add(i);
            }
        }

        //выдача заданий свободным машинам
        private void giveTasks(){
            int flowerbed;
            for (int i=0; i<availableMachines.size(); i++) {
                flowerbed = pickFlowerbed(machines.indexOf(availableMachines.get(i)), availableFlowerbeds);
                if (flowerbed != -1){
                    Machine machine = availableMachines.get(i);
                    System.out.println("Машина " + machine.getIndex() + " поехала от клумбы " + machine.getCurrentPosition() + " к клумбе " + flowerbed);
                    System.out.println("Температура: " + flowerbeds[flowerbed].getTemperatureSensor() +
                            " Влажность: " + flowerbeds[flowerbed].getWetnessSensor());
                    int timeToMove = distances[flowerbed][machine.getCurrentPosition()];
                    machine.init(timeToMove, flowerbeds[flowerbed].getTimeToShower(), flowerbed);
                    machine.dosmth();
                }
                else
                    break;
            }
        }

        //продолжение работы остальных машин
        private void continueOldTasks() {
            for (int i=0; i<busyMachines.size(); i++) {
                if (busyMachines.get(i).dosmth()){
                    Flowerbed tmpflowerbed = flowerbeds[busyMachines.get(i).getTarget()];
                    tmpflowerbed.setWaiting(false);
                    tmpflowerbed.setRestTime(restTime);
                    System.out.println("Машина " + busyMachines.get(i).getIndex() + " закончила полив клумбы " + busyMachines.get(i).getTarget());
                    busyMachines.get(i).free();
                }

            }
        }
    }
}
