package dev.vjcbs.bunq2ynab.rest

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import dev.vjcbs.bunq2ynab.client.BunqClient
import dev.vjcbs.bunq2ynab.client.YnabClient
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.application.log
import io.ktor.features.Compression
import io.ktor.features.ContentNegotiation
import io.ktor.features.StatusPages
import io.ktor.http.HttpStatusCode
import io.ktor.jackson.jackson
import io.ktor.response.respond
import io.ktor.routing.get
import io.ktor.routing.routing
import java.text.DateFormat

fun Application.main() {
    install(Compression)
    install(ContentNegotiation) {
        jackson {
            dateFormat = DateFormat.getDateInstance()
            registerModule(JavaTimeModule())
        }
    }
    install(StatusPages) {
        exception<Throwable> { cause ->
            call.respond(HttpStatusCode.InternalServerError, cause.message ?: "Error")
            throw cause
        }
    }

    val ynabClient = YnabClient()
    val bunqClient = BunqClient()

    routing {
        get("/callback") {
            val importResult = ynabClient.createTransactions(bunqClient.getOutgoingTransactionsForAllBankAccounts())

            log.info("${importResult.transactionIds.size} imported, ${importResult.duplicateImportIds.size} duplicates")

            call.respond(importResult)
        }
    }
}