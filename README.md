# Widget Toggle

RuneLite external plugin for toggling common interface elements with configurable
hotkeys. It provides four independent hotkey slots. For each slot, choose the
hotkey and tick one or more targets: chatbox, the entire side panel, minimap,
and status orbs.
Each hotkey is disabled until you configure it in the plugin settings.

Optionally, the minimap can reappear automatically when you open a side-panel
tab such as inventory or prayers.

## Local development

Run the development client with:

```powershell
.\gradlew.bat run
```

Run automated checks with:

```powershell
.\gradlew.bat test
```

The client starts in developer mode and loads this plugin directly from the project.
