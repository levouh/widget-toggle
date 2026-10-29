package com.widgettoggle;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class WidgetTogglePluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(WidgetTogglePlugin.class);
		RuneLite.main(args);
	}
}
