package com.existingeevee.moretcon.other;

import java.util.ArrayList;
import java.util.List;

import com.existingeevee.moretcon.ModInfo;
import com.existingeevee.moretcon.traits.ModTraits;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.gson.annotations.SerializedName;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.OreDictionary;
import slimeknights.mantle.client.book.data.BookData;
import slimeknights.mantle.client.book.data.element.ImageData;
import slimeknights.mantle.client.book.data.element.TextData;
import slimeknights.mantle.client.gui.book.GuiBook;
import slimeknights.mantle.client.gui.book.element.BookElement;
import slimeknights.mantle.client.gui.book.element.ElementImage;
import slimeknights.mantle.client.gui.book.element.ElementText;
import slimeknights.mantle.util.ItemStackList;
import slimeknights.tconstruct.common.ClientProxy;
import slimeknights.tconstruct.library.Util;
import slimeknights.tconstruct.library.book.TinkerPage;
import slimeknights.tconstruct.library.book.elements.ElementTinkerItem;
import slimeknights.tconstruct.library.client.CustomFontColor;
import slimeknights.tconstruct.library.materials.Material;
import slimeknights.tconstruct.library.modifiers.IModifier;
import slimeknights.tconstruct.library.modifiers.IModifierDisplay;
import slimeknights.tconstruct.library.tools.ToolCore;
import slimeknights.tconstruct.tools.TinkerMaterials;
import slimeknights.tconstruct.tools.TinkerModifiers;

@SideOnly(Side.CLIENT)
public class ContentLigntningModifier extends TinkerPage {

	public static final transient String ID = "lightning_modifier";

	public static final ResourceLocation BOOK_MODIFY = new ResourceLocation(ModInfo.MODID, "textures/other/lightning_modify.png");

	public static final transient int TEX_SIZE = 256;
	//public static final transient ImageData IMG_MODIFY = new ImageData(ClientProxy.BOOK_MODIFY), 0, 0, 122, 70, TEX_SIZE, TEX_SIZE);
	public static final transient ImageData IMG_SLOT_1 = new ImageData(ClientProxy.BOOK_MODIFY, 0, 75, 22, 22, TEX_SIZE, TEX_SIZE);
	public static final transient ImageData IMG_SLOT_2 = new ImageData(ClientProxy.BOOK_MODIFY, 0, 97, 40, 22, TEX_SIZE, TEX_SIZE);
	public static final transient ImageData IMG_SLOT_3 = new ImageData(ClientProxy.BOOK_MODIFY, 0, 119, 58, 22, TEX_SIZE, TEX_SIZE);
	public static final transient ImageData IMG_SLOT_5 = new ImageData(ClientProxy.BOOK_MODIFY, 0, 141, 58, 41, TEX_SIZE, TEX_SIZE);
	public static final transient ImageData IMG_TABLE = new ImageData(BOOK_MODIFY, 214, 0, 42, 46, TEX_SIZE, TEX_SIZE);

	private transient IModifier modifier = ModTraits.modShocked;
	private transient List<Item> tool;

	public TextData[] text;
	public String[] effects;

	public String instruction = "";

	@SerializedName("modifier")
	public String modifierName = ModTraits.modShocked.getIdentifier();
	public String[] demoTool = new String[] { Util.getResource("pickaxe").toString() };

	public ContentLigntningModifier() {
	}

	@Override
	public void load() {
		if (tool == null) {
			tool = Lists.newArrayList();
			for (String entry : demoTool) {
				Item item = Item.REGISTRY.getObject(new ResourceLocation(entry));
				if (item != null) {
					tool.add(item);
				}
			}
		}
	}

