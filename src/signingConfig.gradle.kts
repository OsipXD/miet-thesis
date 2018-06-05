android {
    ...
    signingConfigs {
        create("release") {
            keyAlias = "keyAlias"
            keyPassword = "keyPassword"
            storeFile = "storeFile"
            storePassword = "storePassword"
        }
    }

    buildTypes {
        getByName("release") {
            signingConfig = signingConfigs.getByName("release")
        }
    }
}
