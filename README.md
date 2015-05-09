Idea Remote Pair Plugin
=======================

An IDEA plugin for remote pair progamming. In early development now.

Setup the project
-----------------

We use Scala to write the code, so we use SBT to manage the project. 

We provide an SBT task to convert a normal scala project into an IDEA plugin project, so we can use SBT to manage this project now.

### Setup the project as normal SBT project

1. clone the code
2. Use IDEA importing it as a normal SBT project. I prefer to select the checkboxes which makes development easier: 
    1. use auto-import
    2. Download sources and docs
    3. Download SBT sources and docs
3. Choose a proper IDEA SDK (e.g. `IDEA IC-???`) as project SDK
4. Wait IDEA to download all dependencies and configure them properly, as a normal SBT project
5. IDEA -> Build -> Rebuild project, make sure it's successful

### Convert it to plugin project

1. cd to the project dir
2. `./sbt convertToPluginProject`

It will show:

```
Success! Please restart your IDEA to apply the change!
Success! Please restart your IDEA to apply the change!
[success] Total time: 0 s, completed May 9, 2015 11:34:13 AM
```

The task does 2 things:

1. Convert the project into an IDEA plugin plugin by modifing `.idea/modules/idea-remote-pair.iml`
2. Add a configuration for running this plugin by modifing the `.idea/workspace.xml`

The task does the convertion only when necessary. If you run it again, it may print:

```
Already applied, skip
Already applied, skip
```

### Reload the project in IDEA

Switch to IDEA, it will prompt you something is changed, and asks you to reload the project by click `Yes` button.

If not, you can restart IDEA to reload it.

If everything is well, you can see there is an runnable `idea-remote-pair` on the top right. And if you click on it, a new IDEA editor applies current plugin will show.




