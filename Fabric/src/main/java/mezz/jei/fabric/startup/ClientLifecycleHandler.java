package mezz.jei.fabric.startup;

import mezz.jei.api.IModPlugin;
import mezz.jei.common.Internal;
import mezz.jei.common.network.IConnectionToServer;
import mezz.jei.fabric.events.JeiLifecycleEvents;
import mezz.jei.fabric.network.ClientNetworkHandler;
import mezz.jei.fabric.network.ClientRecipeSyncHandler;
import mezz.jei.fabric.network.ConnectionToServer;
import mezz.jei.fabric.network.PacketRequestRecipes;
import mezz.jei.gui.config.InternalKeyMappings;
import mezz.jei.library.startup.JeiStarter;
import mezz.jei.library.startup.StartData;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
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
	private final IConnectionToServer serverConnection;
	/**
	 * The recipes the server's JEI last sent us. Only used in multiplayer.
	 * Kept across {@link #stopJei()} so that a resource reload does not lose them.
	 */
	private RecipeMap syncedRecipes = RecipeMap.EMPTY;
	private boolean gameStarted;
	private boolean running;

	public ClientLifecycleHandler() {
		this.serverConnection = new ConnectionToServer();
		Internal.setServerConnection(this.serverConnection);

		InternalKeyMappings keyMappings = new InternalKeyMappings(KeyBindingHelper::registerKeyBinding);
		Internal.setKeyMappings(keyMappings);

		ClientNetworkHandler.registerClientPacketHandler(this.serverConnection);

		List<IModPlugin> plugins = FabricPluginFinder.getModPlugins();
		StartData startData = new StartData(
			plugins,
			this.serverConnection
		);

		this.jeiStarter = new JeiStarter(startData);
	}

	public void registerEvents() {
		JeiLifecycleEvents.GAME_START.register(() -> this.gameStarted = true);
		JeiLifecycleEvents.AFTER_RECIPE_SYNC.register(this::onRecipeSync);
		JeiLifecycleEvents.SERVER_RECIPES_RECEIVED.register(this::onServerRecipesReceived);
		JeiLifecycleEvents.GAME_STOP.register(() -> {
			this.gameStarted = false;
			this.syncedRecipes = RecipeMap.EMPTY;
			stopJei();
		});
	}

	/**
	 * Vanilla has finished its own recipe sync, which since Minecraft 1.21.2 carries no
	 * recipes at all. Get the real recipes from wherever they actually live.
	 */
	private void onRecipeSync() {
		if (!this.gameStarted) {
			// recipes can arrive before the player has finished logging in
			return;
		}
		if (Minecraft.getInstance().getSingleplayerServer() != null) {
			// the integrated server is in this process, so read its recipes directly
			restartJei();
			return;
		}
		if (ClientPlayNetworking.canSend(PacketRequestRecipes.TYPE)) {
			// ask JEI on the server for the recipes and start once they arrive, so that
			// JEI starts a single time with a complete recipe list
			ClientRecipeSyncHandler.reset();
			this.serverConnection.sendPacketToServer(PacketRequestRecipes.INSTANCE);
			return;
		}
		// no JEI on the server, or one too old to send recipes. Start without them;
		// JEI reports the missing recipes in chat itself.
		restartJei();
	}

	private void onServerRecipesReceived(RecipeMap recipes) {
		if (!this.gameStarted) {
			return;
		}
		this.syncedRecipes = recipes;
		restartJei();
	}

	private void restartJei() {
		if (running) {
			stopJei();
		}
		startJei();
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

		Internal.setClientSyncedRecipes(getRecipes(minecraft));

		this.jeiStarter.start();
		running = true;
	}

	/**
	 * Since Minecraft 1.21.2 the client's own {@link net.minecraft.world.item.crafting.RecipeManager}
	 * is empty, and Fabric has no recipe sync of its own, so nothing vanilla populates
	 * {@link Internal#setClientSyncedRecipes}.
	 * In single-player the integrated server is right here, so read its recipes directly.
	 * In multiplayer they come from JEI on the server, see
	 * {@code mezz.jei.fabric.network.PacketRecipeSync}.
	 */
	private RecipeMap getRecipes(Minecraft minecraft) {
		IntegratedServer server = minecraft.getSingleplayerServer();
		if (server == null) {
			return this.syncedRecipes;
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
