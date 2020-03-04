[![Gitpod Ready-to-Code](https://img.shields.io/badge/Gitpod-Ready--to--Code-blue?logo=gitpod)](https://gitpod.io/#https://github.com/comroid-git/common-util) 

# comroid polyfills

### spellbind (Gradle)
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
        implementation 'org.comroid:common-util-spellbind:0.10.0'
    }

// using mavenCentral repository
	repositories.jcenter()

	dependencies {
    	implementation 'org.comroid:common-util-spellbind:0.10.0'
	}
```

### trie (Gradle)
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
        implementation 'org.comroid:common-util-trie:1.0.0'
    }

// using mavenCentral repository
	repositories.jcenter()

	dependencies {
    	implementation 'org.comroid:common-util-trie:1.0.0'
	}
```
