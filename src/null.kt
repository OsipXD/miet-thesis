// Переменным, которые могут принимать значение null после типа добавляется
// вопросительный знак
class Foo(val bar: String?)

// Допустим, что метод getFoo может вернуть объект Foo или null
val buz: Foo? = getFoo()

// Если хотя бы один объект в цепочке будет null, то длина будет равняться нулю
val length = buz?.bar?.length ?: 0
// Кроме того, тип переменной length будет автоматически выведен как Int
