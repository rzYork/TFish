package com.tracer0219;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TCommandExecutor implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args) {
        if(s.equalsIgnoreCase("tf")){
            if(args.length==1){
                if(args[0].equalsIgnoreCase("help")){
                    commandSender.sendMessage("/tf help 获取帮助");
                    commandSender.sendMessage("/tf list 查看正在钓鱼区域的玩家");
                    return true;
                }else if(args[0].equals("list")){
                    commandSender.sendMessage("钓鱼玩家列表 (红名为vip用户)");
                    for (Player player : TListener.playerFishing) {
                        commandSender.sendMessage((player.hasPermission("tf.vip")? ChatColor.RED:ChatColor.YELLOW)+player.getName());
                    }
                    return true;

                }
            }

                return false;
        }
        return  true;
    }
}
