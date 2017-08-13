package net.mcbbs.zzz1999.ChestLocker;

import cn.nukkit.Player;
import cn.nukkit.block.Block;
import cn.nukkit.blockentity.BlockEntity;
import cn.nukkit.blockentity.BlockEntityChest;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.block.*;
import cn.nukkit.event.player.PlayerInteractEvent;
import cn.nukkit.nbt.tag.ListTag;
import cn.nukkit.nbt.tag.StringTag;
import cn.nukkit.scheduler.PluginTask;
import cn.nukkit.utils.TextFormat;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import static cn.nukkit.event.EventPriority.HIGH;

public class EventListener implements Listener {

    private ChestLocker plugin;

    /*private ArrayList<Integer> ChestList = new ArrayList<Integer>(){{
        add(Block.CHEST);
        add(Block.TRAPPED_CHEST);
    }};*/




    EventListener(ChestLocker owner){
        this.plugin = owner;
    }
    @EventHandler
    public void onChestLock(PlayerInteractEvent event){
        if(plugin.getLockSetting().containsKey(event.getPlayer().getName())){
            Block block = event.getBlock();
            Player player = event.getPlayer();
            if(block.getId() == Block.CHEST){
                BlockEntity chestP = block.getLevel().getBlockEntity(block);
                if(chestP instanceof BlockEntityChest){
                    BlockEntityChest chest = (BlockEntityChest)chestP;
                    if(chest.namedTag.contains("Owner")){
                        if(Objects.equals(player.getName(),chest.namedTag.getString("Owner"))){
                            player.sendMessage(TextFormat.GRAY+"[ChestLocker] 这个箱子已经被你锁上过了");
                        }else{
                            player.sendMessage(TextFormat.GRAY+"[ChestLocker] 这个箱子已经被[ "+chest.namedTag.getString("Owner")+" ]锁上了，你无法再次锁定");
                        }
                    }else{
                        chest.namedTag.putString("Owner",player.getName());

                        player.sendMessage(TextFormat.GOLD+"[ChestLocker] 成功锁上箱子，这个箱子的拥有者变更为 [ "+player.getName()+" ]");
                        if(chest.getPair()!=null){
                            BlockEntityChest chestPair = chest.getPair();
                            ChestLocker.getInstance().CopyChestInformation(chest,chestPair);
                        }
                    }
                    //this.plugin.getLogger().info("chest  "+chest.namedTag.getAllTags());
                    //this.plugin.getLogger().info("chestPair   "+ chest.namedTag.getAllTags());
                    Map<String,Boolean> map = plugin.getLockSetting();
                    map.remove(player.getName());
                    plugin.setLockSetting(map);
                    player.sendMessage(TextFormat.GRAY+"[ChestLocker] 退出锁定箱子模式");
                }

            }
        }
    }



