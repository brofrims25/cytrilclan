# CytrilClan

A production-ready clan plugin for Paper/Spigot **1.21.x**, built for servers
running both Java and Bedrock players (via Geyser/Floodgate).

## Features

- **Clan lifecycle** - create, invite, join, leave, kick, disband
- **Bases** - up to 3 per clan, warmup teleport (cancels on movement), rename
- **Bank GUI** - free-form deposit area, paginated withdraw with a per-item
  confirm screen, and a paginated IN/OUT transaction history
- **Settings GUI** - banner editing (hold a banner, click to apply), clan
  rename via chat capture, base rename via chat capture
- **Member management** - stats (kills/deaths), kick (instant or with a
  written-book reason via shift-click), promote/demote, and a **1-hour
  delayed leader transfer** that can be cancelled by clicking again before
  it completes
- **Give-item GUI** - place an item, then pick an online clan member to
  receive it
- **Integrations**
  - **LuckPerms** - clan creation permission (`cytrilclan.create`) is checked
    through the native LuckPerms API (not `Player#hasPermission`) for
    accurate results with negated/contextual nodes, falling back to Bukkit's
    check if LuckPerms isn't installed
  - **PlaceholderAPI** - `%cytrilclan_name%`, `%cytrilclan_tag%`,
    `%cytrilclan_role%`, `%cytrilclan_members%`
  - **Floodgate** - soft-depend, `FloodgateHook#isBedrockPlayer` available for
    future Bedrock-specific UX tweaks
- **Storage** - one flat YAML file per clan under
  `plugins/CytrilClan/clans/<name>.yml`; `ClanManager` keeps everything in
  memory for fast lookups and only touches disk on save

## GUI navigation convention

Every CytrilClan screen with a control row follows the same layout so it
feels consistent:

| Position                  | Action        |
|----------------------------|---------------|
| Bottom-left of control row  | Back          |
| Bottom-center of control row| Close menu    |
| Bottom-right of control row | Next page     |

(The deposit and give-item screens deviate slightly because they need a
"Confirm" action that doesn't fit that convention - this is called out in
the code comments in `BankDepositGui` and `GiveItemGui`.)

## Commands

```
/clan                          - open the clan menu
/clan create <name> <tag>      - create a clan
/clan invite <player>          - invite a player (officer/leader only)
/clan join <name>               - accept a pending invite
/clan leave                     - leave your clan
/clan kick <player>              - kick a member
/clan disband                   - disband your clan (leader only)
/clan base [set <name>]         - open the base menu, or claim a new base
/clan bank                      - open the clan bank
/clan settings                  - open clan settings
/clan info [name]                - view clan info
/clan list                      - list all clans
/clan promote|demote <player>    - change a member's rank
/clan give                      - give an item to a member
/clan help                      - list commands
```

Aliases: `/c`, `/cclan`.

## Permissions

| Node                  | Default | Purpose                          |
|-----------------------|---------|-----------------------------------|
| `cytrilclan.create`   | op      | Create a clan                     |
| `cytrilclan.admin`    | op      | Admin override                    |
| `cytrilclan.use`      | true    | Use basic clan commands           |

## Known limitations (by design, documented honestly)

- **Kick-reason book flow**: Minecraft only fires `PlayerEditBookEvent` when
  a *writable* book is signed or its pages are saved on inventory close -
  there's no event for "player is currently typing." This means the kick
  doesn't finalize until the kicker closes or signs the book they were
  handed. If they log out or drop the book without closing it properly, the
  pending kick action is cleared silently on quit (see
  `PlayerConnectionListener`) rather than left dangling.
- **Base rename navigation**: the "Back" button from the base list reopens
  Settings when you arrived via Settings, and the Main Menu otherwise, based
  on a context flag on the inventory holder - not a full navigation stack.
- **Bank storage size**: the shared bank uses a fixed 54-slot backing array
  regardless of `general.bank-rows` in `config.yml`; that setting is reserved
  for a future GUI-size option and isn't wired up to the storage size yet.

## Building

Maven dependencies (Paper API, LuckPerms API, PlaceholderAPI, Floodgate API)
need network access that this environment doesn't have, so the intended build
path is:

1. Push this repository to GitHub.
2. The included GitHub Actions workflow (`.github/workflows/build.yml`) runs
   `mvn clean package` on every push and uploads the compiled jar as a
   downloadable build artifact named **CytrilClan-jar**.
3. Download the artifact from the Actions run summary, then drop the jar into
   your server's `plugins/` folder.

To build locally instead (if you have internet access):

```bash
mvn clean package
```

The shaded jar will be at `target/CytrilClan-1.0.0.jar`.

## Project layout

```
com/cytril/cytrilclan/
  CytrilClan.java            - main plugin class
  model/                      - Clan, ClanMember, ClanRole, ClanBase, BankTransaction
  manager/                    - ClanManager, StorageManager, ConfigManager, PendingActionManager
  commands/                   - ClanCommand
  gui/                        - all GUI screens + ClanGuiHolder/GuiUtil
  listeners/                  - ClanGuiListener, ChatInputListener, BookEditListener, PlayerConnectionListener
  tasks/                      - LeaderTransferTask, TeleportWarmupTask
  integration/                - LuckPermsHook, FloodgateHook, CytrilClanPlaceholders
  util/                       - MessageUtil, ItemBuilder, SoundUtil, ProfanityFilter
```
