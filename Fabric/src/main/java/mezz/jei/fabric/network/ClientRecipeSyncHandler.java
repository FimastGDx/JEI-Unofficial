package mezz.jei.fabric.network;

import mezz.jei.fabric.events.JeiLifecycleEvents;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * Collects the batches of a {@link PacketRecipeSync} sync and fires
 * {@link JeiLifecycleEvents#SERVER_RECIPES_RECEIVED} once the last one arrives.
 * <p>
 * Client-side only. Fabric runs play payload handlers on the client thread, so no
 * synchronization is needed here.
 */
public final class ClientRecipeSyncHandler {
	private static final Logger LOGGER = LogManager.getLogger();
	private static final List<RecipeHolder<?>> received = new ArrayList<>();

	private ClientRecipeSyncHandler() {}

	/**
	 * Drop anything left over from an earlier, unfinished sync.
	 */
	public static void reset() {
		received.clear();
	}

	public static void handleRecipeSync(List<RecipeHolder<?>> recipes, boolean last) {
		received.addAll(recipes);
		if (!last) {
			return;
		}
		List<RecipeHolder<?>> recipesReceived = List.copyOf(received);
		received.clear();
		LOGGER.info("Received {} recipes from the server.", recipesReceived.size());
		JeiLifecycleEvents.SERVER_RECIPES_RECEIVED.invoker()
			.onRecipesReceived(RecipeMap.create(recipesReceived));
	}
}
