android {
    ...
    signingConfigs {
        create("release") {
            val isRunningOnCI = System.getenv("CI") == "true"

            if (isRunningOnCI) {
                // Если сборка из CI, тополучаем значения из переменных окружения
                storeFile = file("../keystore.jks")
                storePassword = System.getenv("keystore_password")
                keyAlias = System.getenv("keystore_alias")
                keyPassword = System.getenv("keystore_alias_password")
            } else {
                // Иначе считываем значения из файла
                val secretProperties = Properties()
                val fis = FileInputStream(project.file("secret.properties"));
                secretProperties.load(fis)
                keyAlias = secretProperties.getProperty("keyAlias")
                        ?: error("'keyAlias' should be specified")
                keyPassword = secretProperties.getProperty("keyPassword")
                        ?: error("'keyPassword' should be specified")
                storeFile = file(secretProperties.getProperty("storeFile")
                        ?: error("'storeFile' should be specified"))
                storePassword = secretProperties.getProperty("storePassword")
                        ?: error("'storePassword' should be specified")
            }
        }
    }
    ...
}
