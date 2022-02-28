import org.junit.jupiter.api.Test
import java.net.HttpURLConnection
import java.net.URL
import kotlin.test.assertEquals

class ServerTest {
    private fun request(method: String, params: String): String {
        with(URL("http://localhost:8080/$method?$params").openConnection() as HttpURLConnection) {
            inputStream.bufferedReader().use {
                return it.readText()
            }
        }
    }

    private fun createUser(id: String, currency: String) = request("create_user", "id=$id&currency=$currency")
    private fun createItem(id: String, priceInRub: String) = request("create_item", "id=$id&priceInRub=$priceInRub")
    private fun listItems(userId: String) = request("list_items", "userId=$userId")

    @Test
    fun test() {
        Server.start(awaitShutdown = false)

        assertEquals("No such method non-existent-method", request("non-existent-method", ""))
        assertEquals("User AlexR created with RUB", createUser("AlexR", "RUB"))
        assertEquals("", listItems("AlexR"))
        assertEquals("User non-existent-user not found", listItems("non-existent-user"))
        assertEquals("Item car created with 12345.0 RUB", createItem("car", "12345"))
        assertEquals("car | 12345.0 RUB", listItems("AlexR"))
        assertEquals("Item milk created with 100.0 RUB", createItem("milk", "100"))
        assertEquals("milk | 100.0 RUB\ncar | 12345.0 RUB", listItems("AlexR"))
        assertEquals(
            "E11000 duplicate key error collection: rxtest.users index: _id_ dup key: { : \"AlexR\" }",
            createUser("AlexR", "RUB")
        )
        assertEquals(
            "E11000 duplicate key error collection: rxtest.users index: _id_ dup key: { : \"AlexR\" }",
            createUser("AlexR", "USD")
        )
        assertEquals("User BobU created with USD", createUser("BobU", "USD"))
        assertEquals("milk | 1.1976047904191616 USD\ncar | 147.8443113772455 USD", listItems("BobU"))
    }
}

/*
car | 1234.0 RUB
milk | 100.0 RUB
 */