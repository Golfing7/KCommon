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

## Sharing data between servers

A data manager caches what it loads, and that cache only covers its own JVM.
When several servers share one Mongo database they each hand out their own copy
of an object, so a change made on one server is invisible to the others until
they restart.

Turning redis on in KCommon's `config.yml` fixes that:

```yaml
redis:
  enabled: true
  address: '127.0.0.1'
  port: 6379
  username: ''
  password: ''
  database: 0
  max-connections: 16
  timeout: 2000
```

Jedis is downloaded on startup only when this is enabled, so installs that
don't use it are unaffected. Every remote data manager then announces the
objects it writes or deletes on `kcommon:data:<plugin>_<manager>`, and the other
servers drop their cached copy when they hear about it. The next read comes
from Mongo.

An object that is changed locally but not yet saved is left alone, and a warning
is logged instead, because dropping it would throw those changes away.

### Locking

Announcing a change is enough for data that one server owns at a time. Data that
several servers can change at the same moment needs a lock, so nobody reads an
object, decides something, and writes it back on top of a decision another
server already made:

```java
DataManagerRemote<Auction> auctions = (DataManagerRemote<Auction>)
        addDataManager("auctions", Auction.class, true);

auctions.withLock(auctionId, () -> {
    Auction auction = auctions.loadFresh(auctionId);
    if (auction == null || auction.isSold())
        return;

    auction.markSold(buyer);
    auctions.store(auction);
});
```

`loadFresh` inside the lock is the important part. The cached copy is what the
lock is protecting against, so read the object again once the lock is held.

Locks live in redis with a five second expiry, which is what releases them when
a server crashes while holding one. `withLock` throws an `IllegalStateException`
when another server holds the lock for longer than a second. Without redis
there is nobody to lock against and the body simply runs.

`getOrCreate` takes the lock on its own, so two servers asking for the same
missing object at the same time end up with one object instead of two.

The adapter itself is available as `KCommon.getInstance().getRedisAdapter()` for
anything that needs a channel or a lock of its own. It is never null; without
redis it is a no-op adapter that grants every lock and publishes nothing.

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
