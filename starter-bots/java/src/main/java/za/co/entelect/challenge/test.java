package za.co.entelect.challenge;
import za.co.entelect.challenge.command.*;
import za.co.entelect.challenge.entities.*;
import za.co.entelect.challenge.enums.CellType;
import za.co.entelect.challenge.enums.Direction;

import java.io.*;
import java.util.*;
import com.google.gson.Gson;
import za.co.entelect.challenge.command.Command;
import za.co.entelect.challenge.entities.GameState;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Random;
import java.util.Scanner;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import za.co.entelect.challenge.command.*;
import za.co.entelect.challenge.entities.*;
import za.co.entelect.challenge.enums.CellType;
import za.co.entelect.challenge.enums.Direction;

import java.util.*;
import java.util.stream.Collectors;
import java.io.*;
public class test {
    public static void main (String[] args) {
        Scanner sc = new Scanner(System.in);
        Gson gson = new Gson();
        Random random = new Random(System.nanoTime());
        System.out.println("b1");

        //while (true) {
            System.out.println("b2");
            try {
                System.out.println("b3");

                String state = new String(Files.readAllBytes(Paths.get("./test.json")));
                System.out.println(state);
                GameState gameState = gson.fromJson(state, GameState.class);
                System.out.println(gameState.map[1][1]);

                //GameState gameState = gson.fromJson(state, GameState.class);
                Bot testBot = new Bot(random, gameState);


            } catch (Exception e) {
                e.printStackTrace();
            }
    }
}
