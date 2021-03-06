package io.github.rudeyeti.serveractivity.utils;

import io.github.rudeyeti.serveractivity.Plugins;
import io.github.rudeyeti.serveractivity.ServerActivity;
import org.apache.commons.lang.math.NumberUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class Generate {
    public static void file(CommandSender sender, String time) {
        try {
            File dataFolder = ServerActivity.plugin.getDataFolder();
            File file = new File(dataFolder, "activity.txt");
            String timeString = Time.get(time);

            // Making sure that the time string is valid.
            if (!NumberUtils.isDigits(timeString)) {
                sender.sendMessage(ChatColor.RED + timeString);
                return;
            }

            // Making sure that no files are being overwritten, so instead it makes a new one.
            for (int i = 1; file.exists(); i++) {
                file = new File(dataFolder, "activity" + i + ".txt");
            }

            file.createNewFile();
            FileWriter fileWriter = new FileWriter(file);

            // Message so you know the command was received properly while it's generating the file.
            sender.sendMessage("The file " + file.getName() + " is being generated, please wait...");

            activity(timeString).forEach((line) -> {
                try {
                    fileWriter.write(line + System.lineSeparator());
                } catch (IOException error) {
                    error.printStackTrace();
                }
            });

            fileWriter.close();
            sender.sendMessage("The file " + file.getName() + " has been successfully generated.");
        } catch (IOException error) {
            error.printStackTrace();
        }
    }

    public static List<String> activity(String time) {
        if (Plugins.getCoreProtect() != null) {
            List<String> lines = new ArrayList<>();

            ServerActivity.server.getWhitelistedPlayers().forEach((offlinePlayer) -> {
                int activity = Plugins.getCoreProtect().performLookup(
                        Integer.parseInt(time), Collections.singletonList(offlinePlayer.getName()), null, null, null, null, 0, null
                ).size();

                // Formula to count the pages of CoreProtect entries, which is basically the total divided by four.
                int pages = activity > 0 ? (int) Math.ceil((activity + 1) / 4.0) : 0;

                lines.add(offlinePlayer.getName() + " - " + pages);
            });

            lines.sort(new Comparator<String>() {
                public int compare(String line1, String line2) {
                    // Making sure that there are no repeats in the whitelist.
                    return line1.equals(line2) ? extractInt(line1) : extractInt(line2) - extractInt(line1);
                }

                int extractInt(String line) {
                    // Extracting the number after the Minecraft username.
                    String number = line.replaceAll(".+ - ", "");
                    return Integer.parseInt(number);
                }
            });

            return lines;
        }
        return null;
    }
}
