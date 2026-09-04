package mezz.jei.fabric.network;

import mezz.jei.api.constants.ModIds;
import mezz.jei.common.network.IConnectionToClient;
import mezz.jei.common.network.ServerPacketContext;
import mezz.jei.common.network.packets.PlayToServerPacket;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

/**
 * Asks the server to send its recipes, because Fabric has no recipe sync of its own
 * and since Minecraft 1.21.2 the client's {@link net.minecraft.world.item.crafting.RecipeManager}
 * is empty. The server answers with one or more {@link PacketRecipeSync}.
 */
public class PacketRequestRecipes extends PlayToServerPacket<PacketRequestRecipes> {
	public static final PacketRequestRecipes INSTANCE = new PacketRequestRecipes();
	public static final CustomPacketPayload.Type<PacketRequestRecipes> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(ModIds.JEI_ID, "request_recipes"));
	public static final StreamCodec<RegistryFriendlyByteBuf, PacketRequestRecipes> STREAM_CODEC = StreamCodec.unit(INSTANCE);

	private PacketRequestRecipes() {

	}

	@Override
	public Type<PacketRequestRecipes> type() {
		return TYPE;
	}

	@Override
	public StreamCodec<RegistryFriendlyByteBuf, PacketRequestRecipes> streamCodec() {
		return STREAM_CODEC;
	}

	@Override
	public void process(ServerPacketContext context) {
		ServerPlayer player = context.player();
		MinecraftServer server = player.getServer();
		if (server == null) {
			return;
		}
		IConnectionToClient connection = context.connection();
		RecipeSyncSender.sendRecipes(connection, player, server.getRecipeManager().getRecipes());
	}
}
