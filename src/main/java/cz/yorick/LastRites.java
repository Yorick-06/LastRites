package cz.yorick;

import cz.yorick.block.BrazierBlock;
import cz.yorick.block.ClayUrnBlock;
import cz.yorick.block.SoulAshBlock;
import cz.yorick.block.entity.BrazierBlockEntity;
import cz.yorick.entity.DamnedOneEntity;
import cz.yorick.item.ClayUrnItem;
import cz.yorick.item.CurseBladeItem;
import cz.yorick.item.EldritchUrnItem;
import cz.yorick.util.GriefStatusEffect;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.datafixer.TypeReferences;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.mob.VexEntity;
import net.minecraft.item.*;
import net.minecraft.potion.Potion;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.stream.Stream;

public class LastRites implements ModInitializer {
	public static final String MOD_ID = "last-rites";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		FabricDefaultAttributeRegistry.register(DAMNED_ONE_ENTITY_TYPE, VexEntity.createVexAttributes());
	}

	public static final Block SOUL_ASH = register("soul_ash", new SoulAshBlock(AbstractBlock.Settings.create()), true);
	public static final Block CLAY_URN = register("clay_urn", new ClayUrnBlock(AbstractBlock.Settings.create()), false);
	public static final Block BRAZIER = register("brazier", new BrazierBlock(AbstractBlock.Settings.create().luminance(state -> state.get(BrazierBlock.FIRE) != BrazierBlock.FireType.NONE ? 15 : 0)), true);

	public static final BlockEntityType<BrazierBlockEntity> BRAZIER_BLOCK_ENTITY = register("brazier", BRAZIER, BrazierBlockEntity::new);

	public static final Item ELDRITCH_URN = register("eldritch_urn", new EldritchUrnItem(new Item.Settings().maxCount(1)));
	public static final Item CLAY_URN_ITEM = register("clay_urn", new ClayUrnItem(CLAY_URN, new Item.Settings()));
	public static final Item CURSEBLADE = register("curseblade", new CurseBladeItem(ToolMaterials.DIAMOND, 2, -2.4F, new Item.Settings()));

	public static final StatusEffect GRIEF_EFFECT = Registry.register(Registries.STATUS_EFFECT, Identifier.of(MOD_ID, "grief"), new GriefStatusEffect());
	public static final Potion GRIEF_POTION = Registry.register(Registries.POTION, Identifier.of(MOD_ID, "grief"), new Potion("grief", new StatusEffectInstance(GRIEF_EFFECT, 600)));

	public static final ItemGroup ITEM_GROUP = Registry.register(Registries.ITEM_GROUP, Identifier.of(MOD_ID, MOD_ID), FabricItemGroup.builder()
			.displayName(Text.translatable("item_group.last-rites"))
					.icon(() -> new ItemStack(ELDRITCH_URN))
					.entries((displayContext, entries) -> getOwned(Registries.ITEM).forEach(item -> entries.add(new ItemStack(item))))
			.build()
	);

	public static final RegistryKey<EntityType<?>> DAMNED_ONE_REGISTRY_KEY = RegistryKey.of(RegistryKeys.ENTITY_TYPE, Identifier.of(MOD_ID, "damned_one"));
	public static final EntityType<DamnedOneEntity> DAMNED_ONE_ENTITY_TYPE = Registry.register(Registries.ENTITY_TYPE, DAMNED_ONE_REGISTRY_KEY,
			EntityType.Builder
					.create(DamnedOneEntity::new, SpawnGroup.MISC)
					.setDimensions(0.6F, 1.7F)
					.makeFireImmune()
					.build("damned_one")
	);

	private static Item register(String id, Item item) {
		return Registry.register(Registries.ITEM, Identifier.of(MOD_ID, id), item);
	}

	private static Block register(String id, Block block, boolean item) {
		if(item) {
			register(id, new BlockItem(block, new Item.Settings()));
		}
		return Registry.register(Registries.BLOCK, Identifier.of(MOD_ID, id), block);
	}

	private static <T extends BlockEntity> BlockEntityType<T> register(String id, Block block, BlockEntityType.BlockEntityFactory<T> factory) {
		Identifier identifier = Identifier.of(MOD_ID, id);
		return Registry.register(
				Registries.BLOCK_ENTITY_TYPE,
				identifier,
				new BlockEntityType<>(factory, Set.of(block), Util.getChoiceType(TypeReferences.BLOCK_ENTITY, id))
		);
	}

	public static <T> Stream<T> getOwned(Registry<T> registry) {
		return registry.streamEntries()
				.filter(entry -> entry.getKey().map(tRegistryKey -> tRegistryKey.getValue().getNamespace().equals(MOD_ID)).orElse(false))
				.map(RegistryEntry.Reference::value);
	}
}