# KCommon Drop Tables
The drop tables used in KCommon are my best effort at replicating Hypixel's drop table system for Skyblock.
(If you haven't played Skyblock, don't worry, this guide still explains everything).

## Purpose
The goal of drop tables is to provide a common way of giving drops (or rewards).
These drop tables were designed to allow a single 'thing' to have a single drop table.
(Think of this as one **entity** having one **drop table**). 

## Capabilities
KCommon's drop tables are capable of generating rewards based on:
* Chance
* Min/max overall
* Min/max in a subset

## YAML Definition
```yaml
loot-table:
  [drop-target-range: '2;4'] # This will apply globally to all drops 
  OR
  [groupings: # This can be used to get more specific with drop groupings
    'group-1': # This is an arbitrary name and can be anything EXCEPT '@default'
      drop-target-range: '3;5'
      drops:
        - 'drop-1'
        - 'drop-2'
        - ...
    'group-2':
      drop-target-range: '0;2'
      drops:
        - 'other-drop-1'
        - 'other-drop-2'
        - ...
  ]
  table:
    'drop-1':
      chance: 50 # This is in percent, in this case it's a 50% chance of happening
      display-name: '&aDrop 1!' # This is optional. Some plugins may require it as they'll use it for messages
      [commands:
        - 'cmd-1'
        - 'eco give {PLAYER} 99999']
      OR
      [items:
        'item-1':
          ... item definition ...
        'item-2':
          ... item definition ...]
      OR
      [item:
        ... item definition ...]
```

## Important Notes

### Order of checking
Let's say you have a drop table with three drops and at most it can drop one of them.
i.e.
```yaml
loot-table:
  drop-target-range: '0;1'
  table:
    'diamond':
      chance: 100
      item:
        type: DIAMOND
    'gold_ingot':
      chance: 100
      item:
        type: GOLD_INGOT
    'emerald':
      chance: 100
      item:
        type: EMERALD
```
Since we can at most drop a single one of the rewards and all rewards have a 100% chance of dropping, KCommon randomizes the order drops are tested.

In effect, KCommon will check in a randomized order. In effect, it may check in the order of
* `diamond`
* `emerald`
* `gold_ingot`

or
* `gold_ingot`
* `diamond`
* `emerald`

### Minimum drop targets
With a minimum drop target, you can configure a drop table to always generate the minimum amount of rewards regardless of odds.
Let's say you're working with the following table:
```yaml
loot-table:
  drop-target-range: '3;5' # The maximum target is unimportant here
  table:
    'diamond':
      chance: 50
      item:
        type: DIAMOND
    'gold_ingot':
      chance: 40
      item:
        type: GOLD_INGOT
    'emerald':
      chance: 20
      item:
        type: EMERALD
    'iron_ingot':
      chance: 50
      item:
        type: IRON_INGOT
    'diamond_block':
      chance: 5
      item:
        type: DIAMOND_BLOCK
```
When generating rewards, the drop table will repeatedly test odds on drops until it meets its minimum target.
KCommon will generate a randomized list of all the drops, in this case let's say it was
* `gold_ingot`
* `emerald`
* `iron_ingot`
* `diamond_block`
* `diamond`

It will then go through that list and try to test the chances of all the drops. If it reaches its minimum target before the end of the list, it simply returns the drops that it found first.

In the event the minimum target isn't met, we simply randomize the list again and repeat the search until we meet the requirement.

(Note that as of now if one pass of the list finds and adds the 'diamond' drop, another pass of the list may find the same drop)

### A note on Item Drops
There are some other options that apply specifically to item drops. They are
* `fancy` - Spawns a dropped item using a hologram which also displays the item's display name.
* `player-locked` - Makes it so only the killer (receiver) can pick up the item drop. `fancy` must also be on.
* `looting-enabled` - If players can use looting on the items
* `fortune-enabled` - If players can use fortune on the items

## Example

### Dungeon Mob Loot
```yaml
dungeon-loot-1:
  groupings:
    # This grouping ensures that at LEAST 1 drop is selected and at MOST
    # all three can be dropped.
    normal-loot:
      drop-target-range: '1;3' # From 1-3 drops can be selected
      drops:
        - 'flesh-1'
        - 'flesh-2'
        - 'flesh-3'

    # This grouping prevents both the legendary and legendary runic sword
    # from being dropped at the same time.
    super-loot:
      drop-target-range: '0;1' # From 0-1 drops can be selected
      drops:
        - 'super-drop-1'
        - 'super-drop-2'

  table:
    'flesh-1':
      chance: 50 # 50% chance to happen
      item:
        type: ROTTEN_FLESH
        variable-amount: 1-3 # Selects a random number from 1 to 3
    'flesh-2':
      chance: 25 # 25% chance to happen
      item:
        type: ROTTEN_FLESH
        variable-amount: 5-7
    'flesh-3':
      chance: 5
      item:
        type: ROTTEN_FLESH
        variable-amount: 20-25
        
    'super-drop-1':
      chance: 1 # 1% chance to happen
      item:
        type: DIAMOND_SWORD
        amount: 1
        name: '&cDungeon Sword'
        lore:
          - '&7&oThis legendary sword was passed down for generations!'
        enchantments:
          SHARPNESS: 6
          UNBREAKING: 3
    'super-drop-2':
      chance: 0.1
      item:
        type: DIAMOND_SWORD
        amount: 1
        name: '&5&kI &cRunic Dungeon Sword &5&kI'
        lore:
          - '&7&oOnce a legendary sword, this sword has been upgraded due to its runic status!'
        enchantments:
          SHARPNESS: 7
          UNBREAKING: 5
```
