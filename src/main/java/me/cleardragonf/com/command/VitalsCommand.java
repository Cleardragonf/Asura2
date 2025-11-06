package me.cleardragonf.com.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import me.cleardragonf.com.Asura;
import me.cleardragonf.com.vitals.PlayerVitalsProvider;
import me.cleardragonf.com.vitals.net.ModNetwork;
import me.cleardragonf.com.vitals.net.VitalsSyncPacket;

@Mod.EventBusSubscriber(modid = Asura.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class VitalsCommand {
    @SubscribeEvent
    public static void register(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> d = event.getDispatcher();
        d.register(Commands.literal("asura_vitals")
                .requires(src -> src.hasPermission(2))
                .then(Commands.literal("get").executes(ctx -> {
                    ServerPlayer sp = ctx.getSource().getPlayerOrException();
                    sp.getCapability(PlayerVitalsProvider.VITALS_CAPABILITY).ifPresent(v -> {
                        ctx.getSource().sendSuccess(() -> Component.literal(
                                "Temp=" + v.getTemperature() + " Thirst=" + v.getThirst()
                        ), false);
                    });
                    return 1;
                }))
                .then(Commands.literal("set")
                        .then(Commands.literal("temp")
                                .then(Commands.argument("value", IntegerArgumentType.integer(-100, 100))
                                        .executes(ctx -> {
                                            ServerPlayer sp = ctx.getSource().getPlayerOrException();
                                            int val = IntegerArgumentType.getInteger(ctx, "value");
                                            sp.getCapability(PlayerVitalsProvider.VITALS_CAPABILITY).ifPresent(v -> {
                                                v.setTemperature(val);
                                                ModNetwork.sendToPlayer(sp, new VitalsSyncPacket(v.getTemperature(), v.getThirst()));
                                            });
                                            ctx.getSource().sendSuccess(() -> Component.literal("Set temp to " + val), true);
                                            return 1;
                                        })))
                        .then(Commands.literal("thirst")
                                .then(Commands.argument("value", IntegerArgumentType.integer(0, 100))
                                        .executes(ctx -> {
                                            ServerPlayer sp = ctx.getSource().getPlayerOrException();
                                            int val = IntegerArgumentType.getInteger(ctx, "value");
                                            sp.getCapability(PlayerVitalsProvider.VITALS_CAPABILITY).ifPresent(v -> {
                                                v.setThirst(val);
                                                ModNetwork.sendToPlayer(sp, new VitalsSyncPacket(v.getTemperature(), v.getThirst()));
                                            });
                                            ctx.getSource().sendSuccess(() -> Component.literal("Set thirst to " + val), true);
                                            return 1;
                                        }))))
        );
    }
}
