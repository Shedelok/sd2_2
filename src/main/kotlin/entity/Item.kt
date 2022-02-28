package entity

import org.bson.Document

private const val ID_KEY = "_id"
private const val PRICE_AMMOUNT_KEY = "price_amount"
private const val PRICE_CURRENCY_KEY = "price_currency"

data class Item(val id: String, val price: Price) {
    constructor(d: Document) : this(d.getString(ID_KEY), RubPrice(d.getDouble(PRICE_AMMOUNT_KEY))) {
        check(d.getString(PRICE_CURRENCY_KEY) == Currency.RUB.name)
    }

    fun toDocument() = Document(
        mapOf(
            ID_KEY to id,
            PRICE_AMMOUNT_KEY to price.amount,
            PRICE_CURRENCY_KEY to price.currency.name
        )
    )
}