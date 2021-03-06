package me.devvydoo.duckhunt.game;

import me.devvydoo.duckhunt.Duckhunt;
import me.devvydoo.duckhunt.round.*;
import me.devvydoo.duckhunt.util.CustomItems;
import me.devvydoo.duckhunt.util.GameItems;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;

public class DuckhuntGame implements Listener {

    public final int DEFAULT_TIME_LIMIT = 3 * 60;
    public final int DEFAULT_ROUND_LIMIT = 99;
    private Duckhunt plugin;
    private World world;
    private Location hunterSpawn;
    private Location duckSpawn;
    private Location spectatorSpawn;
    private Location lobbySpawn;

    private int timeLimit = DEFAULT_TIME_LIMIT;
    private ArrayList<Player> activePlayers;

    private Round currentRound;

    private Player hunter;
    private ArrayList<Player> ducks;

    private int hunterWins = 0;
    private int runnerWins = 0;

    private boolean cancelFallDamage = true;

    public DuckhuntGame(Duckhunt plugin, World world, Location hunterSpawn, Location duckSpawn, Location spectatorSpawn, Location lobbySpawn, int timeLimit) {
        this(plugin);
        this.world = world;
        this.hunterSpawn = hunterSpawn;
        this.duckSpawn = duckSpawn;
        this.spectatorSpawn = spectatorSpawn;
        this.lobbySpawn = lobbySpawn;
        this.timeLimit = timeLimit;
    }

    public DuckhuntGame(Duckhunt plugin) {
        this.plugin = plugin;
        updateActivePlayers();
        currentRound = new WaitingRound(plugin);
        currentRound.startRound();
    }

    public World getWorld() {
        return world;
    }

    public void setCancelFallDamage(boolean cancelFallDamage) {
        this.cancelFallDamage = cancelFallDamage;
    }


    public void updateActivePlayers(){

        activePlayers = new ArrayList<>();

        for (Player player : plugin.getServer().getOnlinePlayers()) {

            if (activePlayers.contains(player))
                continue;

            player.getAttribute(Attribute.GENERIC_ATTACK_SPEED).setBaseValue(128.0);
            if (player.isOp() && !this.isGameReady())
                player.sendMessage(ChatColor.DARK_GRAY + "[" + ChatColor.RED + "Duck Hunt Admin" + ChatColor.DARK_GRAY + "]" + ChatColor.AQUA + " Be sure to setup spawn points using " + ChatColor.LIGHT_PURPLE + "/duckhunt " + ChatColor.AQUA + "if you haven't already!");

            if (player.getGameMode() != GameMode.CREATIVE && player.getGameMode() != GameMode.SPECTATOR) {
                activePlayers.add(player);
                player.sendTitle(ChatColor.GREEN + "In Next Game!", ChatColor.GRAY + "Please wait for an admin to start the next round!", 10, 200, 60);
            }
            else
                player.sendTitle(ChatColor.RED + "Not in Next Game", ChatColor.GRAY + "You are in an invalid gamemode", 10, 200, 10);

        }

        // Remove any offline players
        activePlayers.removeIf(p -> !p.isOnline());

    }

    public void randomizePlayers(){

        ducks = new ArrayList<>();
        hunter = null;

        if (activePlayers.size() < 2)
            return;

        Player randomPlayer = activePlayers.get((int)(Math.random() * activePlayers.size()));

        for (Player player : activePlayers){
            if (player.equals(randomPlayer))
                setHunter(player);
            else
                setRunner(player);
        }
    }

    public void setHunter(Player player){
        player.teleport(hunterSpawn);
        player.sendTitle(ChatColor.RED + "HUNTER", ChatColor.GRAY + "Do whatever it takes to eliminate the runners!", 10, 80, 10);
        player.playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, .4f, .4f);

        player.getInventory().clear();

        for (ItemStack item : GameItems.getHunterKit())
            player.getInventory().addItem(item);

        transitionPlayer(player);

