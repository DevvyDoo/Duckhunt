package me.devvydoo.duckhunt.game;

import me.devvydoo.duckhunt.Duckhunt;
import me.devvydoo.duckhunt.round.*;
import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;

public class DuckhuntGame implements Listener {

    public final int DEFAULT_TIME_LIMIT = 3 * 60;
    public final int DEFAULT_ROUND_LIMIT = 99;
    private Duckhunt plugin;
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

    public DuckhuntGame(Duckhunt plugin, Location hunterSpawn, Location duckSpawn, Location spectatorSpawn, Location lobbySpawn, int timeLimit) {
        this(plugin);
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

    public ItemStack getHunterBow(){
        ItemStack bow = new ItemStack(Material.BOW);
        ItemMeta meta = bow.getItemMeta();
        meta.setDisplayName(ChatColor.GOLD + "Hunter's Bow");
        bow.setItemMeta(meta);
        bow.addEnchantment(Enchantment.ARROW_DAMAGE, 2);
        bow.addEnchantment(Enchantment.ARROW_INFINITE, 1);
        return bow;
    }

    public ItemStack getRunnerPotion(PotionEffectType type, String name, int time, int amplifier, boolean wantSplash){
        ItemStack pot;
        if (wantSplash)
            pot = new ItemStack(Material.SPLASH_POTION);
        else
            pot = new ItemStack(Material.POTION);
        PotionMeta potionMeta = (PotionMeta) pot.getItemMeta();
        if (type.equals(PotionEffectType.SPEED))
            potionMeta.setColor(Color.AQUA);
        PotionEffect heal = new PotionEffect(type, time, amplifier);
        potionMeta.addCustomEffect(heal, true);
        potionMeta.setDisplayName(name);
        pot.setItemMeta(potionMeta);
        return pot;
    }

    public void updateActivePlayers(){

        if (currentRound == null || !currentRound.getRoundType().equals(RoundType.WAITING))

        activePlayers = new ArrayList<>();

        for (Player player : plugin.getServer().getOnlinePlayers()) {
            if (player.isOp() && !this.isGameReady())
                player.sendMessage(ChatColor.DARK_GRAY + "[" + ChatColor.RED + "Duck Hunt Admin" + ChatColor.DARK_GRAY + "]" + ChatColor.AQUA + " Be sure to setup spawn points using " + ChatColor.LIGHT_PURPLE + "/duckhunt " + ChatColor.AQUA + "if you haven't already!");

            if (player.getGameMode() != GameMode.CREATIVE && player.getGameMode() != GameMode.SPECTATOR) {
                activePlayers.add(player);
                player.sendTitle(ChatColor.GREEN + "In Next Game!", ChatColor.GRAY + "Please wait for an admin to start the next round!", 10, 200, 60);
            }
            else
                player.sendTitle(ChatColor.RED + "Not in Next Game", ChatColor.GRAY + "You are in an invalid gamemode", 10, 200, 10);

        }

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

    public void setRunner(Player player){
        player.teleport(duckSpawn);
        player.sendTitle(ChatColor.BLUE + "RUNNER", ChatColor.GRAY + "Make it to the escape and defeat the hunter!", 10, 80, 10);
        player.playSound(player.getLocation(), Sound.ENTITY_CHICKEN_DEATH, .6f, 1);

        player.getInventory().clear();

        player.getInventory().addItem(new ItemStack(Material.WOODEN_SWORD));
        player.getInventory().addItem(getRunnerPotion(PotionEffectType.SPEED, ChatColor.AQUA + "Speed Potion", 10, 1, true));
        player.getInventory().addItem(getRunnerPotion(PotionEffectType.HEAL, ChatColor.RED + "Heal Potion", 1, 2, true));
        player.getInventory().addItem(getRunnerPotion(PotionEffectType.HEAL, ChatColor.RED + "Heal Potion", 1, 2, true));
        player.getInventory().addItem(new ItemStack(Material.GOLDEN_APPLE), new ItemStack(Material.COOKED_BEEF, 3));

        player.setGameMode(GameMode.ADVENTURE);
        player.setFireTicks(0);
        player.setHealth(20);
        player.setFoodLevel(20);
        player.setSaturation(20);


        ducks.add(player);
    }

    private void runnerDied(Player player){

        ducks.remove(player);
        player.sendTitle(ChatColor.RED + "You died!", ChatColor.GRAY + "Maybe next time...", 10, 30, 10);
        player.playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_DEATH, .5f, .5f);
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

        player.setFireTicks(0);
        player.setAllowFlight(true);
        player.setFlying(true);
        player.setInvulnerable(true);
    }

    public Location getHunterSpawn() {
        return hunterSpawn;
    }

    public void setHunterSpawn(Location hunterSpawn) {
        this.hunterSpawn = hunterSpawn;
    }

    public Location getDuckSpawn() {
        return duckSpawn;
    }

    public void setDuckSpawn(Location duckSpawn) {
        this.duckSpawn = duckSpawn;
    }

    public Location getSpectatorSpawn() {
        return spectatorSpawn;
    }

    public void setSpectatorSpawn(Location spectatorSpawn) {
        this.spectatorSpawn = spectatorSpawn;
    }

    public Location getLobbySpawn() {
        return lobbySpawn;
    }

    public void setLobbySpawn(Location lobbySpawn) {
        this.lobbySpawn = lobbySpawn;
    }

    public int getTimeLimit() {
        return timeLimit;
    }

    public void setTimeLimit(int timeLimit) {
        this.timeLimit = timeLimit;
    }

    public ArrayList<Player> getActivePlayers() {
        return activePlayers;
    }

    public Round getCurrentRound() {
        return currentRound;
    }

    public Player getHunter() {
        return hunter;
    }

    public void setHunter(Player player){
        player.teleport(hunterSpawn);
        player.sendTitle(ChatColor.RED + "HUNTER", ChatColor.GRAY + "Do whatever it takes to eliminate the runners!", 10, 80, 10);
        player.playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, .4f, .4f);

        player.getInventory().clear();

        player.getInventory().addItem(getHunterBow(), new ItemStack(Material.ARROW, 64), new ItemStack(Material.COOKED_BEEF, 16));


        player.setGameMode(GameMode.ADVENTURE);
        player.setFireTicks(0);
        player.setHealth(20);
        player.setFoodLevel(20);
        player.setSaturation(20);

        this.hunter = player;
    }

