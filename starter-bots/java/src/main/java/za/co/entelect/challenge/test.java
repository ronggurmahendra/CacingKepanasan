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
import java.util.Arrays.*;
import za.co.entelect.challenge.command.*;
import za.co.entelect.challenge.entities.*;
import za.co.entelect.challenge.enums.CellType;
import za.co.entelect.challenge.enums.Direction;

import java.util.*;
import java.util.stream.Collectors;
import java.io.*;
//import java.util.
public class test {
    public static void main (String[] args) {
        Scanner sc = new Scanner(System.in);
        Gson gson = new Gson();
        Random random = new Random(System.nanoTime());
        int[][] arr = {{1,2},{3,4},{5,6}};
        System.out.println(arr.length);
            try {
                String state = new String(Files.readAllBytes(Paths.get("./test.json")));
                System.out.println(state);
                GameState gameState = gson.fromJson(state, GameState.class);
                System.out.println(gameState.map[1][1]);

                //GameState gameState = gson.fromJson(state, GameState.class);
                Bot testBot = new Bot(random, gameState);
                modifiedCell[][] testRoute = testBot.shortestRoute(gameState.map,gameState.myPlayer.worms[0].position);
                //System.out.println(distance);

                for(int i = 0;i< testRoute.length;i++){
                    for(int j = 0;j<testRoute[0].length;j++){
                        System.out.print(testRoute[i][j].cell.x);
                        System.out.print(" ");
                        System.out.print(testRoute[i][j].cell.y);
                        System.out.print(" Distance: ");
                        System.out.print(testRoute[i][j].distance);
                        System.out.print(" ");
                        System.out.print(testRoute[i][j].cell.type);
                        System.out.print(" prev : ");
                        System.out.print(testRoute[i][j].prev.x);
                        System.out.print(" ");
                        System.out.print(testRoute[i][j].prev.y);
                        System.out.print(" ");
                        System.out.print("Visited : ");
                        System.out.print(testRoute[i][j].visit);
                        System.out.println(" ");
                    }
                }
                Position testPos = testBot.getShortestFirstRoute(testRoute, new Position(24,10));

            } catch (Exception e) {
                e.printStackTrace();
            }
    }
}
