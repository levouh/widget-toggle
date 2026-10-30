package com.widgettoggle;

import com.google.inject.Provides;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.ScriptID;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.PostClientTick;
import net.runelite.api.events.ScriptPostFired;
import net.runelite.api.events.VarClientIntChanged;
import net.runelite.api.events.WidgetLoaded;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.gameval.VarClientID;
import net.runelite.api.gameval.VarbitID;
import net.runelite.api.widgets.Widget;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.config.Keybind;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.input.KeyManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.util.HotkeyListener;

@Slf4j
@PluginDescriptor(
	name = "Widget Toggle",
	description = "Toggle selected interface elements with configurable hotkeys",
	tags = {"interface", "hotkeys", "widgets"}
)
public class WidgetTogglePlugin extends Plugin
{
	@Inject
	private WidgetToggleConfig config;

	@Inject
	private Client client;

	@Inject
	private KeyManager keyManager;

	@Inject
	private ClientThread clientThread;

	private final List<HotkeyListener> hotkeyListeners = new ArrayList<>();
	private final Map<Integer, Boolean> originalVisibility = new HashMap<>();
	private final Map<Widget, Boolean> alwaysHiddenVisibility = new IdentityHashMap<>();
	private final boolean[] slotHidden = new boolean[4];
	private int savedSidePanelTab = 3;
	private Boolean originalSidePanelOpen;
	private int savedChatTab;
	private Boolean originalChatOpen;
	private boolean visibilityRefreshPending;
	private boolean sidePanelWasOpen;

	@Override
	protected void startUp()
	{
		registerHotkey(0, config::slot1Hotkey, this::getSlot1Targets);
		registerHotkey(1, config::slot2Hotkey, this::getSlot2Targets);
		registerHotkey(2, config::slot3Hotkey, this::getSlot3Targets);
		registerHotkey(3, config::slot4Hotkey, this::getSlot4Targets);
		sidePanelWasOpen = isSidePanelOpen();
		applyAlwaysHiddenWidgets();
		log.debug("Widget Toggle started");
	}

	@Override
	protected void shutDown()
	{
		for (HotkeyListener hotkeyListener : hotkeyListeners)
		{
			keyManager.unregisterKeyListener(hotkeyListener);
		}
		hotkeyListeners.clear();

		for (Map.Entry<Integer, Boolean> entry : originalVisibility.entrySet())
		{
			Widget widget = client.getWidget(entry.getKey());
			if (widget != null)
			{
				widget.setHidden(entry.getValue());
			}
		}
		originalVisibility.clear();
		restoreAlwaysHiddenWidgets();

		if (originalSidePanelOpen != null && originalSidePanelOpen != isSidePanelOpen())
		{
			setSidePanelHidden(!originalSidePanelOpen);
		}
		originalSidePanelOpen = null;

		if (originalChatOpen != null && originalChatOpen != isChatOpen())
		{
			setChatHidden(!originalChatOpen);
		}
		originalChatOpen = null;
		for (int slot = 0; slot < slotHidden.length; slot++)
		{
			slotHidden[slot] = false;
		}
		log.debug("Widget Toggle stopped");
	}

	@Subscribe
	public void onVarClientIntChanged(VarClientIntChanged event)
	{
		if (event.getIndex() == VarClientID.CHAT_VIEW)
		{
			syncSlotHiddenStates();
			return;
		}

		if (event.getIndex() != VarClientID.TOPLEVEL_PANEL)
		{
			return;
		}

		boolean sidePanelOpen = isSidePanelOpen();
		boolean justOpened = !sidePanelWasOpen && sidePanelOpen;
		sidePanelWasOpen = sidePanelOpen;
		if (config.restoreMinimapOnSidePanelOpen() && justOpened)
		{
			restoreSidePanelSlotTargets(0, getSlot1Targets());
			restoreSidePanelSlotTargets(1, getSlot2Targets());
			restoreSidePanelSlotTargets(2, getSlot3Targets());
			restoreSidePanelSlotTargets(3, getSlot4Targets());
		}

		syncSlotHiddenStates();
		queueVisibilityRefresh();
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged event)
	{
		if (event.getGameState() == GameState.LOGGED_IN)
		{
			queueVisibilityRefresh();
		}
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged event)
	{
		if ("widget-toggle".equals(event.getGroup()))
		{
			queueVisibilityRefresh();
		}
	}

