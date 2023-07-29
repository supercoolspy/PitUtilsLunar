package io.github.usemsedge;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.util.ChatComponentText;
import net.weavemc.loader.api.ModInitializer;
import net.weavemc.loader.api.command.CommandBus;
import net.weavemc.loader.api.event.EventBus;

import java.io.*;
import java.net.URL;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class PitUtils implements ModInitializer {
    static HashMap<String,String> enchants = new HashMap<>();
    static HashMap<String,String> enchants_short = new HashMap<>();
    private static final String PIT_UTILS_PATH = "PitUtils.dat";
    static boolean loggedIn = false;
    static boolean usingLabyMod = false;
    static boolean isInPit = false;

    static boolean useShortEnchants = false;
    static boolean enchantsLoaded = false;

    //static Dictionary<String, String> enchants = new Hashtable<String, String>();
    //pretty things can come later

    static ArrayList<String> permList = new ArrayList<>();

    private static ScheduledExecutorService autoSaveExecutor;

    static String LOG_PATH = "PitUtils.log";

    static void saveLogInfo(String log) {
        /*
        new Thread(() -> {
            File mystic_file = new File(LOG_PATH);
            try {
                FileWriter fw = new FileWriter(mystic_file, true);
                fw.write(log);
                fw.close();
            }

            catch(IOException e){
                e.printStackTrace();
            }}).start();
         */
        return;
    }

    static void scheduleFileSave() {
        if (autoSaveExecutor != null && !autoSaveExecutor.isShutdown()) {
            autoSaveExecutor.shutdownNow();
        }

        autoSaveExecutor = Executors.newSingleThreadScheduledExecutor();
        autoSaveExecutor.scheduleAtFixedRate(() -> {
            if (loggedIn && isInPit) {
                saveInfo();
            }
        }, 0, 120, TimeUnit.SECONDS);

    }

    static String getLore(ItemStack item) {
        StringBuilder loreBuilder = new StringBuilder();
        NBTTagList list = item.getTagCompound().getCompoundTag("display").getTagList("Lore", 8);
        for (int i = 0; i < list.tagCount(); i++) {
            loreBuilder.append(" " + list.getStringTagAt(i));
        }
        return loreBuilder.toString();
    }

    static HashMap<String, Integer> getEnchants(ItemStack item) {

        HashMap<String, Integer> enchants = new HashMap<>();

        NBTTagList list = item.getTagCompound().getCompoundTag("ExtraAttributes").getTagList("CustomEnchants", 10);
        for (int i = 0; i < list.tagCount(); i++) {
            enchants.put(list.getCompoundTagAt(i).getString("Key"), list.getCompoundTagAt(i).getInteger("Level"));
        }
        return enchants;
    }

    static List<String> getPlayerNames() {
        List<String> x = new ArrayList<>();
        for (EntityPlayer player : Minecraft.getMinecraft().theWorld.playerEntities) {
            x.add(player.getName());
        }
        return x;

/*
        NetHandlerPlayClient nhpc = Minecraft.getMinecraft().thePlayer.sendQueue;
        List<NetworkPlayerInfo> list =
                new GuiPlayerTabOverlay(Minecraft.getMinecraft(), Minecraft.getMinecraft().ingameGUI)*/
    }

    static void saveInfo() {
        new Thread(() -> {
            File util_file = new File(PIT_UTILS_PATH);
            StringBuilder permListString = new StringBuilder();
            for (String s : permList) {
                permListString.append(s).append(",");
            }


            try {
                FileWriter fw = new FileWriter(util_file, false);
                fw.write(permListString + ";" +
                        MysticDropCounter.toggled + "," + MysticDropCounter.killCount + "," + MysticDropCounter.mysticDrops + "," + MysticDropCounter.sinceLastMysticDrop
                                + "," + MysticDropCounter.guiLocation[0] + "," + MysticDropCounter.guiLocation[1] + "," + MysticDropCounter.align + "," + MysticDropCounter.color + ";" +

                         Cooldown.toggled + "," + Cooldown.guiLocation[0] + "," + Cooldown.guiLocation[1] + "," + Cooldown.align + "," + Cooldown.color + ";" +

                         AutoL.toggled + "," + AutoL.onBan + "," + AutoL.onPermList + "," + AutoL.onBountyClaimed + ";" +

                         PermTracker.toggled + "," + PermTracker.sayInChat + "," + PermTracker.guiLocation[0] + "," + PermTracker.guiLocation[1] + "," + PermTracker.align + "," + PermTracker.color + ";" +

                         DarkChecker.toggled + "," + DarkChecker.sayInChat + "," + DarkChecker.guiLocation[0] + "," + DarkChecker.guiLocation[1] + "," + DarkChecker.align + "," + DarkChecker.color + ";" +

                         CountingPlayers.toggled + "," + CountingPlayers.guiLocation[0] + "," + CountingPlayers.guiLocation[1] + "," + CountingPlayers.align + "," + CountingPlayers.color + ";" +

                         LowLifeMystics.toggled + "," + LowLifeMystics.livesToAlert + "," + LowLifeMystics.guiLocation[0] + "," + LowLifeMystics.guiLocation[1] + "," + LowLifeMystics.align + "," + LowLifeMystics.color
                );
                fw.close();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    static boolean isInteger(String s) {
        return isInteger(s, 10);
    }

    static boolean isInteger(String s, int radix) {
        if (s.isEmpty()) {
            return false;
        }
        for (int i = 0; i < s.length() - 1; i++) {
            if (i == 0 && s.charAt(i) == '-') {
                if (s.length() == 1) {
                    return false;
                } else {
                    continue;
                }
            }
            if (Character.digit(s.charAt(i), radix) < 0) {
                return false;
            }
        }
        return true;
    }

    static boolean isBool(String s) {
        return s.equalsIgnoreCase("true") || s.equalsIgnoreCase("false");
    }

    static List<String> getSidebarLines() {
        List<String> lines = new ArrayList<>();
        Scoreboard sb = Minecraft.getMinecraft().theWorld.getScoreboard();

        if (sb == null) {
            return lines;
        }

        ScoreObjective obj = sb.getObjectiveInDisplaySlot(1);

        if (obj == null) {
            return lines;
        }

        Collection<Score> scores = sb.getSortedScores(obj);
        List<Score> list = Lists.newArrayList(scores.stream()
                .filter(input -> input != null && input.getPlayerName() != null && !input.getPlayerName()
                        .startsWith("#")).collect(Collectors.toList())) ;

        if (list.size() > 15) {
            scores = Lists.newArrayList(Iterables.skip(list, scores.size() - 15));
        }
        else {
            scores = list;
        }

        for (Score score : scores) {
            ScorePlayerTeam team = sb.getPlayersTeam(score.getPlayerName());
            lines.add(ScorePlayerTeam.formatPlayerName(team, score.getPlayerName()));
        }

        return lines;
    }

    static void messagePlayer(String message) {
        Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(message));
    }

    static boolean checkUsername(String name) {
        return name.matches("^[A-Za-z0-9_]*$");
    }


    @Override
    public void preInit() {
        EventBus.subscribe(new PitUtilsEventHandler());

        try {
            URL enchants_url = new URL("https://raw.githubusercontent.com/usemsedge/PitUtils/main/enchants_mystics.txt");
            enchantToHashmap(enchants_url, enchants);
            PitUtils.saveLogInfo("enchants loaded: here is the list of things\n\n");
            for (String k : enchants.keySet()) {
                PitUtils.saveLogInfo(k + ":" + enchants.get(k) + "\n");
            }
        }
        catch (Exception e) {
            enchantsLoaded = false;
            PitUtils.saveLogInfo("enchants failed to load\n");
        }

        try {
            URL enchants_url = new URL("https://raw.githubusercontent.com/usemsedge/PitUtils/main/enchants_mystics_short.txt");
            enchantToHashmap(enchants_url, enchants_short);
            PitUtils.saveLogInfo("enchants short loaded: here is the list of things\n\n");
            for (String k : enchants_short.keySet()) {
                PitUtils.saveLogInfo(k + ":" + enchants_short.get(k) + "\n");
            }
        }
        catch (Exception e) {
            enchantsLoaded = false;
            PitUtils.saveLogInfo("enchants short failed to load\n");
        }


        if (new File(PIT_UTILS_PATH).isFile()) {
            try {
                String[] content = new BufferedReader(new FileReader(PIT_UTILS_PATH)).readLine().split(";");
                saveLogInfo("Opened file, her was the content of the file");
                for (String s : content) {
                    saveLogInfo(s + "\n");
                }

                String[] c = content[0].split(",");
                PitUtils.permList.addAll(Arrays.asList(c));

                saveLogInfo("perm list set \n");
                MysticDropCounter.setVars(content[1]);
                saveLogInfo("mystic drop set \n");
                Cooldown.setVars(content[2]);
                saveLogInfo("cooldown set \n");
                AutoL.setVars(content[3]);
                saveLogInfo("autol set \n");
                PermTracker.setVars(content[4]);
                saveLogInfo("perm tracker set");
                DarkChecker.setVars(content[5]);
                saveLogInfo("darkchecker set");
                CountingPlayers.setVars(content[6]);
                saveLogInfo("counting players set");
                LowLifeMystics.setVars(content[7]);
                saveLogInfo("low life mystics set");

            }
            catch (Exception e) {
                e.printStackTrace();
                saveLogInfo("opening data file failed\n");
                saveInfo();
            }
        }
        else {
            saveLogInfo("no data file exists\n");
            saveInfo();
        }
        scheduleFileSave();
        CommandBus.register(new PitUtilsCommand());
    }

    private static void enchantToHashmap(URL enchants_url, HashMap<String, String> enchants) throws IOException {
        InputStream is = enchants_url.openStream();
        int ptr;
        StringBuilder current = new StringBuilder();
        while ((ptr = is.read()) != -1) {
            if (ptr == 10) {
                enchants.put(current.substring(0, current.indexOf(":")), current.substring(current.indexOf(":") + 1));
                current = new StringBuilder();
            }
            else {
                current.append((char) ptr);
            }
        }
        is.close();
        enchantsLoaded = true;
    }
}