    @EventHandler
    @SuppressWarnings("unchecked")
    public void onChestShare(PlayerInteractEvent event){
        if(plugin.getShareSetting().containsKey(event.getPlayer().getName())){
            Block block = event.getBlock();
            Player player = event.getPlayer();
            if(block.getId() == Block.CHEST){
                BlockEntity chestP = block.getLevel().getBlockEntity(block);
                if(chestP instanceof BlockEntityChest){
                    BlockEntityChest chest = (BlockEntityChest)chestP;
                    if(chest.namedTag.contains("Owner")){
                        if(!Objects.equals(player.getName(),chest.namedTag.getString("Owner"))){
                            player.sendMessage(TextFormat.YELLOW+"[ChestLocker] 你不是这个箱子的拥有者");
                            return;
                        }
                        String invite = plugin.getShareSetting().get(player.getName());
                        if(chest.namedTag.contains("Guest")){
                            List list = chest.namedTag.getList("Guest").getAll();

                            if(list.contains(new StringTag("",player.getName()))){
                                player.sendMessage(TextFormat.GRAY+"[ChestLocker] 玩家[ "+ invite+" ] 以存在于共享列表中");
                            }else{
                                /*ArrayList<StringTag> guest = (ArrayList<StringTag>) chest.namedTag.getList("Guest").getAll();
                                guest.add(new StringTag("",invite));
                                chest.namedTag.getList("Guest").setAll(guest);*/
                                List tag = chest.namedTag.getList("Guest").getAll();
                                tag.add(new StringTag("" ,invite));
                                chest.namedTag.getList("Guest").setAll(tag);
                                player.sendMessage(TextFormat.GOLD+"[ChestLocker] 已将 [ "+invite+" ] 列入共享列表");
                                Map<String,String> map = plugin.getShareSetting();
                                map.remove(player.getName());
                                plugin.setShareSetting(map);
                                player.sendMessage(TextFormat.AQUA+"[ChestLocker] 以退出设置箱子共享模式");
                            }
                        }else{
                            chest.namedTag.putList(new ListTag<>("Guest"));
                            List tag = chest.namedTag.getList("Guest").getAll();
                            tag.add(new StringTag("",invite));
                            chest.namedTag.getList("Guest").setAll(tag);
                            player.sendMessage(TextFormat.GOLD+"[ChestLocker] 以设置共享玩家  [ "+invite+" ] .");
                            Map<String,String> map = plugin.getShareSetting();
                            map.remove(player.getName());
                            plugin.setShareSetting(map);
                            player.sendMessage(TextFormat.AQUA+"[ChestLocker] 以退出共享模式");
                        }
                    }else{
                        player.sendMessage(TextFormat.YELLOW+"[ChestLocker] 请先锁定这个箱子再设置共享,该箱子目前没有拥有者");
                    }
                }
            }
        }
    }
    @EventHandler
    @SuppressWarnings("unchecked")
    public void onUnlockChest(PlayerInteractEvent event){
        if(plugin.getUnLockSetting().containsKey(event.getPlayer().getName())){
            Block block = event.getBlock();
            Player player = event.getPlayer();
            if(block.getId() == Block.CHEST){
                BlockEntity chestP = block.getLevel().getBlockEntity(block);
                if(chestP instanceof BlockEntityChest){
                    BlockEntityChest chest = (BlockEntityChest)chestP;
                    if(Objects.equals(player.getName(),chest.namedTag.getString("Owner"))){
                        chest.namedTag.remove("Owner");
                        chest.namedTag.remove("Guest");
                        for(Player p:chest.getInventory().getViewers()){
                            chest.getInventory().onClose(p);
                        }
                        player.sendMessage(TextFormat.RED+"[ChestLocker] 你以解锁该箱子，并且删除所有共享该箱子的玩家信息");
                        Map<String,Boolean> map = plugin.getUnLockSetting();
                        map.remove(player.getName());
                        plugin.setUnLockSetting(map);
                        player.sendMessage(TextFormat.AQUA+"[ChestLocker] 以退出解锁箱子模式");
                    }else{
                        player.sendMessage(TextFormat.YELLOW+"[ChestLocker] 你选择了一个不属于你的箱子，你无法对此进行操作");
                    }

                }
            }
        }
    }
    @EventHandler
    @SuppressWarnings("unchecked")
    public void onUnshareChest(PlayerInteractEvent event){
        if(plugin.getUnshareSetting().containsKey(event.getPlayer().getName())){
            Block block = event.getBlock();
            Player player = event.getPlayer();
            String unshare = this.plugin.getUnshareSetting().get(player.getName());
            if(block .getId()==Block.CHEST){
                BlockEntity chestP =block.getLevel().getBlockEntity(block);
                if(chestP instanceof BlockEntityChest){
                    BlockEntityChest chest = (BlockEntityChest) chestP;
                    if(Objects.equals(chest.namedTag.getString("Owner"),player.getName())){
                        List list =  chest.namedTag.getList("Guest").getAll();
                        if(list.contains(new StringTag("",unshare))){
                            //list.remove(new StringTag("",unshare));
                            List tag = chest.namedTag.getList("Guest").getAll();
                            tag.remove(unshare);
                            chest.namedTag.getList("Guest").setAll(tag);
                            player.sendMessage(TextFormat.GOLD+"[ChestLocker] 以将 [ "+unshare+" ] 移除出箱子共享列表中");
                            //TODO test whether the remove/add were succeed
                            Map<String,String> map = plugin.getUnshareSetting();
                            map.remove(unshare);
                            plugin.setUnshareSetting(map);
                            player.sendMessage(TextFormat.AQUA+"以退出取消箱子共享模式");

                        }else{
                            player.sendMessage(TextFormat.GRAY+"[ChestLocker] "+unshare+"  不在这个箱子的共享列表中");
                        }
                    }
                }
            }
        }

    }
    @EventHandler
    public void DemandChest(PlayerInteractEvent event){
        if(ChestLocker.getInstance().getDemandChest().containsKey(event.getPlayer().getName())){
            if(event.getBlock().getId() == Block.CHEST){
                Block block = event.getBlock();
                if(block.getLevel().getBlockEntity(block) instanceof BlockEntityChest){
                    BlockEntityChest chest = (BlockEntityChest) block.getLevel().getBlockEntity(block);
                    Player p = event.getPlayer();
                    p.sendMessage(TextFormat.LIGHT_PURPLE+"----------------\n["+block.getName()+"]"+block.toString() +" 箱子信息如下:");
                    p.sendMessage(TextFormat.LIGHT_PURPLE+"箱子所有者: ["+(chest.namedTag.exist("Owner") ? chest.namedTag.getString("Owner") : "不存在的")+"]");
                    if(chest.namedTag.exist("Guest")) {
                        List list = chest.namedTag.getList("Guest").getAll();
                        p.sendMessage(TextFormat.LIGHT_PURPLE+"箱子共享者:");
                        for (Object aList : list) {
                            p.sendMessage(TextFormat.LIGHT_PURPLE+String.valueOf(aList));
                        }

                    }
                    p.sendMessage(TextFormat.LIGHT_PURPLE+"----------------");
                    Map<String,Boolean> map = ChestLocker.getInstance().getDemandChest();
                    map.remove(event.getPlayer().getName());
                    this.plugin.setDemandChest(map);
                }
            }else{
                event.getPlayer().sendPopup(TextFormat.GRAY+"查询失败,点击的不是箱子");
            }
        }
    }

