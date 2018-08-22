package com.tutanota.lime_light.banHistory.commands;

import com.google.common.collect.Lists;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.event.ClickEvent;
import net.minecraft.event.HoverEvent;
import net.minecraft.util.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

public class CommandBanHistory extends CommandBase
{
    @Override
    public String getCommandName() { return "history"; }

    @Override
    public String getCommandUsage(ICommandSender sender) { return "/history <name>"; }

    @Override
    public List<String> getCommandAliases()
    {
        List<String> aliases = new ArrayList<String>();
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
        NetHandlerPlayClient netHandler = Minecraft.getMinecraft().thePlayer.sendQueue; // Creates the connection to the server
        List<NetworkPlayerInfo> playerInfo = new ArrayList(netHandler.getPlayerInfoMap()); // Grabs the player info
        List<String> playerList = Lists.<String>newArrayList(); // Creates a list to hold player names

        // Loops through the player info
        for (int i = 0; i < playerInfo.size(); ++i)
        {
            if (i < playerInfo.size())
            {
                playerList.add(playerInfo.get(i).getGameProfile().getName()); // Adds the player name to the list
            }
        }
        return getListOfStringsMatchingLastWord(args, playerList);
    }

    @Override
    public boolean isUsernameIndex(String[] args, int index) { return false; }

    @Override
    public int compareTo(ICommand o) { return 0; }

    /*
        @param url  : url used to contact the check punishment server
        @Description: Gathers the punishment data of a player on MCCentral.org, and returns an array containing the data
     */
    private static String[] LoadHistory(URL url) throws IOException
    {
        URLConnection connection = url.openConnection();                           // Opens the URL connection
        InputStream stream = connection.getInputStream();                          // Creates an input stream to grab the data
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream)); // Creates a BufferedReader to read the data
        String line;
        String[] history = null;

        while((line = reader.readLine()) != null)                                  // Runs while there is data to be read
        {
            if(line.contains("<td>"))
            {
                history = line.replaceAll("</td>", "")           // Removes unnecessary characters
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
    private static void DisplayHistory(String username, ICommandSender sender) throws MalformedURLException {
        String message = "";
        StringBuffer buffer = new StringBuffer(message);                                            // Creates a StringBuffer which is thread safe
        URL url = new URL("http://mc-central.net/newbans/username.php?username=" + username); // Punishment search URL
        String[] history = null;
        int counter = 0;

        try {
            history = LoadHistory(url);             // Loads the player's history
        } catch (IOException e)
        {
            e.printStackTrace();
        }

        // @Top Dividing Line
        buffer = SendDividingLine(buffer);
        sender.addChatMessage(new ChatComponentText(buffer.toString()));
        buffer.setLength(0);

        // @Punishment Page Link
        IChatComponent comp = new ChatComponentText(EnumChatFormatting.GREEN + username + "'s Punishment History");
        IChatComponent compHover = new ChatComponentText(EnumChatFormatting.GRAY + "Click to view " + username + "'s Punishment Page.");
        ClickEvent click = new ClickEvent(ClickEvent.Action.OPEN_URL, url.toString());       // Creates the click event
        HoverEvent hover = new HoverEvent(HoverEvent.Action.SHOW_TEXT, compHover);           // Creates the hover event
        ChatStyle style = new ChatStyle().setChatClickEvent(click).setChatHoverEvent(hover); // Adds the click and hover events to the chat style
        comp.setChatStyle(style);                                                            // Sets the chat style of the message
        sender.addChatMessage(comp);                                                         // Sends the message to the chat

        // @Punishment History
        if(history != null)
        {
            for(int i = 0; i < history.length; i++)      // Loops through the data
            {
                counter++;
                switch(counter)
                {
                    case 1: // Skips the username of the staff member
                        break;
                    case 2: // Adds the [*] to the beginning of the text
                        buffer.append(EnumChatFormatting.DARK_GRAY)
                                .append("[")
                                .append(GetBanLength(history[i+4]))
                                .append("*")
                                .append(EnumChatFormatting.DARK_GRAY)
                                .append("] ");
                        break;
                    case 3: // Sends the Punishment Category
                        buffer.append(EnumChatFormatting.DARK_GRAY)
                                .append("[")
                                .append(EnumChatFormatting.WHITE)
                                .append(ShortenCategory(history[i]))
                                .append(EnumChatFormatting.DARK_GRAY)
                                .append("] ");
                        break;
                    case 4:
                        break;
                    case 5: // Sends the Punishment Reason
                        buffer.append(EnumChatFormatting.GRAY).append(history[i]);
                        break;
                    case 6: // Sends the message
                        IChatComponent punishedByComp;
                        if(!history[i].equals("Expired"))
                        {
                            punishedByComp = new ChatComponentText(EnumChatFormatting.GREEN + "Punished by " + history[i+1] + "\n"
                                    + EnumChatFormatting.GREEN + "Time Remaining: " + EnumChatFormatting.YELLOW + history[i]);     // Hover text
                        }else
                        {
                            punishedByComp = new ChatComponentText(EnumChatFormatting.GREEN + "Punished by " + history[i+1]); // Hover text
                        }
                        IChatComponent punishment = new ChatComponentText(buffer.toString());                                      // Punishment text
                        HoverEvent punishedByHover = new HoverEvent(HoverEvent.Action.SHOW_TEXT, punishedByComp);                  // Creating hover event
                        ChatStyle punishmentStyle = new ChatStyle().setChatHoverEvent(punishedByHover);                            // Adding hover event to the style
                        punishment.setChatStyle(punishmentStyle);                                                                  // Adding style to punishment text
                        sender.addChatMessage(punishment);                                                                         // Sending the message
                        buffer.setLength(0);                                                                                       // Resets the buffer
                        counter = 0;
                        break;
                    default:
                        break;
                }
            }
        }


        // @Bottom Dividing Line
        buffer = SendDividingLine(buffer);
        sender.addChatMessage(new ChatComponentText(buffer.toString()));
        buffer.setLength(0);
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
                try {
                    DisplayHistory(username, sender);
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
            }
        };
        historyThread.start();
    }

    /*
        @param category: String representing the type of punishment
        @Description   : Returns a shorter version of the received category
     */
    private static String ShortenCategory(String category)
    {
        if(category.equals("Client Modification"))
        {
             return "Client";
        }else if(category.equals("Chat Offence"))
        {
            return "Chat";
        }else if(category.equals("Gameplay Offense"))
        {
            return "Gameplay";
        }else
        {
            return category;
        }
    }

    /*
        @param length: Amount of time left until the punishment expires
        @Description : Returns an EnumChatFormatting (color) depending on whether or not a punishment has expired
     */
    private static EnumChatFormatting GetBanLength(String length)
    {
        if(length.equals("Expired"))
        {
            return EnumChatFormatting.RED;
        }else
        {
            return EnumChatFormatting.GREEN;
        }
    }

    private static StringBuffer SendDividingLine(StringBuffer buffer)
    {
        buffer.append(EnumChatFormatting.DARK_GREEN)
                .append("]")
                .append(EnumChatFormatting.STRIKETHROUGH)
                .append("----------------------------------------------")
                .append(EnumChatFormatting.RESET)
                .append(EnumChatFormatting.DARK_GREEN)
                .append("[");
        return buffer;
    }
}
