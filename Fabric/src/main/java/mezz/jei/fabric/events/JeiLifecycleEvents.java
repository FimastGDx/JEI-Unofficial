package mezz.jei.fabric.events;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import net.minecraft.world.item.crafting.RecipeMap;

public class JeiLifecycleEvents {
	public static final Event<Runnable> GAME_START =
			EventFactory.createArrayBacked(Runnable.class, callbacks -> () -> {
				for (Runnable callback : callbacks) {
					callback.run();
				}
			});

	public static final Event<Runnable> GAME_STOP =
			EventFactory.createArrayBacked(Runnable.class, callbacks -> () -> {
				for (Runnable callback : callbacks) {
					callback.run();
				}
			});

	public static final Event<Runnable> AFTER_RECIPE_SYNC =
			EventFactory.createArrayBacked(Runnable.class, callbacks -> () -> {
				for (Runnable callback : callbacks) {
					callback.run();
				}
			});

	/**
	 * Fired when JEI on the server has finished sending its recipes to this client.
	 * Fabric has no recipe sync of its own, see {@code mezz.jei.fabric.network.PacketRecipeSync}.
	 */
	public static final Event<RecipesReceived> SERVER_RECIPES_RECEIVED =
			EventFactory.createArrayBacked(RecipesReceived.class, callbacks -> recipes -> {
				for (RecipesReceived callback : callbacks) {
					callback.onRecipesReceived(recipes);
				}
			});

	public static final Event<RegisterResourceReloadListener> REGISTER_RESOURCE_RELOAD_LISTENER =
			EventFactory.createArrayBacked(RegisterResourceReloadListener.class, callbacks -> (resourceManager, textureManager) -> {
				for (RegisterResourceReloadListener callback : callbacks) {
					callback.registerResourceReloadListener(resourceManager, textureManager);
				}
			});

	@Environment(EnvType.CLIENT)
	@FunctionalInterface
	public interface RegisterResourceReloadListener {
		void registerResourceReloadListener(ReloadableResourceManager resourceManager, TextureManager textureManager);
	}

	@Environment(EnvType.CLIENT)
	@FunctionalInterface
	public interface RecipesReceived {
		void onRecipesReceived(RecipeMap recipes);
	}
}
