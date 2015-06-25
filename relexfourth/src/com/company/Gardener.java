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
    private Flowerbed[] flowerbeds;

    public Gardener(InputInfo inputInfo) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        final InputAllSensors inputAllSensors = mapper.readValue(Main.read(Main.inputSensorsValue), InputAllSensors.class);
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
        timer.schedule(worker, 0, 1000);    //запуск задачи с интервалом 1 сек
    }

    //выбор клумбы для полива
    public int pickFlowerbed(List<Flowerbed> availables) {
        List<Integer> readyToWash = new ArrayList<Integer>();
        for (int i = 0; i < availables.size(); i++) {
            if ((availables.get(i).getWetnessSensor() < criticalWetnessValue
                    || availables.get(i).getTemperatureSensor() > criticalTemperatureValue) &&
                    availables.get(i).getRestTime() == 0) {
                readyToWash.add(i);
            }
        }
        if (readyToWash.isEmpty())
            return -1;
        int index = readyToWash.get(new Random().nextInt(readyToWash.size()));
        availables.get(index).setWaiting(true);
        return Arrays.asList(flowerbeds).indexOf(availables.get(index));
    }

    //обратный отсчет до следующего полива
    public void updateFlowerbeds() {
        for (int i = 0; i < flowerbedCount; i++) {
            if (flowerbeds[i].getRestTime() != 0)
                flowerbeds[i].setRestTime(flowerbeds[i].getRestTime() - 1);
        }
    }


    class Worker extends TimerTask{
        private List<Machine> busyMachines = new ArrayList<Machine>();
        private List<Machine> availableMachines = new ArrayList<Machine>();
        private List<Flowerbed> availableFlowerbeds = new ArrayList<Flowerbed>();

        @Override
        public void run() {
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
                    availableFlowerbeds.add(flowerbeds[i]);
            }
        }

        //выдача заданий свободным машинам
        private void giveTasks(){
            int flowerbed;
            for (int i=0; i<availableMachines.size(); i++) {
                flowerbed = pickFlowerbed(availableFlowerbeds);
                if (flowerbed != -1){
                    Machine machine = availableMachines.get(i);
                    System.out.println("Машина " + machine.getIndex() + " отправлена к клумбе " + flowerbed);
                    System.out.println("Температура: " + flowerbeds[flowerbed].getTemperatureSensor() +
                            " Влажность: " + flowerbeds[flowerbed].getWetnessSensor());
                    availableFlowerbeds.remove(flowerbeds[flowerbed]);
                    machine.init(flowerbeds[flowerbed].getTimeToMove(), flowerbeds[flowerbed].getTimeToShower(), flowerbed);
                    machine.doWork();
                }
                else
                    break;
            }
        }

        //продолжение работы остальных машин
        private void continueOldTasks(){
            for (int i=0; i<busyMachines.size(); i++) {
                if (busyMachines.get(i).doWork()){
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
