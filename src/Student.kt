data class Student(
        val id: String,
        var fullName: String,
        var group: String,
        var debts: List<Subject> = arrayListOf()
)
