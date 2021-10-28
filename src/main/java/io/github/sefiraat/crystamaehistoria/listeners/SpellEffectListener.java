package io.github.sefiraat.crystamaehistoria.listeners;

import io.github.sefiraat.crystamaehistoria.CrystamaeHistoria;
import io.github.sefiraat.crystamaehistoria.magic.CastInformation;
import io.github.sefiraat.crystamaehistoria.magic.spells.core.MagicProjectile;
import io.github.sefiraat.crystamaehistoria.utils.Keys;
import io.github.thebusybiscuit.slimefun4.libraries.dough.data.persistent.PersistentDataAPI;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LightningStrike;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.WitherSkeleton;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.weather.LightningStrikeEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class SpellEffectListener implements Listener {

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onProjectileHit(ProjectileHitEvent event) {
        final Projectile projectile = event.getEntity();
        final Optional<MagicProjectile> optionalMagicProjectile = CrystamaeHistoria.getProjectileMap().keySet()
            .stream()
            .filter(magicProjectile1 -> magicProjectile1.matches(projectile))
            .findFirst();

        if (!optionalMagicProjectile.isPresent()) {
            return;
        }

        final MagicProjectile magicProjectile = optionalMagicProjectile.get();
        final CastInformation castInfo = CrystamaeHistoria.getProjectileCastInfo(magicProjectile);
        final Entity hitEntity = event.getHitEntity();

        event.setCancelled(true);
        castInfo.setProjectileLocation(magicProjectile.getLocation());

        if (entityHitAllowed(castInfo, hitEntity)) {
            castInfo.setMainTarget((LivingEntity) hitEntity);
            castInfo.setDamageLocation(hitEntity.getLocation());
            castInfo.runPreAffectEvent();
            castInfo.runAffectEvent();
            castInfo.runPostAffectEvent();
        }

        if (event.getHitBlock() != null) {
            castInfo.setHitBlock(event.getHitBlock());
            castInfo.setDamageLocation(event.getHitBlock().getLocation());
            castInfo.runProjectileHitBlockEvent();
        }

        magicProjectile.kill();
    }

    private boolean entityHitAllowed(CastInformation castInformation, Entity hitEntity) {
        final Player player = Bukkit.getPlayer(castInformation.getCaster());
        return hitEntity instanceof LivingEntity
            && hitEntity.getUniqueId() != castInformation.getCaster()
            && player != null;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onLightningStrikeHit(LightningStrikeEvent event) {
        final LightningStrike lightningStrike = event.getLightning();
        final UUID uuid = lightningStrike.getUniqueId();
        if (CrystamaeHistoria.getStrikeMap().containsKey(uuid)) {
            CastInformation castInformation = CrystamaeHistoria.getStrikeCastInfo(uuid);

            final Location location = event.getLightning().getLocation();
            castInformation.setDamageLocation(location);

            castInformation.runPreAffectEvent();
            castInformation.runAffectEvent();
            castInformation.runPostAffectEvent();

            event.setCancelled(true);
            CrystamaeHistoria.getStrikeMap().remove(uuid);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onInvulnerablePlayerDamaged(EntityDamageEvent event) {
        NamespacedKey key = Keys.PDC_IS_INVULNERABLE;
        EntityDamageEvent.DamageCause cause = event.getCause();
        if (event.getEntity() instanceof LivingEntity
            && PersistentDataAPI.hasLong(event.getEntity(), key)
        ) {
            LivingEntity livingEntity = (LivingEntity) event.getEntity();
            long expiry = PersistentDataAPI.getLong(livingEntity, key);
            if (expiry >= System.currentTimeMillis()) {
                if (cause != EntityDamageEvent.DamageCause.CUSTOM
                    && cause != EntityDamageEvent.DamageCause.SUICIDE
                ) {
                    event.setCancelled(true);
                }
            } else {
                PersistentDataAPI.remove(livingEntity, key);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onWitherWeatherDeath(EntityDeathEvent event) {
        NamespacedKey key = Keys.PDC_IS_WEATHER_WITHER;
        if (event.getEntity() instanceof WitherSkeleton
            && PersistentDataAPI.getBoolean(event.getEntity(), key)
        ) {
            List<ItemStack> itemStackList = event.getDrops();
            for (ItemStack itemStack : itemStackList) {
                if (itemStack.getType() == Material.WITHER_SKELETON_SKULL) {
                    return;
                }
            }
            itemStackList.add(new ItemStack(Material.WITHER_SKELETON_SKULL));
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onMagicSummonDeath(EntityDeathEvent event) {
        NamespacedKey key = Keys.PDC_IS_SPAWN_OWNER;
        if (PersistentDataAPI.hasBoolean(event.getEntity(), key)) {
            event.setCancelled(true);
            event.getEntity().remove();
        }
    }




}
