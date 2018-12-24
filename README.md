Gradle Setup
=============
1.In your root build.gradle:
  
	allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
  
2.Add the dependency

    dependencies {
	        implementation 'com.github.guo-hang:Banner:0.0.1'
    }
  
