package me.cleardragonf.com.network;

import me.cleardragonf.com.Asura;
import me.cleardragonf.com.network.msg.C2SLeftClickCast;
import me.cleardragonf.com.network.msg.C2SSetProgramChain;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.ChannelBuilder;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.SimpleChannel;

public class AsuraNetwork {
    private static final String PROTOCOL = "1";
    public static SimpleChannel CHANNEL;

    public static void register() {
        if (CHANNEL != null) return;
        CHANNEL = ChannelBuilder.named(new ResourceLocation(Asura.MODID, "main"))
                .serverAcceptedVersions((s) -> true)
                .clientAcceptedVersions((s) -> true)
                .networkProtocolVersion(() -> PROTOCOL)
                .simpleChannel();

        int id = 0;
        CHANNEL.messageBuilder(C2SSetProgramChain.class, id++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(C2SSetProgramChain::encode)
                .decoder(C2SSetProgramChain::decode)
                .consumerMainThread(C2SSetProgramChain::handle)
                .add();

        CHANNEL.messageBuilder(C2SLeftClickCast.class, id++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(C2SLeftClickCast::encode)
                .decoder(C2SLeftClickCast::decode)
                .consumerMainThread(C2SLeftClickCast::handle)
                .add();
    }
}

