package mezz.jei.fabric.network;

import mezz.jei.api.constants.ModIds;
import mezz.jei.common.network.ClientPacketContext;
import mezz.jei.common.network.packets.PlayToClientPacket;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeHolder;

import java.util.List;

/**
 * One batch of recipes from the server, in answer to {@link PacketRequestRecipes}.
 * Recipes are split into batches by {@link RecipeSyncSender} because a single custom
 * payload cannot exceed 1 MiB. The batch with {@link #last} set ends the sync.
 */
public class PacketRecipeSync extends PlayToClientPacket<PacketRecipeSync> {
	public static final Type<PacketRecipeSync> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(ModIds.JEI_ID, "recipe_sync"));
	public static final StreamCodec<RegistryFriendlyByteBuf, PacketRecipeSync> STREAM_CODEC = StreamCodec.composite(
		RecipeHolder.STREAM_CODEC.apply(ByteBufCodecs.list()),
		p -> p.recipes,
		ByteBufCodecs.BOOL,
		p -> p.last,
		PacketRecipeSync::new
	);

	private final List<RecipeHolder<?>> recipes;
	private final boolean last;

	public PacketRecipeSync(List<RecipeHolder<?>> recipes, boolean last) {
		this.recipes = recipes;
		this.last = last;
	}

	@Override
	public Type<PacketRecipeSync> type() {
		return TYPE;
	}

	@Override
	public StreamCodec<RegistryFriendlyByteBuf, PacketRecipeSync> streamCodec() {
		return STREAM_CODEC;
	}

	@Override
	public void process(ClientPacketContext context) {
		// this class is loaded on dedicated servers to register the payload type,
		// so the client-only handler is only referenced from here, where it never runs.
		ClientRecipeSyncHandler.handleRecipeSync(recipes, last);
	}
}
