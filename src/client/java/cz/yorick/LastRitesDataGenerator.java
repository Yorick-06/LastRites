package cz.yorick;

import cz.yorick.datagen.BlockLootTableGenerator;
import cz.yorick.datagen.ModelGenerator;
import cz.yorick.datagen.RecipeGenerator;
import cz.yorick.datagen.BlockTagGenerator;
import cz.yorick.item.ClayUrnItem;
import cz.yorick.item.CurseBladeItem;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider;
import net.minecraft.item.BlockItem;
import net.minecraft.registry.Registries;

public class LastRitesDataGenerator implements DataGeneratorEntrypoint {
	@Override
	public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
		FabricDataGenerator.Pack pack = fabricDataGenerator.createPack();
		pack.addProvider(RecipeGenerator::new);
		pack.addProvider(ModelGenerator::new);
		pack.addProvider(LastRitesDataGenerator::genLang);
		pack.addProvider(BlockLootTableGenerator::new);
		pack.addProvider(BlockTagGenerator::new);
	}

	private static FabricLanguageProvider genLang(FabricDataOutput dataOutput) {
		return new FabricLanguageProvider(dataOutput) {
			@Override
			public void generateTranslations(TranslationBuilder translationBuilder) {
				//blocks use the block translation key for the item
				LastRites.getOwned(Registries.ITEM)
						.filter(item -> !(item instanceof BlockItem))
						.forEach(item -> translationBuilder.add(item, autoTranslate(item.getTranslationKey())));

				LastRites.getOwned(Registries.BLOCK)
						.forEach(block -> translationBuilder.add(block, autoTranslate(block.getTranslationKey())));

				translationBuilder.add(LastRites.GRIEF_EFFECT, autoTranslate(LastRites.GRIEF_EFFECT.getTranslationKey()));
				translationBuilder.add(LastRites.DAMNED_ONE_ENTITY_TYPE, autoTranslate(LastRites.DAMNED_ONE_ENTITY_TYPE.getTranslationKey()));

				translationBuilder.add("item_group.last-rites", "Last Rites");

				translationBuilder.add(CurseBladeItem.SOUL_ASH_TRANSLATION_KEY, autoTranslate(CurseBladeItem.SOUL_ASH_TRANSLATION_KEY));
				translationBuilder.add(CurseBladeItem.BONUS_REACH_TRANSLATION_KEY, autoTranslate(CurseBladeItem.BONUS_REACH_TRANSLATION_KEY));
				translationBuilder.add(ClayUrnItem.EMPTY_TRANSLATION_KEY, autoTranslate(ClayUrnItem.EMPTY_TRANSLATION_KEY));
			}
		};
	}

	private static String autoTranslate(String translationKey) {
		String[] splitKey = translationKey.split("\\.");
		String key = splitKey[splitKey.length - 1];
		char[] chars = key.toCharArray();
		chars[0] = Character.toUpperCase(chars[0]);
		for (int i = 1; i < chars.length; i++) {
			if(chars[i] == '_') {
				chars[i] = ' ';
				continue;
			}

			if(chars[i - 1] == ' ') {
				chars[i] = Character.toUpperCase(chars[i]);
			}
		}

		return new String(chars);
	}
}
