object CalculatorSpec : Spek({
    given("a calculator") {
        val calculator = Calculator()

        on("addition") {
            val actual = calculator.sum(200, 10)
            it("should return the result of adding the first number to the second number") {
                assertEquals(210, actual)
            }
        }

        on("subtraction") {
            val actual = calculator.subtract(200, 10)
            it("should return the result of subtracting the second number from the first number") {
                assertEquals(180, actual)
            }
        }
    }
})
