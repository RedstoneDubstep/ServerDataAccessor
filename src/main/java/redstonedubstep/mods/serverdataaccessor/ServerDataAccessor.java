package redstonedubstep.mods.serverdataaccessor;

import net.minecraft.commands.Commands.CommandSelection;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.fml.IExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.network.NetworkConstants;
import redstonedubstep.mods.serverdataaccessor.commands.CommandRoot;

@Mod("serverdataaccessor")
public class ServerDataAccessor {
	public ServerDataAccessor() {
		ModLoadingContext.get().registerExtensionPoint(IExtensionPoint.DisplayTest.class, () -> new IExtensionPoint.DisplayTest(() -> NetworkConstants.IGNORESERVERONLY, (a, b) -> true));
		MinecraftForge.EVENT_BUS.addListener(this::registerCommands);
		ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, SDMConfig.SERVER_SPEC);
	}

	public void registerCommands(RegisterCommandsEvent event){
		if (event.getEnvironment() == CommandSelection.DEDICATED)
			CommandRoot.registerServerDataCommand(event.getDispatcher());

		CommandRoot.registerWorldDataCommand(event.getDispatcher());
	}
}
