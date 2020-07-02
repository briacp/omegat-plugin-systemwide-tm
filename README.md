# OmegaT Plugin - System-wide TM

## Description

This Proof-of-concept plugin adds a system-wide TM by using symlinks:

* On project load, it links the `$OMT_CONFIG/tm/{source}-{language}`directory to `$PROJECT_TM_DIR/system`. If there's already a TMX corresponding to this project, it renames the file to `$project-level1.tmx.ignore`.

* On project compile, it copies the generated `level1.tmx` to the `$OMT_CONFIG/tm/{source}-{language}` directory.

* On project close, it renames the file to `$project-level1.tmx.ignore` to `$project-level1.tmx`.


See [https://sourceforge.net/p/omegat/feature-requests/1497/](https://sourceforge.net/p/omegat/feature-requests/1497/)


## Installation

You can download the plugin jar file from the [release page](../../releases). The OmegaT plugin jar should be placed in `$HOME/.omegat/plugins` (Linux), `~/Library/Preferences/OmegaT/plugins/` (macOS), or `C:\Program Files\OmegaT\plugins` (Windows) depending on your operating system.

## Symlinks in Windows 

Symlinks are supported in recent Windows systems, as long as the drive is NTFS.

```
  > secpol.msc 
    -> Local Policies -> User Rights Assignment -> Create symbolic links
    -> Add current user
  > gpupdate /force
  > Log off from the current session
```

## License

This project is distributed under the GNU general public license version 3 or later.

