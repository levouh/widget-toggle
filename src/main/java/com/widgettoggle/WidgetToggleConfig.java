package com.widgettoggle;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;
import net.runelite.client.config.Keybind;

@ConfigGroup("widget-toggle")
public interface WidgetToggleConfig extends Config
{
	@ConfigSection(name = "General", description = "General behavior for hotkey slots.", position = 0)
	String GENERAL_SECTION = "general";
	@ConfigSection(name = "Hotkey slot 1", description = "Configure the first widget-toggle hotkey.", position = 1)
	String SLOT_1_SECTION = "slot1";
	@ConfigSection(name = "Hotkey slot 2", description = "Configure the second widget-toggle hotkey.", position = 2)
	String SLOT_2_SECTION = "slot2";
	@ConfigSection(name = "Hotkey slot 3", description = "Configure the third widget-toggle hotkey.", position = 3)
	String SLOT_3_SECTION = "slot3";
	@ConfigSection(name = "Hotkey slot 4", description = "Configure the fourth widget-toggle hotkey.", position = 4)
	String SLOT_4_SECTION = "slot4";
	@ConfigSection(name = "Always hidden", description = "Hide individual interface controls independently of hotkey slots.", position = 5)
	String ALWAYS_HIDDEN_SECTION = "alwaysHidden";

	@ConfigItem(keyName = "slot1Hotkey", name = "Hotkey", description = "Hotkey for this slot.", section = SLOT_1_SECTION, position = 1)
	default Keybind slot1Hotkey() { return Keybind.NOT_SET; }
	@ConfigItem(keyName = "slot1Chatbox", name = "Chatbox", description = "Toggle the chatbox with this hotkey.", section = SLOT_1_SECTION, position = 2)
	default boolean slot1Chatbox() { return false; }
	@ConfigItem(keyName = "slot1SidePanel", name = "Side panel", description = "Toggle the entire right-side panel, including its tabs and contents, with this hotkey.", section = SLOT_1_SECTION, position = 3)
	default boolean slot1SidePanel() { return false; }
	@ConfigItem(keyName = "slot1Minimap", name = "Minimap", description = "Toggle the minimap with this hotkey.", section = SLOT_1_SECTION, position = 4)
	default boolean slot1Minimap() { return false; }
	@ConfigItem(keyName = "slot1StatusOrbs", name = "Status orbs", description = "Toggle the health, prayer, run, and special-attack orbs with this hotkey.", section = SLOT_1_SECTION, position = 5)
	default boolean slot1StatusOrbs() { return false; }

	@ConfigItem(keyName = "slot2Hotkey", name = "Hotkey", description = "Hotkey for this slot.", section = SLOT_2_SECTION, position = 6)
	default Keybind slot2Hotkey() { return Keybind.NOT_SET; }
	@ConfigItem(keyName = "slot2Chatbox", name = "Chatbox", description = "Toggle the chatbox with this hotkey.", section = SLOT_2_SECTION, position = 7)
	default boolean slot2Chatbox() { return false; }
	@ConfigItem(keyName = "slot2SidePanel", name = "Side panel", description = "Toggle the entire right-side panel, including its tabs and contents, with this hotkey.", section = SLOT_2_SECTION, position = 8)
	default boolean slot2SidePanel() { return false; }
	@ConfigItem(keyName = "slot2Minimap", name = "Minimap", description = "Toggle the minimap with this hotkey.", section = SLOT_2_SECTION, position = 9)
	default boolean slot2Minimap() { return false; }
	@ConfigItem(keyName = "slot2StatusOrbs", name = "Status orbs", description = "Toggle the health, prayer, run, and special-attack orbs with this hotkey.", section = SLOT_2_SECTION, position = 10)
	default boolean slot2StatusOrbs() { return false; }

