package entity

import org.bson.Document

private const val ID_KEY = "_id"
private const val CURRENCY_KEY = "currency"

data class User(val id: String, val currency: Currency) {
    constructor(d: Document) : this(d.getString(ID_KEY), Currency.valueOf(d.getString(CURRENCY_KEY)))

    fun toDocument() = Document(
        mapOf(
            ID_KEY to id,
            CURRENCY_KEY to currency.toString()
        )
    )
}