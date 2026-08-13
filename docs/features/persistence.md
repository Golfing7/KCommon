# Persistence

Use `DataManagerContainer` for data that must survive a server restart.
`Module` shuts down its registered data managers as part of the disable
lifecycle.

## Define persisted data

Use `AbstractSerializable` for keyed global or feature data. Use
`SenderSerializable` when each record belongs to a player and should be keyed
by that player's UUID.

```java
public final class PlayerProfile extends SenderSerializable {
    private int greetings;

    public int getGreetings() {
        return greetings;
    }

    public void incrementGreetings() {
        greetings++;
        change();
    }
}
```

Persisted classes need a no-argument constructor. Keep runtime-only fields
`transient`, and use `change()` after mutating an object so an auto-save data
manager knows that it needs to be written.

## Register and load data

Make the module a `DataManagerContainer` and register the data class before
loading it:

```java
public final class GreetingsModule extends Module implements DataManagerContainer {
    @Override
    public void onEnable() {
        DataManager<PlayerProfile> profiles =
                addDataManager("player-profiles", PlayerProfile.class);
        profiles.setStrictSaving(true);
    }

    public PlayerProfile profile(UUID playerId) {
        return getOrCreate(playerId, PlayerProfile.class);
    }

    @Override
    public void onDisable() {
        // KCommon shuts down registered data managers after this method.
    }
}
```

The manager uses a local data store by default. Changed cached objects are
written on the normal save cycle, and module shutdown saves the remaining
cached objects. `setStrictSaving(true)` makes shutdown write only objects
marked with `change()`, which is useful for larger data sets.

Pass `true` as the third argument to `addDataManager` to request the remote
Mongo-backed manager:

```java
addDataManager("player-profiles", PlayerProfile.class, true);
```

Only use the remote manager when MongoDB is configured and available in the
runtime environment.

## Data operations

`DataManagerContainer` provides type-safe helpers:

```java
PlayerProfile profile = getOrCreate(player.getUniqueId(), PlayerProfile.class);
PlayerProfile loaded = loadData(player.getUniqueId(), PlayerProfile.class);
boolean exists = dataExists(player.getUniqueId(), PlayerProfile.class);
saveData(profile);
deleteData(profile);
```

Use `saveData` when an immediate write is required. Otherwise, mark changed
objects and let the registered manager handle its normal persistence cycle.

When a record is a snapshot of transient module state, capture the final state
and call `saveData` from `onDisable()` before KCommon shuts down the data
manager:

```java
@Override
public void onDisable() {
    runtimeData.capture(activeState);
    saveData(runtimeData);
}
```

If a persisted object owns runtime resources, reattach them after loading:

```java
for (GreetingProfile profile : getAllDataOfType(GreetingProfile.class)) {
    profile.bind(this);
}
```

Use `SenderSerializable` when the key is always a player UUID. If the same
record can belong to a player, island, block, or another scope, use
`AbstractSerializable` and assign the appropriate stable key yourself.
