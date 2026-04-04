package redstonedubstep.mods.serverdataaccessor.commands.world;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;

import net.minecraft.ChatFormatting;
import net.minecraft.SharedConstants;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.DimensionArgument;
import net.minecraft.commands.arguments.IdentifierArgument;
import net.minecraft.commands.arguments.NbtPathArgument;
import net.minecraft.commands.arguments.NbtPathArgument.NbtPath;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.storage.SavedDataStorage;
import redstonedubstep.mods.serverdataaccessor.util.TagFormatUtil;

public class DimensionDataCommand {
	private static final SuggestionProvider<CommandSourceStack> SUGGEST_LEVEL_DATA_FILES = (ctx, suggestionsBuilder) -> SharedSuggestionProvider.suggest(suggestDimensionDataFiles(ctx), suggestionsBuilder);

	public static ArgumentBuilder<CommandSourceStack, ?> register() {
		return Commands.literal("dimensiondata")
				.then(Commands.argument("dimension", DimensionArgument.dimension())
						.then(Commands.argument("file", IdentifierArgument.id()).suggests(SUGGEST_LEVEL_DATA_FILES).executes(ctx -> getLevelData(ctx, DimensionArgument.getDimension(ctx, "dimension"), IdentifierArgument.getId(ctx, "file"), 1, null))
								.then(Commands.argument("page", IntegerArgumentType.integer(1)).executes(ctx -> getLevelData(ctx, DimensionArgument.getDimension(ctx, "dimension"), IdentifierArgument.getId(ctx, "file"), IntegerArgumentType.getInteger(ctx, "page"), null))
										.then(Commands.argument("path", NbtPathArgument.nbtPath()).executes(ctx -> getLevelData(ctx, DimensionArgument.getDimension(ctx, "dimension"), IdentifierArgument.getId(ctx, "file"), IntegerArgumentType.getInteger(ctx, "page"), NbtPathArgument.getPath(ctx, "path")))))));
	}

	private static int getLevelData(CommandContext<CommandSourceStack> ctx, ServerLevel level, Identifier filename, int page, NbtPath path) throws CommandSyntaxException {
		SavedDataStorage dataStorage = level.getDataStorage();
		String levelName = ctx.getArgument("dimension", Identifier.class).toString();
		CompoundTag data;

		try {
			 data = dataStorage.readTagFromDisk(dataStorage.getDataFile(filename), null, SharedConstants.getCurrentVersion().dataVersion().version()).getCompoundOrEmpty("data");
		} catch(Exception exception) {
			ctx.getSource().sendFailure(Component.translatable("Couldn't read data \"%1$s\" of dimension \"%2$s\"", filename, levelName));
			return 0;
		}

		Tag foundTag = path != null ? path.get(data).iterator().next() : data;

		int totalTagEntries = TagFormatUtil.getTagSize(foundTag);
		int totalPages = (int)Math.ceil(totalTagEntries / 50D);
		int currentPage = page > totalPages ? totalPages - 1 : page - 1;

		if (totalTagEntries == 0) {
			ctx.getSource().sendFailure(Component.translatable("Data \"%1$s\" of dimension \"%2$s\" does not contain any tags at given path", filename, levelName));
			return 0;
		}

		if (foundTag instanceof CompoundTag compoundTag)
			TagFormatUtil.removeNestedCollectionTags(compoundTag);

		TagFormatUtil.splitTagToPage(foundTag, currentPage, 50);
		ctx.getSource().sendSuccess(() -> Component.translatable("Sending data with name \"%1$s\" at path \"%2$s\" of dimension \"%3$s\" (%4$s total entries): %5$s", Component.literal(filename.toString()).withStyle(ChatFormatting.GRAY), Component.literal(path != null ? path.toString() : "").withStyle(ChatFormatting.AQUA), levelName, totalTagEntries, NbtUtils.toPrettyComponent(foundTag)), false);

		if (totalPages > 1)
			ctx.getSource().sendSuccess(() -> Component.translatable("Displaying page %1$s out of %2$s with %3$s entries", currentPage + 1, totalPages, TagFormatUtil.getTagSize(foundTag)), false);

		return totalTagEntries;
	}

	private static Stream<String> suggestDimensionDataFiles(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
		ServerLevel dimension = DimensionArgument.getDimension(ctx, "dimension");
		Path dataFolderForDimension = dimension.getDataStorage().dataFolder;
		Stream<String> files;

		try {
			files = Files.walk(dataFolderForDimension).filter(p -> p.toFile().isFile()).map(p -> p.subpath(p.getNameCount() - 2, p.getNameCount()).toString().replace("\\", ":").replace(".dat", ""));
		}
		catch (Exception e) {
			ctx.getSource().sendFailure(Component.translatable("Couldn't read data of dimension \"%1$s\"", dimension));
			files = Stream.of();
		}

		return files;
	}
}