        this.hunter = player;
    }

    public void setRunner(Player player){
        player.teleport(duckSpawn);
        player.sendTitle(ChatColor.BLUE + "RUNNER", ChatColor.GRAY + "Make it to the escape and defeat the hunter!", 10, 80, 10);
        player.playSound(player.getLocation(), Sound.ENTITY_CHICKEN_DEATH, .85f, 1);

        player.getInventory().clear();

        for (ItemStack item : GameItems.getRunnerKit())
            player.getInventory().addItem(item);

        transitionPlayer(player);

        ducks.add(player);
    }

    private void runnerDied(Player player){

        ducks.remove(player);
        player.sendTitle(ChatColor.RED + "You died!", ChatColor.GRAY + "Maybe next time...", 10, 30, 10);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_BAT_DEATH, .9f, .4f);
        setSpectator(player);

    }

    private void setSpectator(Player player){

        for (Player otherPlayer : Bukkit.getOnlinePlayers()){
            if (player.equals(otherPlayer))
                continue;
            if (ducks.contains(otherPlayer) || otherPlayer.equals(hunter)){
                otherPlayer.hidePlayer(plugin, player);
            } else {
                otherPlayer.showPlayer(plugin, player);
                player.showPlayer(plugin, otherPlayer);
            }
        }

        if (player.getFireTicks() > 0)
            player.setFireTicks(0);
        player.setAllowFlight(true);
        player.setFlying(true);
        player.setInvulnerable(true);
        player.setCanPickupItems(false);
    }

    public Location getHunterSpawn() {
        return hunterSpawn;
    }

    public void setHunterSpawn(Location hunterSpawn) {
        if (world == null)
            world = hunterSpawn.getWorld();
        else {
            if (!world.equals(hunterSpawn.getWorld()))
                return;
        }
        this.hunterSpawn = hunterSpawn;
    }

    public Location getDuckSpawn() {
        return duckSpawn;
    }

    public void setDuckSpawn(Location duckSpawn) {
        if (world == null)
            world = duckSpawn.getWorld();
        else {
            if (!world.equals(duckSpawn.getWorld()))
                return;
        }
        this.duckSpawn = duckSpawn;
    }

    public Location getSpectatorSpawn() {
        return spectatorSpawn;
    }

    public void setSpectatorSpawn(Location spectatorSpawn) {
        if (world == null)
            world = spectatorSpawn.getWorld();
        else {
            if (!world.equals(spectatorSpawn.getWorld()))
                return;
        }
        this.spectatorSpawn = spectatorSpawn;
    }

    public Location getLobbySpawn() {
        return lobbySpawn;
    }

    public void setLobbySpawn(Location lobbySpawn) {
        if (world == null)
            world = lobbySpawn.getWorld();
        else {
            if (!world.equals(lobbySpawn.getWorld()))
                return;
        }
        this.lobbySpawn = lobbySpawn;
    }

    public int getTimeLimit() {
        return timeLimit;
    }

    public ArrayList<Player> getActivePlayers() {
        return activePlayers;
    }

    public Round getCurrentRound() {
        return currentRound;
    }

    private void transitionPlayer(Player player) {
        player.setGameMode(GameMode.ADVENTURE);
        for (PotionEffectType type : PotionEffectType.values())
            player.removePotionEffect(type);
        if (player.getFireTicks() > 0)
            player.setFireTicks(0);
        player.setHealth(20);
        player.setFoodLevel(20);
        player.setSaturation(20);
        player.setInvulnerable(false);
    }

    public ArrayList<Player> getDucks() {
        return ducks;
    }

    public void startGame(){
        if (this.currentRound.getRoundType().equals(RoundType.WAITING) && isGameReady()) {
            hunterWins = 0;
            runnerWins = 0;
            plugin.updateWorldLocations(world, this);
            nextRound();
        }
        else
            throw new RuntimeException("Could not start round! Currently there is a game going on or the game is not ready!");
    }

    public void nextRound(){

        this.currentRound.endRound();

        switch (this.currentRound.getNextRoundType()){
            case WAITING:
                this.currentRound = new WaitingRound(plugin);
                break;
            case PREGAME:
                if (DEFAULT_ROUND_LIMIT <= runnerWins + hunterWins || activePlayers.size() <= 1){
                    this.currentRound = new WaitingRound(plugin);
                    break;
                }
                this.currentRound = new PreRound(plugin);
                break;
            case ACTION:
                this.currentRound = new ActionRound(plugin);
                break;
            case POST:
                this.currentRound = new PostRound(plugin);
                break;
            default:
                throw new RuntimeException("Unknown round! " + this.currentRound.getNextRoundType());
        }

        this.currentRound.startRound();
    }

    public void endGame(){
        this.currentRound.endRound();
        this.currentRound = new WaitingRound(plugin);
    }

    public boolean isGameReady(){
        return hunterSpawn != null && duckSpawn != null && spectatorSpawn != null && lobbySpawn != null && timeLimit != 0 && activePlayers != null && activePlayers.size() > 1;
    }

    public void announceRunnersWon(String reason){
        for (Player p : plugin.getGame().getActivePlayers()) {
            p.sendTitle(ChatColor.AQUA + "Runners win!", ChatColor.GRAY + reason, 10, 5 * 20, 20);
            p.playSound(p.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, .7f, 1);
        }
        runnerWins++;
        plugin.getServer().broadcastMessage(ChatColor.GRAY + "[" + ChatColor.GOLD + "Duck Hunt" + ChatColor.GRAY + "]" + ChatColor.GRAY + " Runners win! The runners have won " + ChatColor.GREEN.toString() + runnerWins + ChatColor.GRAY.toString() + " time" + (runnerWins > 1 ? "s" : ""));
    }

    public void announceHunterWon(String reason){
        for (Player p : plugin.getGame().getActivePlayers()) {
            p.sendTitle(ChatColor.RED + "Hunter wins!", ChatColor.GRAY + reason, 10, 5 * 20, 20);
            p.playSound(p.getLocation(), Sound.ENTITY_DROWNED_AMBIENT, .8f, .4f);
        }
        hunterWins++;
        plugin.getServer().broadcastMessage(ChatColor.GRAY + "[" + ChatColor.GOLD + "Duck Hunt" + ChatColor.GRAY + "]" + ChatColor.GRAY + " Hunter wins! The Hunter has won " + ChatColor.GREEN.toString() + hunterWins + ChatColor.GRAY + " time" + (hunterWins > 1 ? "s" : ""));
    }


    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerDamage(EntityDamageEvent event){

        // We don't care about non players
        if (!(event.getEntity() instanceof Player))
            return;

        if (event.getCause().equals(EntityDamageEvent.DamageCause.FALL) && cancelFallDamage)
            event.setCancelled(true);

        // Essentially godmode when we are not in action phase
        if (!(currentRound instanceof ActionRound))
            event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerDamagedByPlayer(EntityDamageByEntityEvent event){

        // Consider only the times where the two entities are players
        if (ducks != null && event.getEntity() instanceof Player && event.getDamager() instanceof Player){
            Player hurt = (Player) event.getEntity();
            Player damager = (Player) event.getDamager();

            // If the either of the two cant see each other don't let it hpapen
            if (!hurt.canSee(damager) || !damager.canSee(hurt))
                event.setCancelled(true);

            // If they are both a duck, disable the damage
            if (ducks.contains(hurt) && ducks.contains(damager))
                event.setCancelled(true);
        }

    }

    @EventHandler
    public void onPlayerDeath(EntityDeathEvent event){
        if (event.getEntity() instanceof Player && currentRound instanceof ActionRound){
            event.setCancelled(true);

            Player playerDied = (Player) event.getEntity();

            for (ItemStack invItem : playerDied.getInventory().getContents()){
                if (invItem != null)
                    playerDied.getWorld().dropItemNaturally(playerDied.getEyeLocation(), invItem);
            }

            playerDied.getInventory().clear();
            if (playerDied.isOp())
                playerDied.getInventory().addItem(plugin.getCustomItemManager().getCustomItemOfType(CustomItems.ADMIN_FEATHER));

            if (playerDied.equals(hunter)){
                playerDied.getWorld().playSound(playerDied.getLocation(), Sound.ENTITY_BAT_DEATH, .9f, .4f);
                setSpectator(playerDied);
                if (hunter.getKiller() != null && !hunter.getKiller().equals(hunter))
                    playerDied.getServer().broadcastMessage(ChatColor.AQUA + ChatColor.stripColor(hunter.getKiller().getDisplayName()) + ChatColor.GRAY + " destroyed " + ChatColor.RED + ChatColor.stripColor(playerDied.getDisplayName()));
                else
                    playerDied.getServer().broadcastMessage(ChatColor.RED + ChatColor.stripColor(playerDied.getDisplayName()) + ChatColor.GRAY + " died to unknown causes...");
                nextRound();
            } else if (ducks.contains(playerDied)){
                runnerDied(playerDied);
                if (hunter.equals(playerDied.getKiller())){
                    playerDied.getServer().broadcastMessage(ChatColor.RED + ChatColor.stripColor(hunter.getDisplayName()) + ChatColor.GRAY + " sniped " + ChatColor.AQUA + ChatColor.stripColor(playerDied.getDisplayName()));
                } else {
                    playerDied.getServer().broadcastMessage(ChatColor.AQUA + ChatColor.stripColor(playerDied.getDisplayName()) + ChatColor.GRAY + " died to unknown causes...");
                }
                if (ducks.size() == 0)
                    nextRound();
            }
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event){

        updateActivePlayers();

        if (!(currentRound instanceof WaitingRound))
            setSpectator(event.getPlayer());

        if (lobbySpawn != null)
            event.getPlayer().teleport(lobbySpawn);
        else
            event.getPlayer().teleport(event.getPlayer().getWorld().getSpawnLocation());

        event.getPlayer().getInventory().clear();
        if (event.getPlayer().isOp())
            event.getPlayer().getInventory().addItem(plugin.getCustomItemManager().getCustomItemOfType(CustomItems.ADMIN_FEATHER));
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event){
        updateActivePlayers();
        activePlayers.remove(event.getPlayer());

        if (event.getPlayer().equals(hunter))
            nextRound();
        else if (ducks != null && ducks.contains(event.getPlayer())){
            runnerDied(event.getPlayer());
            if (ducks.size() == 0)
                nextRound();
        }
        event.getPlayer().getInventory().clear();

    }

    @EventHandler
    public void onPlayerGamemodeChange(PlayerGameModeChangeEvent event){
        if (currentRound instanceof WaitingRound && !activePlayers.contains(event.getPlayer()))
            updateActivePlayers();
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event){
        if (event.getPlayer().isOp())
            return;

        event.setCancelled(true);
    }

    @EventHandler
    public void onSpectatorInteract(PlayerInteractEvent event){
        if (currentRound instanceof ActionRound && event.getPlayer().getAllowFlight())
            event.setCancelled(true);
    }
}
