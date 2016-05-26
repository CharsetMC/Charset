This is the GitHub repository for Charset.

# Using Charset in Gradle projects

To use Charset in your Gradle project, add the following lines to build.gradle:

    repositories {
        maven {
            name = "Charset Maven"
            url = "http://charset.asie.pl/maven"
        }
    }
    
    dependencies {
        deobfCompile "pl.asie.charset:charset-api:0.2.6"
    }

Feel free to replace 0.2.6 with the Charset version you want to use and "api" with the module you want to include. To include multiple modules, just 
copy the "deobfCompile" line multiple times to match.