    @EventHandler(priority = HIGH)
    public void PlaceChest(BlockPlaceEvent event){
        Block block = event.getBlock();
        if(block.getId() == Block.CHEST)
            this.plugin.getServer().getScheduler().scheduleDelayedTask(new PluginTask<ChestLocker>(this.plugin) {
                //delay 1tick
                @Override
                public void onRun(int i) {
                    try {
                        if (((BlockEntityChest) block.getLevel().getBlockEntity(block)).getPair() != null) {
                            BlockEntityChest chestPair = ((BlockEntityChest) block.getLevel().getBlockEntity(block)).getPair();
                            //BlockEntityChest chest = (BlockEntityChest)block.getLevel().getBlockEntity(block);
                            if (chestPair.namedTag.exist("Owner") && !Objects.equals(chestPair.namedTag.getString("Owner"), event.getPlayer().getName())) {
                                block.getLevel().useBreakOn(block);
                                event.getPlayer().sendMessage(TextFormat.YELLOW+"[ChestLocker] 你无法在这里放置箱子，因为这个箱子所连接的另一个箱子不属于你");
                            }
                        }
                    }catch(Exception ignore){
                        //ignore
                    }
                }
            },1);

    }
    @EventHandler(priority = HIGH)
    public void openChestInventory(PlayerInteractEvent event){
        if(event.isCancelled()){
            return;
        }
        if(event.getBlock().getId() == Block.CHEST){
            Block block = event.getBlock();
            BlockEntityChest chest = (BlockEntityChest) block.getLevel().getBlockEntity(block);
            BlockEntityChest chestPair = null;
            if(chest.getPair()!=null){
                chestPair = chest.getPair();
            }
            if(chest.namedTag.exist("Owner")){
                if(!ChestLocker.getInstance().testPermission(chest,event.getPlayer())){
                    if(!ChestLocker.getInstance().testPermission(chestPair,event.getPlayer())){

                        event.setCancelled();
                        event.getPlayer().sendMessage(TextFormat.RED+"[ChestLocker] 你没有打开这个箱子的权限");
                    }
                }
            }
        }
    }
    @EventHandler(priority = HIGH)
    public void onDestoryChest(BlockBreakEvent event){
        if(event.getBlock().getId()==Block.CHEST){
            Block block = event.getBlock();
            if(block.getLevel().getBlockEntity(block) instanceof BlockEntityChest){
                BlockEntityChest chest = (BlockEntityChest) block.getLevel().getBlockEntity(block);
                if(ChestLocker.getInstance().getChestOwner(chest)!=null && !Objects.equals(ChestLocker.getInstance().getChestOwner(chest),event.getPlayer().getName())){
                    event.setCancelled();
                    event.getPlayer().sendMessage(TextFormat.RED+"[ChestLocker] 这个箱子不属于你无法破坏");
                }
            }
        }
    }
}