button.setOnClickListener {
    // Получаем данные пользователя из API (долгая операция)
    val rawData = getUserData("test")
    // Десериализуем пользователя (долгая операция)
    val user = deserializeUser(rawData)
    showUserData(user)
}

// 1. Добавим функции обратного вызова в функции getUserData и deserializeUser
button.setOnClickListener {
    getUserData("test") { rawData ->
        deserializeUser(rawData) { user ->
            showUserData(user)
        }
    }
}

// 2. Используем RxJava
button.setOnClickListener {
    getUserData("test")
            .flatMap { rawData ->
                deserializeUser(rawData)
            }
            .subscribe { user ->
                showUserData(user)
            }
}

// 3. Используем корутины
button.setOnClickListener {
    launch(UI) {
        val rawData = getUserData("test")
        val user = deserializeUser(rawData)
        showUserData(user)
    }
}
