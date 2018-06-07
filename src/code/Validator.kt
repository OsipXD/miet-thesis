package ru.endlesscode.miet.orioks.util

import javax.inject.Inject


class Validator @Inject constructor() {

    companion object {
        const val NO_ERROR = 0
        const val TOO_SHORT = 1
        const val HAVE_NO_LETTER = 2
        const val HAVE_NO_DIGIT = 3
        const val ILLEGAL_CHARACTERS = 4
        const val ENDS_WITH_WRONG = 5
        const val STARTS_WITH_WRONG = 6
        const val BLANK = 7
        const val WRONG_LENGTH = 8
        const val TOO_LONG = 9
    }

    fun validate(string: CharSequence, rules: ValidatorContext.() -> Unit): Int {
        return ValidatorContext().apply(rules).validate(string)
    }


    class ValidatorContext internal constructor() {

        private val rules = mutableSetOf<ValidationRule>()

        fun minimalLength(length: Int) {
            rule(TOO_SHORT) { it.length >= length }
        }

        fun lengthExact(length: Int) {
            rule(WRONG_LENGTH) { it.length == length }
        }

        fun maximalLength(length: Int) {
            rule(TOO_LONG) { it.length <= length }
        }

        fun atLeastOneLetter() {
            rule(HAVE_NO_LETTER) { it matches ".*[A-Za-z]+.*".toRegex() }
        }

        fun atLeastOneDigit() {
            rule(HAVE_NO_DIGIT) { it matches ".*[0-9]+.*".toRegex() }
        }

        fun legalCharacters(digits: Boolean = true, letters: Boolean = true, additional: String = "") {
            val builder = StringBuilder("^[")
            if (letters) builder.append("A-Za-z")
            if (digits) builder.append("0-9")
            builder.append(additional).append("]*$")

            rule(ILLEGAL_CHARACTERS) { it matches builder.toString().toRegex() }
        }

        fun notStartsWith(substring: String) {
            rule(STARTS_WITH_WRONG) { !it.startsWith(substring) }
        }

        fun notEndsWith(substring: String) {
            rule(ENDS_WITH_WRONG) { !it.endsWith(substring) }
        }

        fun notBlank() {
            rule(BLANK) { it.isNotBlank() }
        }

        internal fun validate(string: CharSequence): Int {
            return rules.firstOrNull { !it.validate(string) }?.code ?: NO_ERROR
        }

        private fun rule(errorCode: Int, validation: (CharSequence) -> Boolean) {
            rules.add(ValidationRule(errorCode, validation))
        }
    }
}


internal class ValidationRule(
        val code: Int,
        val validate: (CharSequence) -> Boolean
)
