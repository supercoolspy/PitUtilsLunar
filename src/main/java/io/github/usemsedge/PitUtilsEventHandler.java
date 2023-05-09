package io.github.usemsedge;

import club.maxstats.weave.loader.api.event.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StringUtils;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class PitUtilsEventHandler {
    private boolean firstJoin = true;
    private FontRenderer renderer = Minecraft.getMinecraft().fontRendererObj;
    private int tick = 0;

    @SubscribeEvent
    public void onPlayerClick(MouseEvent e) {
        if (e.getButton() != 1) return;
        if (Cooldown.toggled) {
            Cooldown.onPlayerClick(e);
        }

    }


    @SubscribeEvent
    public void onChatMessageRecieved(ChatReceivedEvent e) {

        String msg = StringUtils.stripControlCodes(e.getMessage().getUnformattedText());
        MysticDropCounter.onChatMessageReceived(msg);
        if (AutoL.toggled) {
            AutoL.checkIfSayL(msg);
        }
    }

    @SubscribeEvent
    public void onTick(TickEvent e) {
        tick++;
        if (tick > 9 && Minecraft.getMinecraft() != null
                && Minecraft.getMinecraft().thePlayer != null) {
            if (Minecraft.getMinecraft().getCurrentServerData() != null
                    && Minecraft.getMinecraft().getCurrentServerData().serverIP != null
                    && Minecraft.getMinecraft().theWorld.getScoreboard().getObjectiveInDisplaySlot(1)
                    != null) {
                PitUtils.isInPit = (stripString(StringUtils.stripControlCodes(
                        Minecraft.getMinecraft().theWorld.getScoreboard().getObjectiveInDisplaySlot(1)
                                .getDisplayName())).contains("THE HYPIXEL PIT") && Minecraft.getMinecraft()
                        .getCurrentServerData().serverIP.toLowerCase().contains("hypixel.net"));
            }
            PermTracker.permedPlayersInServer = PermTracker.findPermedPlayersInServer();
            DarkChecker.playersUsingDarksInServer = DarkChecker.checkForDarks();
            CountingPlayers.checkGear();
            CountingPlayers.updateCount();
            LowLifeMystics.checkAllLives();

            tick = 0;
        }

        Cooldown.adjustCooldowns();
    }

    @SubscribeEvent
    public void onPlayerJoinevent(ServerConnectEvent event) {
        PitUtils.loggedIn = true;
        //TODO: Change message to say lunar one is not a rat
        new ScheduledThreadPoolExecutor(1).schedule(() -> Minecraft.getMinecraft().thePlayer
                .addChatMessage(new ChatComponentText(EnumChatFormatting.RED +
                "Downloads not from github.com/usemsedge/mystic-counter are RATs.\n" + EnumChatFormatting.GREEN + "Type /pit help to get a list of commands.")), 3, TimeUnit.SECONDS);
    }

    @SubscribeEvent
    public void renderGameOverlayEvent(RenderGameOverlayEvent.Post event) {
        if (!PitUtils.isInPit) return;
        renderStats();
    }

    private String stripString(String s) {
        char[] nonValidatedString = StringUtils.stripControlCodes(s).toCharArray();
        StringBuilder validated = new StringBuilder();
        for (char a : nonValidatedString) {
            if ((int) a < 127 && (int) a > 20) {
                validated.append(a);
            }
        }
        return validated.toString();
    }

    private boolean isUsingLabymod() {
        return PitUtils.usingLabyMod;
    }

    private void renderStats() {
        if (MysticDropCounter.toggled) {
            MysticDropCounter.renderStats(renderer);
        }
        if (Cooldown.toggled) {
            Cooldown.renderStats(renderer);
        }
        if (PermTracker.toggled) {
            PermTracker.renderStats(renderer);
        }
        if (DarkChecker.toggled) {
            DarkChecker.renderStats(renderer);
        }
        if (CountingPlayers.toggled) {
            CountingPlayers.renderStats(renderer);
        }
        if (LowLifeMystics.toggled) {
            LowLifeMystics.renderStats(renderer);
        }
    }
}