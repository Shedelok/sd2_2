import entity.Currency
import entity.Item
import entity.RubPrice
import entity.User
import io.netty.buffer.ByteBuf
import io.reactivex.netty.protocol.http.server.HttpServer
import io.reactivex.netty.protocol.http.server.HttpServerRequest
import rx.Observable
import rx.observables.StringObservable

object Server {
    fun start(awaitShutdown: Boolean = true) {
        val server = HttpServer
            .newServer(8080)
            .start { req, resp ->
                resp.writeString(processRequest(req))
            }

        if (awaitShutdown) {
            server.awaitShutdown()
        }
    }

    private fun processRequest(req: HttpServerRequest<ByteBuf>): Observable<String> {
        return when (val method = req.decodedPath.substring(1)) {
            "create_user" -> {
                val id = req.queryParameters["id"]?.get(0)
                val currency = req.queryParameters["currency"]?.get(0)

                if (id != null && Currency.values().any { it.name == currency }) {
                    Mongo
                        .createUser(User(id, Currency.valueOf(currency!!)))
                        .map { "User $id created with $currency" }
                        .onErrorReturn { it.message }
                } else {
                    Observable.just("Expected id and currency")
                }
            }
            "create_item" -> {
                val id = req.queryParameters["id"]?.get(0)
                val priceInRub = req.queryParameters["priceInRub"]?.get(0)?.toDoubleOrNull()

                if (id != null && priceInRub != null) {
                    Mongo
                        .createItem(Item(id, RubPrice(priceInRub)))
                        .map { "Item $id created with $priceInRub RUB" }
                        .onErrorReturn { it.message }
                } else {
                    Observable.just("Expected id and priceInRub")
                }
            }
            "list_items" -> {
                val userId = req.queryParameters["userId"]?.get(0)

                if (userId != null) {
                    val itemsStrings = Mongo.getUser(userId)
                        .flatMap {
                            Mongo.getAllItems(it.currency)
                        }
                        .map { "${it.id} | ${it.price.amount} ${it.price.currency.name}" }
                        .onErrorReturn {
                            if (it is NoSuchElementException) {
                                "User $userId not found"
                            } else {
                                it.message
                            }
                        }

                    StringObservable.join(itemsStrings, System.lineSeparator())
                } else {
                    Observable.just("Expected userId")
                }
            }
            else -> {
                Observable.just("No such method $method")
            }
        }
    }
}