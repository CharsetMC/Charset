This is the GitHub repository for Charset.

# Using Charset in Gradle projects

To use Charset in your Gradle project, add the following lines to build.gradle:

    repositories {
        maven {
            name = "Elytra Maven"
            url = "http://repo.elytradev.com"
        }
    }
    
    dependencies {
        deobfCompile "pl.asie.charset:Charset:0.4.2.1"
    }

Feel free to replace 0.4.2.1 with the Charset version you want to use and "api" with the module you want to include. To include multiple modules, just 
copy the "deobfCompile" line multiple times to match.
