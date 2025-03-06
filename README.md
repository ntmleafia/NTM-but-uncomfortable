# <u>**PAIN**</u>

The build is messed up as fk,
I got it working on IntelliJ:

1. Make sure you use Java 8 + Gradle 7
2. Put computertronics (deobf dev jar) in the folder the project is in (project parent folder) [download](https://wiki.vexatos.com/wiki:computronics)
3. Load the project

### If you encounter LeafiaGls.java:19: error: cannot find symbol:
````java
LeafiaGls.java:19: error: cannot find symbol
public class LeafiaGls extends GlStateManager {
       ^
  symbol:   constructor GlStateManager()
  location: class GlStateManager
````
do this:
1. run `gradle clean`
2. run `gradle clean --refresh-dependencies`
3. goto `com.leafia.transformer.LeafiaGls` and click `Download Sources` this magically fixes the error idk some kind of environment issue, download source runs a lot of tasks, mappings and setup MCP env

### Also access transformers are broken and wont apply automatically
thats why i just bundled the at.cfg's of mods you may want to use for testing (JEI and OpenComputers)
````gradle
accessTransformers(
                    file("${projectDir}/Assets/jei_at.cfg"),
                    file("${projectDir}/Assets/oc_at.cfg"))
````

### Help minecraft screen is black on startup
- Somehow the OpenGL context is messed up sometimes, (could be window mode related)

If you are in windowed mode:
Just resize the window, for example press the maximize button

I don't know any fix for fullscreen mode or if that event happens in fullscreen mode

# <u>**pain**</u>

Here's version of NTM that i decided to modify from Extended Edition 2.0.1, since the mod suddenly stopped receiving updates<br>
This is more of a local project not focused on production, and also I have no experience in Java coding, so it's not recommended for a wide use lol

It is forked from Extended Edition made by [Alcater](https://github.com/Alcatergit/Hbm-s-Nuclear-Tech-GIT)<br>
which is forked from the fork made by [TheOriginalGolem](https://github.com/TheOriginalGolem/Hbm-s-Nuclear-Tech-GIT)<br>
which is forked from the port made by [Drillgon200](https://github.com/Drillgon200/Hbm-s-Nuclear-Tech-GIT)<br>
which is ported from the original mod made by [HBMTheBobcat](https://github.com/HbmMods/Hbm-s-Nuclear-Tech-GIT).

Details are on their README's. What I want to say here is that GitHub is way, WAY too confusing to me I have no idea how to mark this repository as a proper fork of Extended Edition. Sigh.

(update: internet say it's impossible to mark as a fork retroactively. ntm hamster reloaded also lacks the "forked from" thingy so im happy im not the only one lol)