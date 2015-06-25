package com.company;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.Scanner;

public class Main {

    public static String workingDir = System.getProperty("user.dir")+"/resources/";
    public static String inputConfigFile = workingDir + "input.txt";
    public static String inputSensorsValue = workingDir + "sensors.txt";
    private static Random random = new Random();
    public static InputInfo inputInfo;

    public static void main(String[] args) throws IOException, InterruptedException {
        loadData();
        Gardener gardener = new Gardener(inputInfo);
        gardener.work();
    }

    public static int getRand(int lowerbound, int upperbound){
        return random.nextInt(upperbound-lowerbound) + lowerbound;
    }

    public static void loadData() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        inputInfo = mapper.readValue(read(inputConfigFile), InputInfo.class);
        InputAllSensors inputAllSensors = new InputAllSensors();
        for (int i = 0; i < inputInfo.getFlowerbedCount(); i++){
            inputAllSensors.add(new InputSensor(new ArrayList<Integer>(Arrays.asList(getRand(20,40),getRand(40,60),getRand(20,35)
                    ,getRand(10,70),getRand(25,35),getRand(10,35))),
                    new ArrayList<Integer>(Arrays.asList(getRand(60,80),getRand(40,90),getRand(50,80)
                            ,getRand(80,100),getRand(55,80),getRand(40,60))),getRand(2,7)));
        }
        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        String json = ow.writeValueAsString(inputAllSensors);
        write(inputSensorsValue, json);
        System.out.println("Отредактируйте журнал температур и введите ОК");
        Scanner sc = new Scanner(System.in);
        while (!sc.next().equalsIgnoreCase("OK"))
            ;
    }

    public static void write(String fileName, String text){
        File file = new File(fileName);
        try {
            if(!file.exists()){
                file.createNewFile();
            }
            PrintWriter out = new PrintWriter(file.getAbsoluteFile());
            try {
                out.print(text);
            } finally {
                out.close();
            }
        } catch(IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String read(String filename) throws IOException {
        StringBuilder sb = new StringBuilder();
        File file = new File(filename);
        BufferedReader in = new BufferedReader(new FileReader(file.getAbsoluteFile()));
        String s;
        while ((s = in.readLine()) != null) {
            sb.append(s);
            sb.append("\n");
        }
        in.close();
        return sb.toString();
    }
}
