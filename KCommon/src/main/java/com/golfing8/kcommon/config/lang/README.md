# Message Configuration

Generally, all messages that are sent from a KCommon plugin are sent using a [Message](Message.java) instance.

These messages are meant to be heavily configurable and comprehensive in how messages can be sent.

## Configuration

Messages can range from the very simple

```yaml
some-message: 'Hello World!'
```

to the very complex

```yaml
some-message:
  message:
    - 'Hello from line one!'
    - 'Hi from line two!'
  actionbar: "I'm over here too!"
  title:
    title: '&cWelcome'
    subtitle: '&7To this subtitle!'
    # All of these measurements are in ticks.
    in: 20
    stay: 40
    out: 40
  sounds:
    '0':
      # These sounds use the XSound enum.
      sound: 'ENTITY_PLAYER_BURP'
      pitch: 1.0
      volume: 1.0
      # A delay in ticks to wait to send the sound.
      delay: 10
```

You can even set a message to be paged if you think it's too long!

```yaml
some-message:
  # Assume the multiline placeholder here gives back 50 lines.
  message:
    - '%SOME_MULTILINE_PLACEHOLDER%'
  paged: true
  # The rest of these options are optional and can be left out.
  page-height: 10
  page-header: '&6&m----------&r &a{PREVIOUS} &ePage &e{PAGE}&7/&e{MAX_PAGE} &a{NEXT} &6&m----------'
  page-footer: '&6&m----------------------------------------------------'
```