    public ArrayList<Player> getDucks() {
        return ducks;
    }

    public boolean isActivePlayer(Player player){
        return activePlayers.contains(player);
    }

    public void startGame(){
        if (this.currentRound.getRoundType().equals(RoundType.WAITING) && isGameReady()) {
            hunterWins = 0;
            runnerWins = 0;
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
                if (DEFAULT_ROUND_LIMIT <= runnerWins + hunterWins){
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
        return hunterSpawn != null && duckSpawn != null && spectatorSpawn != null && lobbySpawn != null && timeLimit != 0 && activePlayers != null && !activePlayers.isEmpty();
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
            p.playSound(p.getLocation(), Sound.ENTITY_DROWNED_AMBIENT, .8f, .3f);
        }
        hunterWins++;
        plugin.getServer().broadcastMessage(ChatColor.GRAY + "[" + ChatColor.GOLD + "Duck Hunt" + ChatColor.GRAY + "]" + ChatColor.GRAY + " Hunter wins! The Hunter has won " + ChatColor.GREEN.toString() + hunterWins + ChatColor.GRAY + " time" + (hunterWins > 1 ? "s" : ""));
    }


    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerDamage(EntityDamageEvent event){

        // We don't care about non players
        if (!(event.getEntity() instanceof Player))
            return;

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

            // If they are both a duck, disable the damage
            if (ducks.contains(hurt) && ducks.contains(damager)){
                event.setCancelled(true);
            }

        }

    }

    @EventHandler
    public void onPlayerDeath(EntityDeathEvent event){
        if (event.getEntity() instanceof Player && currentRound instanceof ActionRound){
            event.setCancelled(true);

            Player playerDied = (Player) event.getEntity();

            if (playerDied.equals(hunter)){
                setSpectator(playerDied);
                if (hunter.getKiller() != null)
                    playerDied.getServer().broadcastMessage(ChatColor.AQUA + ChatColor.stripColor(hunter.getKiller().getDisplayName()) + ChatColor.GRAY + " destroyed " + ChatColor.RED + ChatColor.stripColor(playerDied.getDisplayName()));
                else
                    playerDied.getServer().broadcastMessage(ChatColor.RED + ChatColor.stripColor(playerDied.getDisplayName()) + ChatColor.GRAY + " died to unknown causes...");
//                announceRunnersWon();
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
        if (!(currentRound instanceof WaitingRound)){
            setSpectator(event.getPlayer());
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event){
        updateActivePlayers();

        if (event.getPlayer().equals(hunter))
            nextRound();
        else if (ducks.contains(event.getPlayer())){
            runnerDied(event.getPlayer());
            if (ducks.size() == 0)
                nextRound();
        }

    }
}
