This is the GitHub repository for Charset.

# Support

Support for the mod - that is, bug report handling - is provided on the [issue tracker](https://github.com/CharsetMC/Charset/issues).

For feature requests and discussion, I highly encourage you to visit #charset on the EsperNet IRC network. ([Webchat](http://webchat.esper.net/?channels=#charset))

# Using Charset in Gradle projects

To use Charset in your Gradle project, add the following lines to build.gradle:

    repositories {
        maven {
            name = "Elytra Maven"
            url = "http://repo.elytradev.com"
        }
    }
    
    dependencies {
        deobfCompile "pl.asie.charset:charset:0.5.0.119"
    }

Feel free to replace 0.5.0.119 with the Charset version you want to use. You can also use ":api". To include multiple 
modules, just copy the "deobfCompile" line multiple times to match.
