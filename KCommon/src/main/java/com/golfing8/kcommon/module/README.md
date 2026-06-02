# Modules
The module is one of the fundamental features of KCommon. 
Each module maintains its own state and can be thought of as a singular feature, similar to a plugin. Modules were designed with the express purpose of eliminating common boilerplate in plugins. 

## Reloadability
The module system was built with the idea of forced reloadability. 
While developing (and working with) plugins, I found that most plugins were **not** reloadable and thus required things like PlugMan to force a reload.
With KCommon, all modules have reloadability baked in by using the `/kmodules reload <module>`

## Togglability
Every module can be toggled on or off using the `/kmodules enable <module>` and `/kmodules disable <module>` commands.
When turned off, a module *should* be made completely silent, consuming virtually zero resources of the server.

## Configurability
A fundamental flaw in many public plugins is lack of configurability. 
For a beginner server administrator or someone with no experience in Java, editing plugins can be next to impossible.
KCommon provides a versatile framework for configurability that allows for developers to skip the monotonous process of making a feature configurable.
