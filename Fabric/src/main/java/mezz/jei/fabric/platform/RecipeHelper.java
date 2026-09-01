package mezz.jei.fabric.platform;

import mezz.jei.api.recipe.vanilla.IJeiBrewingRecipe;
import mezz.jei.api.recipe.vanilla.IVanillaRecipeFactory;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.common.platform.IPlatformRecipeHelper;
import net.minecraft.core.Holder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.ShapedRecipePattern;
import net.minecraft.world.item.crafting.SmithingRecipe;
import net.minecraft.world.item.enchantment.Enchantment;

import java.util.List;
import java.util.Optional;

public class RecipeHelper implements IPlatformRecipeHelper {
	@Override
	public Optional<Ingredient> getBase(SmithingRecipe recipe) {
		return recipe.baseIngredient();
	}

	@Override
	public Optional<Ingredient> getAddition(SmithingRecipe recipe) {
		return recipe.additionIngredient();
	}

	@Override
	public Optional<Ingredient> getTemplate(SmithingRecipe recipe) {
		return recipe.templateIngredient();
	}

	@Override
	public List<IJeiBrewingRecipe> getBrewingRecipes(IIngredientManager ingredientManager, IVanillaRecipeFactory vanillaRecipeFactory, PotionBrewing potionBrewing) {
		return BrewingRecipeMaker.getBrewingRecipes(ingredientManager, vanillaRecipeFactory, potionBrewing);
	}

	@Override
	public String[] shrinkShapedRecipePattern(List<String> pattern) {
		return ShapedRecipePattern.shrink(pattern);
	}

	@Override
	public boolean isItemEnchantable(ItemStack stack, Holder<Enchantment> enchantment) {
		return true;
	}
}
