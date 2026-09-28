package redstonedubstep.mods.serverdataaccessor.commands.world;

import java.io.File;
import java.nio.file.Path;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.DimensionArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.storage.LevelResource;

public class RegionFileCommand {
	public static ArgumentBuilder<CommandSourceStack, ?> register() {
		return Commands.literal("regions")
				.then(Commands.argument("dimension", DimensionArgument.dimension())
						.then(Commands.literal("entities").executes(ctx -> getRegionFolderInformation(ctx, "entities")))
						.then(Commands.literal("points-of-interests").executes(ctx -> getRegionFolderInformation(ctx, "poi")))
						.then(Commands.literal("region").executes(ctx -> getRegionFolderInformation(ctx, "region"))));
	}

	private static int getRegionFolderInformation(CommandContext<CommandSourceStack> ctx, String regionSubFolder) throws CommandSyntaxException {
		ResourceKey<Level> dimensionType = DimensionArgument.getDimension(ctx, "dimension").dimension();
		Path basePath = ctx.getSource().getServer().getWorldPath(LevelResource.ROOT).getParent();
		String levelName = dimensionType.identifier().toString();
		Path dimensionPath = DimensionType.getStorageFolder(dimensionType, basePath);
		File regionFolder = dimensionPath.resolve(regionSubFolder).toFile();
		File[] regionFiles = regionFolder.listFiles();
		long totalSize = 0;

		if (regionFiles == null || regionFiles.length == 0) {
			ctx.getSource().sendFailure(Component.translatable("Could not find region files in folder \"%s\" of dimension \"%2$s\"", regionSubFolder, levelName));
			return 0;
		}

		int notEmptyFiles = 0;

		for (File regionFile : regionFiles) {
			if (regionFile.isFile() && regionFile.length() > 0) {
				notEmptyFiles++;
				totalSize += regionFile.length();
			}
		}

		int finalNotEmptyFiles = notEmptyFiles;
		long finalTotalSize = totalSize;

		ctx.getSource().sendSuccess(() -> Component.translatable("Found %1$s region files of type \"%2$s\" in dimension \"%3$s\", of which %4$s are empty, with a total size of %5$s kilobytes", regionFiles.length, regionSubFolder, levelName, regionFiles.length - finalNotEmptyFiles, finalTotalSize / 1024), false);
		return regionFiles.length;
	}
}
