class CalculatorTest {
    private calculator: Calculator

    @Before
    fun setUp() {
        this.calculator = Calculator()
    }

    @Test
    fun testSum_whenPassedTwoNumbers_shouldReturnResultOfSum() {
        val result = calculator.sum(200, 10)
        assertEquals(210, result)
    }

    @Test
    fun testSubtract_whenPassedTwoNumbers_shouldReturnResultOfSubtract() {
        val result = calculator.subtract(200, 10)
        assertEquals(180, result)
    }
}
