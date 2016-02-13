package nl.riebie.mcclans.listeners;

import nl.riebie.mcclans.ClansImpl;
import nl.riebie.mcclans.clan.ClanImpl;
import nl.riebie.mcclans.config.Config;
import nl.riebie.mcclans.messages.Messages;
import nl.riebie.mcclans.player.ClanPlayerImpl;
import nl.riebie.mcclans.utils.Utils;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.entity.DamageEntityEvent;

import java.util.Optional;

/**
 * Created by Koen on 13/02/2016.
 */
public class FriendlyFireListener {

    @Listener
    public void onDamageEntity(DamageEntityEvent event) {
        if (event.isCancelled()) {
            return;
        }
        if (!Config.getBoolean(Config.ALLOW_FF_PROTECTION)) {
            return;
        }
        if (!(event.getTargetEntity() instanceof Player)) {
            return;
        }
        Optional<Player> damagerOpt = event.getCause().first(Player.class);
        if (!damagerOpt.isPresent()) {
            return;
        }

        Player victim = (Player) event.getTargetEntity();
        Player damager = damagerOpt.get();

        handleDamageEntity(event, victim, damager);
    }

    public void handleDamageEntity(DamageEntityEvent event, Player victim, Player damager) {
        ClanPlayerImpl victimClanPlayer = ClansImpl.getInstance().getClanPlayer(victim.getUniqueId());
        ClanPlayerImpl damagerClanPlayer = ClansImpl.getInstance().getClanPlayer(damager.getUniqueId());
        if (victimClanPlayer == null || damagerClanPlayer == null)
            return;
        if (victimClanPlayer.getClan() == null || damagerClanPlayer.getClan() == null)
            return;
        if (Utils.isWorldBlockedFromAllowingFriendlyFireProtection(victim.getWorld().getName())) {
            return;
        }
        if (victim.getUniqueId().equals(damager.getUniqueId())) {
            return;
        }

        ClanImpl victimPlayerClan = victimClanPlayer.getClan();
        ClanImpl damagerPlayerClan = damagerClanPlayer.getClan();
        if (victimPlayerClan.isPlayerFriendlyToThisClan(damagerClanPlayer)) {
            if (victimClanPlayer.isFfProtected() && victimPlayerClan.isFfProtected() && damagerPlayerClan.isFfProtected()) {
                event.setCancelled(true);
                Messages.sendWarningMessage(damager, Messages.FRIENDLY_FIRE_IS_OFF);
            }
        }
    }

}
