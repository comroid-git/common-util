## Gradle
```groovy
    repositories {
        maven {
            url "https://maven.jetbrains.space/comroid/repo"
            credentials.username "Anonymous.User"
            credentials.password "anonymous"
        }
    }

    dependencies {
        implementation 'org.comroid:common-util-spellbind:0.9.0'
    }
```