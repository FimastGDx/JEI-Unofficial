package mezz.jei.fabric.network;

import io.netty.buffer.Unpooled;
import mezz.jei.common.network.IConnectionToClient;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Sends the server's recipes to one player, in batches.
 * <p>
 * {@link net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket} refuses
 * payloads over 1 MiB, so every recipe is measured first and batches are cut before they
 * reach that limit. Recipes that cannot be serialized at all are skipped rather than
 * allowed to break the whole sync.
 */
public final class RecipeSyncSender {
	private static final Logger LOGGER = LogManager.getLogger();
	private static final int MAX_BATCH_BYTES = 800_000;

	private RecipeSyncSender() {}

	public static void sendRecipes(IConnectionToClient connection, ServerPlayer player, Collection<RecipeHolder<?>> recipes) {
		List<RecipeHolder<?>> batch = new ArrayList<>();
		int batchBytes = 0;
		int sent = 0;
		int skipped = 0;
		int batches = 0;

		RegistryFriendlyByteBuf scratch = new RegistryFriendlyByteBuf(Unpooled.buffer(), player.registryAccess());
		try {
			for (RecipeHolder<?> recipe : recipes) {
				int size = measure(scratch, recipe);
				if (size < 0 || size > MAX_BATCH_BYTES) {
					skipped++;
					continue;
				}
				if (batchBytes + size > MAX_BATCH_BYTES) {
					connection.sendPacketToClient(new PacketRecipeSync(batch, false), player);
					batches++;
					batch = new ArrayList<>();
					batchBytes = 0;
				}
				batch.add(recipe);
				batchBytes += size;
				sent++;
			}
		} finally {
			scratch.release();
		}

		// always send a final batch, even an empty one, so the client knows the sync is done
		connection.sendPacketToClient(new PacketRecipeSync(batch, true), player);
		batches++;

		LOGGER.info("Sent {} recipes to {} in {} batches.", sent, player.getGameProfile().getName(), batches);
		if (skipped > 0) {
			LOGGER.warn("Skipped {} recipes that could not be sent to {}.", skipped, player.getGameProfile().getName());
		}
	}

	/**
	 * @return the serialized size of the recipe in bytes, or -1 if it cannot be serialized.
	 */
	private static int measure(RegistryFriendlyByteBuf scratch, RecipeHolder<?> recipe) {
		scratch.clear();
		try {
			RecipeHolder.STREAM_CODEC.encode(scratch, recipe);
		} catch (RuntimeException e) {
			LOGGER.error("Failed to serialize recipe for JEI, skipping it: {}", recipe.id().location(), e);
			return -1;
		}
		return scratch.readableBytes();
	}
}
