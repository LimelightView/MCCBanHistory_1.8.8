package com.tutanota.lime_light.banHistory.commands;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

public class CommandBanHistory implements ICommand
{
    @Override
    public String getCommandName() { return "history"; }

    @Override
    public String getCommandUsage(ICommandSender sender) { return "/history <name>"; }

    @Override
    public List<String> getCommandAliases()
    {
        List<String> aliases = new ArrayList<String>();
        aliases.add("banHistory");
        aliases.add("muteHistory");
        aliases.add("punishmentHistory");
        aliases.add("his");
        return aliases;
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) throws CommandException
    {
        try
        {
            if(args[0].length() > 0)         // Checks if an argument was received
            {
                final String name = args[0]; // Grabs the username
                BeginLoading(name, sender);  // Starts fetching the player's history
            }
        }catch(ArrayIndexOutOfBoundsException e)
        {
            sender.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "[MCC History] : " + getCommandUsage(sender)));
        }
    }

    @Override
    public boolean canCommandSenderUseCommand(ICommandSender sender) { return true; }

    @Override
    public List<String> addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos)
    {
        return null;
    }

    @Override
    public boolean isUsernameIndex(String[] args, int index) { return false; }

    @Override
    public int compareTo(ICommand o) { return 0; }

    /*
        @param username: Username of the player whose history should be loaded
        @Description   : Gathers the punishment data of a player on MCCentral.org, and returns an array containing the data
     */
    private static String[] LoadHistory(String username) throws IOException
    {
        URL url = new URL("http://mc-central.net/newbans/username.php?username=" + username); // Punishment search URL
        URLConnection connection = url.openConnection();                                            // Opens the URL connection
        InputStream stream = connection.getInputStream();                                           // Creates an input stream to grab the data
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));                  // Creates a BufferedReader to read the data
        String line;
        String[] history = null;
        while((line = reader.readLine()) != null)                                                   // Runs while there is data to be read
        {
            if(line.contains("<td>"))
            {
                history = line.replaceAll("</td>", "")                            // Removes unnecessary characters
                              .replaceAll("<tr>", "")
                              .replaceAll("</tr>", "")
                              .replaceAll("</table>", "")
                              .replaceAll("<br>", "")
                              .split("<td>");
            }
        }
        return history;
    }

    /*
        @param username: Name of the player whose punishment history should be displayed
        @param sender  : The ICommandSender that sent the command
        @Description   : Sends the gathered punishment history to the sender's chat
     */
    private static void DisplayHistory(String username, ICommandSender sender)
    {
        String message = "";
        String[] history = null;
        StringBuffer buffer = new StringBuffer(message); // Creates a StringBuffer which is thread safe
        try {
            history = LoadHistory(username);             // Loads the player's history
            int counter = 0;
            sender.addChatMessage(new ChatComponentText(
                    EnumChatFormatting.RED + "[MCC HISTORY] : " + EnumChatFormatting.WHITE + history[1]));
            for(int i = 0; i < history.length; i++)      // Loops through the data
            {
                counter++;
                switch(counter)
                {
                    case 0:
                        break;
                    case 1:
                        break;
                    case 2:
                        break;
                    case 3: // Sends the Punishment Category
                        buffer.append(EnumChatFormatting.GREEN).append("*").append(history[i]).append(" ");
                        break;
                    case 4: // Sends the Punishment Severity
                        buffer.append("(").append(history[i]).append(") | ").append(EnumChatFormatting.RESET);
                        break;
                    case 5: // Sends the Punishment Reason
                        buffer.append(history[i]).append(" | ");
                        break;
                    case 6: // Sends the time until the Punishment Expires
                        if(history[i].equals("Expired"))
                        {
                            sender.addChatMessage(new ChatComponentText(buffer.toString() + EnumChatFormatting.RED + history[i]));
                        }
                        else
                        {
                            sender.addChatMessage(new ChatComponentText(buffer.toString() + EnumChatFormatting.YELLOW + history[i]));
                        }
                        buffer.setLength(0); // Resets the buffer
                        counter = 0;
                        break;
                    default:
                        break;
                }
            }
            sender.addChatMessage(new ChatComponentText(
                    EnumChatFormatting.WHITE + "---------------------END HISTORY---------------------"));
        } catch (IOException e)
        {
            e.printStackTrace();
        } catch (NullPointerException e) // Fires if the URL could not be contacted
        {
            sender.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "[MCC HISTORY] : "
                    + EnumChatFormatting.WHITE + username + " could not be found. Please make sure you have spelled the" +
                    " name correctly."));
            e.printStackTrace();
        } catch (Exception e)
        {
            sender.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "[MCC HISTORY] : "
                    + EnumChatFormatting.WHITE + username + " could not be found. Please make sure you have spelled the" +
                    " name correctly.")); // Sends the StringBuffer string to chat
            e.printStackTrace();
        }
    }

    /*
        @param username: Username of the player whose punishment history should be collected
        @param sender  : The ICommandSender who sent the command
        @Description   : Creates the thread needed to keep Minecraft from lagging while fetching the history
     */
    private static void BeginLoading(final String username, final ICommandSender sender)
    {
        Thread historyThread = new Thread()
        {
            public void run()
            {
                DisplayHistory(username, sender);
            }
        };
        historyThread.start();
    }
}
