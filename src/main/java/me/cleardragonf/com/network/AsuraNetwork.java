package me.cleardragonf.com.network;

import me.cleardragonf.com.Asura;
import me.cleardragonf.com.network.msg.C2SLeftClickCast;
import me.cleardragonf.com.network.msg.C2SSetProgramChain;
import me.cleardragonf.com.network.msg.C2SRequestWardData;
import me.cleardragonf.com.network.msg.S2CWardDataSync;
import me.cleardragonf.com.network.msg.C2SSelectWard;
import me.cleardragonf.com.network.msg.C2SToggleWardActive;
import me.cleardragonf.com.network.msg.C2SSetWardOptionBool;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.ChannelBuilder;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.SimpleChannel;

public class AsuraNetwork {
    private static final int PROTOCOL = 1;
    public static SimpleChannel CHANNEL;

    public static void register() {
        if (CHANNEL != null) return;
        CHANNEL = ChannelBuilder.named(Asura.MODID + ":main")
                .networkProtocolVersion(PROTOCOL)
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

        CHANNEL.messageBuilder(C2SRequestWardData.class, id++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(C2SRequestWardData::encode)
                .decoder(C2SRequestWardData::decode)
                .consumerMainThread(C2SRequestWardData::handle)
                .add();

        CHANNEL.messageBuilder(S2CWardDataSync.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(S2CWardDataSync::encode)
                .decoder(S2CWardDataSync::decode)
                .consumerMainThread(S2CWardDataSync::handle)
                .add();

        CHANNEL.messageBuilder(C2SSelectWard.class, id++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(C2SSelectWard::encode)
                .decoder(C2SSelectWard::decode)
                .consumerMainThread(C2SSelectWard::handle)
                .add();

        CHANNEL.messageBuilder(C2SToggleWardActive.class, id++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(C2SToggleWardActive::encode)
                .decoder(C2SToggleWardActive::decode)
                .consumerMainThread(C2SToggleWardActive::handle)
                .add();

        CHANNEL.messageBuilder(C2SSetWardOptionBool.class, id++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(C2SSetWardOptionBool::encode)
                .decoder(C2SSetWardOptionBool::decode)
                .consumerMainThread(C2SSetWardOptionBool::handle)
                .add();
    }
}

