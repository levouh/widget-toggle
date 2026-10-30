# Widget Toggle

Widget Toggle is a RuneLite external plugin that lets you show or hide common game-interface elements with configurable hotkeys. It is designed for a clean, compact layout without replacing RuneLite's normal tab controls.

## Features

- Four independently configurable hotkey slots
- Each slot can control one or more elements together
- Supports Fixed, Resizable Classic, and Resizable Modern layouts
- Uses RuneLite's normal side-panel and chatbox behavior
- Optional side-panel links for the minimap and status orbs
- Optional persistent hiding for individual orbs and utility buttons

## Hotkey slots

Each of the four **Hotkey slot** sections contains a hotkey and checkboxes for the elements it controls:

| Target | Behavior |
| --- | --- |
| Chatbox | Opens or closes the game chatbox. |
| Side panel | Opens or closes the whole right-hand tab panel, including its tab contents. |
| Minimap | Shows or hides the minimap and compass together. |
| Status orbs | Shows or hides the status-orb group. |

Tick more than one target to treat them as a single layout preset. For example, assign `F6` to a slot with **Side panel**, **Minimap**, and **Status orbs** selected to hide or restore all three together.

Hotkeys follow the current interface state. If you manually open or close Chatbox or Side panel, the next relevant hotkey press restores or hides it as expected. If a normal RuneLite tab hotkey opens a side panel, the linked slot can also be restored automatically through the General option below.

## General settings

### Restore links

When enabled, opening a normal side-panel tab—such as Inventory or Prayers—with its RuneLite hotkey restores every element selected alongside **Side panel** in the relevant slot. This keeps a combined side-panel/minimap/orbs preset from getting out of sync.

### Link minimap

When enabled, the minimap hides whenever the side panel closes, including when you click an already-selected tab such as Prayers or Inventory. It returns when a side-panel tab opens, unless an active hotkey keeps the minimap hidden.

### Link orbs

When enabled, the status-orb group follows the side panel in the same way: it hides when the panel closes and returns when a tab opens, unless an active hotkey keeps it hidden.

## Always hidden settings

The **Always hidden** section is independent of the hotkey slots. Each selected control stays hidden even when a slot restores the wider Status orbs group.

Available persistent controls are:

- Health orb
- Prayer orb
- Run-energy orb
- Special-attack orb
- XP button
- World Map button
- Wiki button

Turn a setting off to restore that control to the visibility it had before Widget Toggle hid it.

## Local development

Run the development client from this project directory:

```powershell
.\gradlew.bat run
```

RuneLite starts in developer mode with Widget Toggle loaded. To use a Jagex Account in the development client, use RuneLite's Jagex Account sidebar panel.

Run the automated build checks with:

```powershell
.\gradlew.bat test
```

## License

This project is licensed under the [BSD 2-Clause License](LICENSE).