	@Override
	public void build(BookData book, ArrayList<BookElement> list, boolean rightSide) {
		if (modifier == null) {
			TinkerModifiers.log.error("MOdifier " + modifierName + " not found");
			return;
		}
		
		int color = 0xdddddd;
		int inCount = 1;
		ItemStack[][] inputItems = null;
		if (modifier instanceof IModifierDisplay) {
			IModifierDisplay modifierDisplay = (IModifierDisplay) modifier;
			color = modifierDisplay.getColor();

			// determine how many slots we need to display
			List<List<ItemStack>> inputList = modifierDisplay.getItems(); // cache it, can be expensive
			inputItems = new ItemStack[5][]; // max size so we don't have to iterate twice

			for (int i = 0; i < 5; i++) {
				inputItems[i] = new ItemStack[inputList.size()];
				for (int j = 0; j < inputItems[i].length; j++) {
					inputItems[i][j] = ItemStack.EMPTY;
				}
			}

			for (int i = 0; i < inputList.size(); i++) {
				List<ItemStack> inputs = new ArrayList<>(inputList.get(i));
				if (inputs.size() > inCount) {
					inCount = inputs.size();
				}

				for (int j = 0; j < inputs.size() && j < 5; j++) {
					ItemStack stack = inputs.get(j);
					if (!stack.isEmpty() && stack.getMetadata() == OreDictionary.WILDCARD_VALUE) {
						stack = stack.copy();
						stack.setItemDamage(0);
					}
					inputItems[j][i] = stack;
				}
			}
		}

		addTitle(list, CustomFontColor.encodeColor(color) + modifier.getLocalizedName(), true);

		// description
		int h = GuiBook.PAGE_WIDTH / 3 - 10;
		list.add(new ElementText(10, 20, GuiBook.PAGE_WIDTH - 20, h, text));

		if (effects.length > 0) {
			TextData head = new TextData(parent.translate("modifier.effect"));
			head.underlined = true;
			list.add(new ElementText(10, 20 + h, GuiBook.PAGE_WIDTH / 2 - 5, GuiBook.PAGE_HEIGHT - h - 20, head));

			List<TextData> effectData = Lists.newArrayList();
			for (String e : effects) {
				effectData.add(new TextData("\u25CF "));
				effectData.add(new TextData(e));
				effectData.add(new TextData("\n"));
			}

			list.add(new ElementText(10, 30 + h, GuiBook.PAGE_WIDTH / 2 + 5, GuiBook.PAGE_HEIGHT - h - 20, effectData));
		}

		ImageData img = IMG_SLOT_1;

		int imgX = GuiBook.PAGE_WIDTH / 2 + 20;
		int imgY = GuiBook.PAGE_HEIGHT / 2 + 30;

		imgX = imgX + 29 - img.width / 2;
		imgY = imgY + 20 - img.height / 2;

		list.add(new ElementImage(imgX + (img.width - IMG_TABLE.width) / 2, imgY - 24, -1, -1, IMG_TABLE));

		ItemStackList demo = getDemoTools(inputItems);

		list.add(new ElementText(10, imgY + 3, GuiBook.PAGE_WIDTH / 2 + 5, GuiBook.PAGE_HEIGHT - h - 20, instruction));

		ElementTinkerItem toolItem = new ElementTinkerItem(imgX + 3, imgY + 3, 1f, demo);
		toolItem.noTooltip = true;

		list.add(toolItem);
	}

	protected ItemStackList getDemoTools(ItemStack[][] inputItems) {
		ItemStackList demo = ItemStackList.withSize(tool.size());

		for (int i = 0; i < tool.size(); i++) {
			if (tool.get(i) instanceof ToolCore) {
				ToolCore core = (ToolCore) tool.get(i);
				List<Material> mats = ImmutableList.of(TinkerMaterials.wood, TinkerMaterials.cobalt, TinkerMaterials.ardite, TinkerMaterials.manyullyn);
				mats = mats.subList(0, core.getRequiredComponents().size());
				demo.set(i, ((ToolCore) tool.get(i)).buildItemForRendering(mats));
			} else if (tool != null) {
				demo.set(i, new ItemStack(tool.get(i)));
			}

			if (!demo.get(i).isEmpty()) {
				modifier.apply(demo.get(i));
			}
		}

		return demo;
	}
}
