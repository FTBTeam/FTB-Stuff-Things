/*
 * This file is part of pnc-repressurized.
 *
 *     pnc-repressurized is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     pnc-repressurized is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with pnc-repressurized.  If not, see <https://www.gnu.org/licenses/>.
 */

package dev.ftb.mods.ftbobb.integration.jei;

import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.network.chat.Component;

public abstract class BaseOBBCategory<T> implements IRecipeCategory<T> {
    private final RecipeType<T> type;
    private final Component localizedName;
    private final IDrawable background;
    private final IDrawable icon;

    protected BaseOBBCategory(RecipeType<T> type, Component localizedName, IDrawable background, IDrawable icon) {
        this.type = type;
        this.localizedName = localizedName;
        this.background = background;
        this.icon = icon;
    }

    @Override
    public Component getTitle() {
        return localizedName;
    }

    @Override
    public IDrawable getBackground() {
        return background;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public RecipeType<T> getRecipeType() {
        return type;
    }

    static IGuiHelper guiHelper() {
        return OBBJeiPlugin.jeiHelpers.getGuiHelper();
    }
}
