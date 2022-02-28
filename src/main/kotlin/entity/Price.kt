package entity

sealed interface Price {
    abstract val amount: Double
    abstract val currency: Currency

    fun convertTo(newCurrency: Currency): Price {
        val newAmount = amount * currency.getToRubsExchangeRate() / newCurrency.getToRubsExchangeRate()
        return when (newCurrency) {
            Currency.RUB -> RubPrice(newAmount)
            Currency.USD -> UsdPrice(newAmount)
            Currency.EUR -> EurPrice(newAmount)
        }
    }
}

data class RubPrice(override val amount: Double) : Price {
    override val currency = Currency.RUB
}

data class UsdPrice(override val amount: Double) : Price {
    override val currency = Currency.USD
}

data class EurPrice(override val amount: Double) : Price {
    override val currency = Currency.EUR
}

enum class Currency {
    RUB,
    USD,
    EUR;

    fun getToRubsExchangeRate() = when (this) {
        RUB -> 1.0
        USD -> 83.5
        EUR -> 93.6
    }
}