	@Subscribe
	public void onWidgetLoaded(WidgetLoaded event)
	{
		if (isTrackedWidgetGroup(event.getGroupId()))
		{
			queueVisibilityRefresh();
		}
	}

	@Subscribe
	public void onScriptPostFired(ScriptPostFired event)
	{
		int scriptId = event.getScriptId();
		if (scriptId == ScriptID.TOPLEVEL_REDRAW || scriptId == ScriptID.TOPLEVEL_RESIZE_CUSTOMISE)
		{
			queueVisibilityRefresh();
		}
	}

	@Subscribe
	public void onPostClientTick(PostClientTick event)
	{
		if (!visibilityRefreshPending)
		{
			return;
		}

		visibilityRefreshPending = false;
		reapplyHiddenWidgetTargets();
	}

	private void registerHotkey(int slot, Supplier<Keybind> keybindSupplier, Supplier<List<WidgetToggleTarget>> targetsSupplier)
	{
		HotkeyListener hotkeyListener = new HotkeyListener(keybindSupplier)
		{
			@Override
			public void hotkeyPressed()
			{
				List<WidgetToggleTarget> targets = targetsSupplier.get();
				boolean hideTargets = shouldHideTargets(targets);
				slotHidden[slot] = hideTargets;
				if (targets.contains(WidgetToggleTarget.SIDE_PANEL))
				{
					setSidePanelHidden(hideTargets);
				}

				clientThread.invokeLater(() -> setTargetsAfterScripts(targets, hideTargets));
			}
		};

		hotkeyListeners.add(hotkeyListener);
		keyManager.registerKeyListener(hotkeyListener);
	}

	private void restoreSidePanelSlotTargets(int slot, List<WidgetToggleTarget> targets)
	{
		if (targets.contains(WidgetToggleTarget.SIDE_PANEL))
		{
			slotHidden[slot] = false;
			clientThread.invokeLater(() -> setTargetsAfterScripts(targets, false));
		}
	}

	private void setTargetsAfterScripts(List<WidgetToggleTarget> targets, boolean hidden)
	{
		for (WidgetToggleTarget target : targets)
		{
			if (target != WidgetToggleTarget.SIDE_PANEL)
			{
				setWidgetHidden(target, hidden);
			}
		}
		applyAlwaysHiddenWidgets();
		queueVisibilityRefresh();
	}

	private void queueVisibilityRefresh()
	{
		visibilityRefreshPending = true;
	}

	private void reapplyHiddenWidgetTargets()
	{
		for (int slot = 0; slot < slotHidden.length; slot++)
		{
			if (!slotHidden[slot])
			{
				continue;
			}

			for (WidgetToggleTarget target : getSlotTargets(slot))
			{
				if (target == WidgetToggleTarget.MINIMAP || target == WidgetToggleTarget.STATUS_ORBS)
				{
					setWidgetHidden(target, true);
				}
			}
		}
		applyAlwaysHiddenWidgets();
	}

	private void syncSlotHiddenStates()
	{
		for (int slot = 0; slot < slotHidden.length; slot++)
		{
			slotHidden[slot] = areTargetsHidden(getSlotTargets(slot));
		}
	}

	private boolean areTargetsHidden(List<WidgetToggleTarget> targets)
	{
		if (targets.isEmpty())
		{
			return false;
		}

		for (WidgetToggleTarget target : targets)
		{
			if (!isTargetHidden(target))
			{
				return false;
			}
		}
		return true;
	}

	private boolean shouldHideTargets(List<WidgetToggleTarget> targets)
	{
		if (targets.contains(WidgetToggleTarget.SIDE_PANEL))
		{
			return isSidePanelOpen();
		}
		if (targets.contains(WidgetToggleTarget.CHATBOX))
		{
			return isChatOpen();
		}
		return !areTargetsHidden(targets);
	}

	private boolean isTargetHidden(WidgetToggleTarget target)
	{
		if (target == WidgetToggleTarget.CHATBOX)
		{
			return !isChatOpen();
		}
		if (target == WidgetToggleTarget.SIDE_PANEL)
		{
			return !isSidePanelOpen();
		}

		for (int widgetId : getWidgetIds(target))
		{
			Widget widget = client.getWidget(widgetId);
			if (widget == null || !widget.isSelfHidden())
			{
				return false;
			}
		}
		return true;
	}