	@ConfigItem(keyName = "slot3Hotkey", name = "Hotkey", description = "Hotkey for this slot.", section = SLOT_3_SECTION, position = 11)
	default Keybind slot3Hotkey() { return Keybind.NOT_SET; }
	@ConfigItem(keyName = "slot3Chatbox", name = "Chatbox", description = "Toggle the chatbox with this hotkey.", section = SLOT_3_SECTION, position = 12)
	default boolean slot3Chatbox() { return false; }
	@ConfigItem(keyName = "slot3SidePanel", name = "Side panel", description = "Toggle the entire right-side panel, including its tabs and contents, with this hotkey.", section = SLOT_3_SECTION, position = 13)
	default boolean slot3SidePanel() { return false; }
	@ConfigItem(keyName = "slot3Minimap", name = "Minimap", description = "Toggle the minimap with this hotkey.", section = SLOT_3_SECTION, position = 14)
	default boolean slot3Minimap() { return false; }
	@ConfigItem(keyName = "slot3StatusOrbs", name = "Status orbs", description = "Toggle the health, prayer, run, and special-attack orbs with this hotkey.", section = SLOT_3_SECTION, position = 15)
	default boolean slot3StatusOrbs() { return false; }

	@ConfigItem(keyName = "slot4Hotkey", name = "Hotkey", description = "Hotkey for this slot.", section = SLOT_4_SECTION, position = 16)
	default Keybind slot4Hotkey() { return Keybind.NOT_SET; }
	@ConfigItem(keyName = "slot4Chatbox", name = "Chatbox", description = "Toggle the chatbox with this hotkey.", section = SLOT_4_SECTION, position = 17)
	default boolean slot4Chatbox() { return false; }
	@ConfigItem(keyName = "slot4SidePanel", name = "Side panel", description = "Toggle the entire right-side panel, including its tabs and contents, with this hotkey.", section = SLOT_4_SECTION, position = 18)
	default boolean slot4SidePanel() { return false; }
	@ConfigItem(keyName = "slot4Minimap", name = "Minimap", description = "Toggle the minimap with this hotkey.", section = SLOT_4_SECTION, position = 19)
	default boolean slot4Minimap() { return false; }
	@ConfigItem(keyName = "slot4StatusOrbs", name = "Status orbs", description = "Toggle the health, prayer, run, and special-attack orbs with this hotkey.", section = SLOT_4_SECTION, position = 20)
	default boolean slot4StatusOrbs() { return false; }

	@ConfigItem(keyName = "restoreMinimapOnSidePanelOpen", name = "Restore linked elements when a side panel opens", description = "Restore all elements selected alongside Side panel when you open a side-panel tab, such as inventory or prayers.", section = GENERAL_SECTION, position = 1)
	default boolean restoreMinimapOnSidePanelOpen() { return false; }

	@ConfigItem(keyName = "hideHealthOrb", name = "Health orb", description = "Always hide the health orb.", section = ALWAYS_HIDDEN_SECTION, position = 1)
	default boolean hideHealthOrb() { return false; }
	@ConfigItem(keyName = "hidePrayerOrb", name = "Prayer orb", description = "Always hide the prayer orb.", section = ALWAYS_HIDDEN_SECTION, position = 2)
	default boolean hidePrayerOrb() { return false; }
	@ConfigItem(keyName = "hideRunOrb", name = "Run orb", description = "Always hide the run-energy orb.", section = ALWAYS_HIDDEN_SECTION, position = 3)
	default boolean hideRunOrb() { return false; }
	@ConfigItem(keyName = "hideSpecialAttackOrb", name = "Special-attack orb", description = "Always hide the special-attack orb.", section = ALWAYS_HIDDEN_SECTION, position = 4)
	default boolean hideSpecialAttackOrb() { return false; }
	@ConfigItem(keyName = "hideXpButton", name = "XP button", description = "Always hide the XP drops button.", section = ALWAYS_HIDDEN_SECTION, position = 5)
	default boolean hideXpButton() { return false; }
	@ConfigItem(keyName = "hideWorldMapButton", name = "World Map button", description = "Always hide the World Map button.", section = ALWAYS_HIDDEN_SECTION, position = 6)
	default boolean hideWorldMapButton() { return false; }
	@ConfigItem(keyName = "hideWikiButton", name = "Wiki button", description = "Always hide the Wiki button.", section = ALWAYS_HIDDEN_SECTION, position = 7)
	default boolean hideWikiButton() { return false; }
}
