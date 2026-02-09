package redstonedubstep.mods.serverdataaccessor.commands;

import com.mojang.brigadier.CommandDispatcher;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.permissions.PermissionCheck;
import redstonedubstep.mods.serverdataaccessor.SDMConfig;
import redstonedubstep.mods.serverdataaccessor.commands.server.CrashReportsCommand;
import redstonedubstep.mods.serverdataaccessor.commands.server.LogsCommand;
import redstonedubstep.mods.serverdataaccessor.commands.server.ServerPropertiesCommand;
import redstonedubstep.mods.serverdataaccessor.commands.world.AdvancementsCommand;
import redstonedubstep.mods.serverdataaccessor.commands.world.DimensionDataCommand;
import redstonedubstep.mods.serverdataaccessor.commands.world.PlayerDataCommand;
import redstonedubstep.mods.serverdataaccessor.commands.world.RegionFileCommand;
import redstonedubstep.mods.serverdataaccessor.commands.world.StatisticsCommand;
import redstonedubstep.mods.serverdataaccessor.commands.world.StructuresCommand;
import redstonedubstep.mods.serverdataaccessor.commands.world.WorldDataCommand;

public class CommandRoot {
	public static void registerServerDataCommand(CommandDispatcher<CommandSourceStack> dispatcher) {
		dispatcher.register(Commands.literal("serverdataaccess").requires(ctx -> getPermissionCheckFromLevel(SDMConfig.CONFIG.serverdataCommandPermissionLevel.get()).check(ctx.permissions()))
				.then(CrashReportsCommand.register())
				.then(LogsCommand.register())
				.then(ServerPropertiesCommand.register()));
	}

	public static void registerWorldDataCommand(CommandDispatcher<CommandSourceStack> dispatcher) {
		dispatcher.register(Commands.literal("worlddataaccess").requires(ctx -> getPermissionCheckFromLevel(SDMConfig.CONFIG.worlddataCommandPermissionLevel.get()).check(ctx.permissions()))
				.then(AdvancementsCommand.register())
				.then(DimensionDataCommand.register())
				.then(PlayerDataCommand.register())
				.then(RegionFileCommand.register())
				.then(StatisticsCommand.register())
				.then(StructuresCommand.register())
				.then(WorldDataCommand.register()));
	}

	public static PermissionCheck getPermissionCheckFromLevel(int permissionLevel) {
		return switch (permissionLevel) {
			case 1 -> Commands.LEVEL_MODERATORS;
			case 2 -> Commands.LEVEL_GAMEMASTERS;
			case 3 -> Commands.LEVEL_ADMINS;
			case 4 -> Commands.LEVEL_OWNERS;
			default -> Commands.LEVEL_ALL;
		};
	}
}
