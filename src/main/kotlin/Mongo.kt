import com.mongodb.rx.client.MongoClients
import entity.Currency
import entity.Item
import entity.User

object Mongo {
    private val database = MongoClients.create("mongodb://localhost:27017").getDatabase("rxtest")

    private val users get() = database.getCollection("users")
    private val items get() = database.getCollection("items")

    init {
        users.drop().toBlocking().single()
        items.drop().toBlocking().single()
    }

    fun getUser(id: String) =
        users.find().toObservable()
            .map(::User)
            .filter { it.id == id }
            .single()

    fun createUser(user: User) = users.insertOne(user.toDocument())

    fun getAllItems(currency: Currency) =
        items.find().toObservable()
            .map(::Item)
            .map {
                Item(it.id, it.price.convertTo(currency))
            }
            .sorted { a, b -> a.price.amount.compareTo(b.price.amount) }

    fun createItem(item: Item) = items.insertOne(item.toDocument())
}