	private List<WidgetToggleTarget> getSlotTargets(int slot)
	{
		switch (slot)
		{
			case 0:
				return getSlot1Targets();
			case 1:
				return getSlot2Targets();
			case 2:
				return getSlot3Targets();
			case 3:
				return getSlot4Targets();
			default:
				throw new IllegalArgumentException("Unknown hotkey slot: " + slot);
		}
	}

	private boolean isTrackedWidgetGroup(int groupId)
	{
		return groupId == InterfaceID.Orbs.UNIVERSE >>> 16
			|| groupId == InterfaceID.Toplevel.MAPCONTAINER >>> 16
			|| groupId == InterfaceID.ToplevelOsrsStretch.MAP_MINIMAP >>> 16
			|| groupId == InterfaceID.ToplevelPreEoc.MAP_MINIMAP >>> 16;
	}

	private void applyAlwaysHiddenWidgets()
	{
		setAlwaysHidden(InterfaceID.Orbs.ORB_HEALTH, config.hideHealthOrb());
		setAlwaysHidden(InterfaceID.Orbs.ORB_PRAYER, config.hidePrayerOrb());
		setAlwaysHidden(InterfaceID.Orbs.ORB_RUNENERGY, config.hideRunOrb());
		setAlwaysHidden(InterfaceID.Orbs.ORB_SPECENERGY, config.hideSpecialAttackOrb());
		setAlwaysHidden(InterfaceID.Orbs.XP_DROPS, config.hideXpButton());
		setAlwaysHidden(InterfaceID.Orbs.ORB_WORLDMAP, config.hideWorldMapButton());
		setAlwaysHidden(InterfaceID.Orbs.WIKI, config.hideWikiButton());
	}

	private void setAlwaysHidden(int widgetId, boolean hidden)
	{
		Widget widget = client.getWidget(widgetId);
		if (widget == null)
		{
			return;
		}

		setAlwaysHidden(widget, hidden);
		if (widget.getChildren() != null)
		{
			for (Widget child : widget.getChildren())
			{
				if (child != null)
				{
					setAlwaysHidden(child, hidden);
				}
			}
		}
	}

	private void setAlwaysHidden(Widget widget, boolean hidden)
	{
		if (hidden)
		{
			alwaysHiddenVisibility.putIfAbsent(widget, widget.isSelfHidden());
			widget.setHidden(true);
			return;
		}

		Boolean originalHidden = alwaysHiddenVisibility.remove(widget);
		if (originalHidden != null)
		{
			widget.setHidden(originalHidden);
		}
	}

	private void restoreAlwaysHiddenWidgets()
	{
		for (Map.Entry<Widget, Boolean> entry : alwaysHiddenVisibility.entrySet())
		{
			entry.getKey().setHidden(entry.getValue());
		}
		alwaysHiddenVisibility.clear();
	}

	private List<WidgetToggleTarget> getSlot1Targets()
	{
		return getTargets(config.slot1Chatbox(), config.slot1SidePanel(), config.slot1Minimap(), config.slot1StatusOrbs());
	}

	private List<WidgetToggleTarget> getSlot2Targets()
	{
		return getTargets(config.slot2Chatbox(), config.slot2SidePanel(), config.slot2Minimap(), config.slot2StatusOrbs());
	}

	private List<WidgetToggleTarget> getSlot3Targets()
	{
		return getTargets(config.slot3Chatbox(), config.slot3SidePanel(), config.slot3Minimap(), config.slot3StatusOrbs());
	}

	private List<WidgetToggleTarget> getSlot4Targets()
	{
		return getTargets(config.slot4Chatbox(), config.slot4SidePanel(), config.slot4Minimap(), config.slot4StatusOrbs());
	}

	private List<WidgetToggleTarget> getTargets(boolean chatbox, boolean sidePanel, boolean minimap, boolean statusOrbs)
	{
		List<WidgetToggleTarget> targets = new ArrayList<>();
		if (chatbox)
		{
			targets.add(WidgetToggleTarget.CHATBOX);
		}
		if (sidePanel)
		{
			targets.add(WidgetToggleTarget.SIDE_PANEL);
		}
		if (minimap)
		{
			targets.add(WidgetToggleTarget.MINIMAP);
		}
		if (statusOrbs)
		{
			targets.add(WidgetToggleTarget.STATUS_ORBS);
		}
		return targets;
	}

	private void setWidgetHidden(WidgetToggleTarget target, boolean hidden)
	{
		if (target == WidgetToggleTarget.CHATBOX)
		{
			setChatHidden(hidden);
			return;
		}

		for (int widgetId : getWidgetIds(target))
		{
			Widget widget = client.getWidget(widgetId);
			if (widget == null)
			{
				log.debug("Widget {} is not currently available", widgetId);
				continue;
			}

			originalVisibility.putIfAbsent(widgetId, widget.isSelfHidden());
			widget.setHidden(hidden);
		}
	}

