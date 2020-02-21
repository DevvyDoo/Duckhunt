package me.devvydoo.duckhunt.util;

import me.devvydoo.duckhunt.game.DuckhuntGame;
import org.bukkit.Location;
import org.bukkit.World;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Scanner;

public class DuckhuntFile {

    private World world;
    private String filepath;
    private Location hunterSpawn;
    private Location runnerSpawn;
    private Location spectatorSpawn;
    private Location lobbySpawn;
    private int timeSeconds = 180;

    public Location getHunterSpawn() {
        return hunterSpawn;
    }

    public Location getRunnerSpawn() {
        return runnerSpawn;
    }

    public Location getSpectatorSpawn() {
        return spectatorSpawn;
    }

    public Location getLobbySpawn() {
        return lobbySpawn;
    }

    public int getTimeSeconds() {
        return timeSeconds;
    }

    private String serializeLocation(Location location, String type){
        StringBuilder stringBuilder = new StringBuilder(type + ": ");
        stringBuilder.append(location.getBlockX()).append(",");
        stringBuilder.append(location.getBlockY()).append(",");
        stringBuilder.append(location.getBlockZ()).append(",");
        stringBuilder.append(Math.round(location.getYaw())).append(",");
        stringBuilder.append(5);
        return stringBuilder.toString();
    }

    private Location deserializeLocation(String input) throws  NumberFormatException {
        String[] splitArgs = input.split(",");
        float[] splitNums = new float[splitArgs.length];
        for (int i = 0; i < splitArgs.length; i++)
            splitNums[i] = Float.parseFloat(splitArgs[i]);
        return new Location(world, splitNums[0], splitNums[1], splitNums[2], splitNums[3], splitNums[4]);
    }

    public void parseFile(){
        try (Scanner scanner = new Scanner(new File(filepath + "/duckhunt.options"))){

            while (scanner.hasNextLine()){
                String line = scanner.nextLine();

                if (line.startsWith("hunter: ")){
                    line = line.replace("hunter: ", "");
                    hunterSpawn = deserializeLocation(line);
                }
                else if (line.startsWith("runner: ")){
                    line = line.replace("runner: ", "");
                    runnerSpawn = deserializeLocation(line);
                }
                else if (line.startsWith("spectator: ")){
                    line = line.replace("spectator: ", "");
                    spectatorSpawn = deserializeLocation(line);
                }
                else if (line.startsWith("lobby: ")) {
                    line = line.replace("lobby: ", "");
                    lobbySpawn = deserializeLocation(line);
                }
                else if (line.startsWith("time: ")){
                    line = line.replace("time: ", "");
                    timeSeconds = Integer.parseInt(line);
                }

            }


        } catch (FileNotFoundException | NumberFormatException ignored){
            makeNewFile();
        }
    }

    private void makeNewFile(){

        if (!shouldSaveToFile())
            return;

        try (PrintWriter printWriter = new PrintWriter(filepath + "/duckhunt.options")){

            printWriter.println(serializeLocation(hunterSpawn, "hunter"));
            printWriter.println(serializeLocation(runnerSpawn, "runner"));
            printWriter.println(serializeLocation(spectatorSpawn, "spectator"));
            printWriter.println(serializeLocation(lobbySpawn, "lobby"));
            printWriter.println("time: " + timeSeconds);

        }catch (IOException e){
            e.printStackTrace();
        }


    }

    public boolean shouldSaveToFile(){
        return lobbySpawn != null && spectatorSpawn != null && runnerSpawn != null && hunterSpawn != null & timeSeconds > 0;
    }

    public void updateLocations(DuckhuntGame game){
        lobbySpawn = game.getLobbySpawn();
        spectatorSpawn = game.getSpectatorSpawn();
        runnerSpawn = game.getDuckSpawn();
        hunterSpawn = game.getHunterSpawn();
        timeSeconds = game.getTimeLimit();
        if (shouldSaveToFile())
            makeNewFile();
    }

    /**
     * A pending file to save in a world file
     *
     * @param world The world object
     * @param filepath The path to the world in the server folder, 'server/world/'
     */
    public DuckhuntFile(World world, String filepath) {
        this.world = world;
        this.filepath = filepath;
    }
}
