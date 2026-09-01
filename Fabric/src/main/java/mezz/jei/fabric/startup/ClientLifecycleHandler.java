package mezz.jei.fabric.startup;

import mezz.jei.api.IModPlugin;
import mezz.jei.common.Internal;
import mezz.jei.common.network.IConnectionToServer;
import mezz.jei.fabric.events.JeiLifecycleEvents;
import mezz.jei.fabric.network.ClientNetworkHandler;
import mezz.jei.fabric.network.ConnectionToServer;
import mezz.jei.gui.config.InternalKeyMappings;
import mezz.jei.library.startup.JeiStarter;
import mezz.jei.library.startup.StartData;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collection;
import java.util.List;

public class ClientLifecycleHandler {
	private static final Logger LOGGER = LogManager.getLogger();

	private final JeiStarter jeiStarter;
	private boolean running;

	public ClientLifecycleHandler() {
		IConnectionToServer serverConnection = new ConnectionToServer();
		Internal.setServerConnection(serverConnection);

		InternalKeyMappings keyMappings = new InternalKeyMappings(KeyBindingHelper::registerKeyBinding);
		Internal.setKeyMappings(keyMappings);

		ClientNetworkHandler.registerClientPacketHandler(serverConnection);

		List<IModPlugin> plugins = FabricPluginFinder.getModPlugins();
		StartData startData = new StartData(
			plugins,
			serverConnection
		);

		this.jeiStarter = new JeiStarter(startData);
	}

	public void registerEvents() {
		JeiLifecycleEvents.GAME_START.register(() ->
			JeiLifecycleEvents.AFTER_RECIPE_SYNC.register(() -> {
				if (running) {
					stopJei();
				}
				startJei();
			})
		);
		JeiLifecycleEvents.GAME_STOP.register(this::stopJei);
	}

	public ResourceManagerReloadListener getReloadListener() {
		return (resourceManager) -> {
			if (running) {
				Minecraft minecraft = Minecraft.getInstance();
				if (!minecraft.isSameThread()) {
					// we may receive reload events on the server thread in single-player, ignore them
					return;
				}
				stopJei();
				startJei();
			}
		};
	}

	private void startJei() {
		Minecraft minecraft = Minecraft.getInstance();
		if (minecraft.level == null) {
			LOGGER.error("Failed to start JEI, there is no Minecraft client level.");
			return;
		}
		if (running) {
			LOGGER.error("Failed to start JEI, it is already running.");
			return;
		}

		Internal.setClientSyncedRecipes(getIntegratedServerRecipes(minecraft));

		this.jeiStarter.start();
		running = true;
	}

	/**
	 * Fabric has no recipe sync from the server on this branch, and since Minecraft 1.21.2
	 * the client's own {@link net.minecraft.world.item.crafting.RecipeManager} is empty,
	 * so nothing ever populates {@link Internal#setClientSyncedRecipes}.
	 * In single-player the integrated server is right here, so read its recipes directly.
	 * In multiplayer this stays empty and JEI reports the missing recipes in chat.
	 */
	private static RecipeMap getIntegratedServerRecipes(Minecraft minecraft) {
		IntegratedServer server = minecraft.getSingleplayerServer();
		if (server == null) {
			return RecipeMap.EMPTY;
		}
		Collection<RecipeHolder<?>> recipes = server.getRecipeManager().getRecipes();
		LOGGER.info("Read {} recipes from the integrated server.", recipes.size());
		return RecipeMap.create(recipes);
	}

	private void stopJei() {
		LOGGER.info("Stopping JEI");
		this.jeiStarter.stop();
		Internal.setClientSyncedRecipes(RecipeMap.EMPTY);
		running = false;
	}
}