	private int[] getWidgetIds(WidgetToggleTarget target)
	{
		if (!client.isResized())
		{
			return getFixedWidgetIds(target);
		}

		final int classicArrangement = 0;
		if (client.getVarbitValue(VarbitID.RESIZABLE_STONE_ARRANGEMENT) == classicArrangement)
		{
			return getClassicResizableWidgetIds(target);
		}

		return getModernResizableWidgetIds(target);
	}

	private int[] getFixedWidgetIds(WidgetToggleTarget target)
	{
		switch (target)
		{
			case CHATBOX:
				return new int[]{InterfaceID.Toplevel.CHAT_CONTAINER};
			case MINIMAP:
				return new int[]{InterfaceID.Toplevel.MAPCONTAINER};
			case STATUS_ORBS:
				return new int[]{InterfaceID.Toplevel.ORBS};
			case SIDE_PANEL:
				throw new IllegalArgumentException("The side panel is controlled by a client script");
			default:
				throw new IllegalArgumentException("Unknown widget-toggle target: " + target);
		}
	}

	private int[] getClassicResizableWidgetIds(WidgetToggleTarget target)
	{
		switch (target)
		{
			case CHATBOX:
				return new int[]{InterfaceID.ToplevelOsrsStretch.CHAT_CONTAINER};
			case MINIMAP:
				return new int[]{InterfaceID.ToplevelOsrsStretch.MAP_MINIMAP};
			case STATUS_ORBS:
				return new int[]{InterfaceID.ToplevelOsrsStretch.ORBS};
			case SIDE_PANEL:
				throw new IllegalArgumentException("The side panel is controlled by a client script");
			default:
				throw new IllegalArgumentException("Unknown widget-toggle target: " + target);
		}
	}

	private int[] getModernResizableWidgetIds(WidgetToggleTarget target)
	{
		switch (target)
		{
			case CHATBOX:
				return new int[]{InterfaceID.ToplevelPreEoc.CHAT_CONTAINER};
			case MINIMAP:
				return new int[]{InterfaceID.ToplevelPreEoc.MAP_MINIMAP};
			case STATUS_ORBS:
				return new int[]{InterfaceID.ToplevelPreEoc.ORBS};
			case SIDE_PANEL:
				throw new IllegalArgumentException("The side panel is controlled by a client script");
			default:
				throw new IllegalArgumentException("Unknown widget-toggle target: " + target);
		}
	}

	private boolean isSidePanelOpen()
	{
		return client.getVarcIntValue(VarClientID.TOPLEVEL_PANEL) != -1;
	}

	private void setSidePanelHidden(boolean hidden)
	{
		if (hidden == isSidePanelOpen())
		{
			toggleSidePanel();
		}
	}

	private boolean isChatOpen()
	{
		return client.getVarcIntValue(VarClientID.CHAT_VIEW) != 1337;
	}

	private void setChatHidden(boolean hidden)
	{
		if (hidden == isChatOpen())
		{
			toggleChat();
		}
	}

	private void toggleChat()
	{
		if (client.getGameState() != GameState.LOGGED_IN)
		{
			return;
		}

		int currentChatTab = client.getVarcIntValue(VarClientID.CHAT_VIEW);
		if (originalChatOpen == null)
		{
			originalChatOpen = currentChatTab != 1337;
		}
		if (currentChatTab != 1337)
		{
			savedChatTab = currentChatTab;
		}

		int targetChatTab = currentChatTab == 1337 ? savedChatTab : currentChatTab;
		client.runScript(175, 1, targetChatTab);
	}

	private void toggleSidePanel()
	{
		if (client.getGameState() != GameState.LOGGED_IN)
		{
			return;
		}

		int currentTab = client.getVarcIntValue(VarClientID.TOPLEVEL_PANEL);
		if (originalSidePanelOpen == null)
		{
			originalSidePanelOpen = currentTab != -1;
		}
		if (currentTab != -1)
		{
			savedSidePanelTab = currentTab;
		}

		clientThread.invokeLater(() -> client.runScript(914, 1, 1131, savedSidePanelTab));
	}

	@Provides
	WidgetToggleConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(WidgetToggleConfig.class);
	}
}
