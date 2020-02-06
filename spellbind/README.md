## Gradle
```groovy
// using JetBrains Space repository
    repositories {
        maven {
            url "https://maven.jetbrains.space/comroid/repo"
            credentials.username "Anonymous.User"
            credentials.password "anonymous"
        }
    }

    dependencies {
        implementation 'org.comroid:common-util-spellbind:0.9.2'
    }

// using mavenCentral repository
	repositories.jcenter()

	dependencies {
    	implementation 'org.comroid:common-util-spellbind:0.9.2'
	}
```
