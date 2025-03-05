package mobi.sevenwinds.common

import com.typesafe.config.ConfigFactory
import io.ktor.config.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.testing.*
import io.ktor.util.*
import io.restassured.RestAssured
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import java.util.concurrent.TimeUnit

open class ServerTest {
    companion object {
        @Container
        private val postgresContainer = PostgreSQLContainer<Nothing>("postgres:10.23-alpine").apply {
            withDatabaseName("test_db")
            withUsername("test")
            withPassword("test")
        }

        private var serverStarted = false

        private lateinit var server: ApplicationEngine

        @Suppress("unused")
        @KtorExperimentalAPI
        @ExperimentalCoroutinesApi
        @BeforeAll
        @JvmStatic
        fun startServer() {
            withTestApplication {
                startTestContainers()

                if (!serverStarted) {
                    server = embeddedServer(Netty, environment = applicationEngineEnvironment {
                        config = HoconApplicationConfig(ConfigFactory.load("test.conf"))

                        connector {
                            port = config.property("ktor.deployment.port").getString().toInt()
                            host = "127.0.0.1"
                        }
                    })
                    server.start()
                    serverStarted = true

                    RestAssured.baseURI = "http://localhost"
                    RestAssured.port = server.environment.config.property("ktor.deployment.port").getString().toInt()
                    RestAssured.enableLoggingOfRequestAndResponseIfValidationFails()

                    Runtime.getRuntime().addShutdownHook(Thread { server.stop(0, 0, TimeUnit.SECONDS) })
                }
            }
        }

        @AfterAll
        @JvmStatic
        fun teardown() {
            postgresContainer.stop()
        }

        private fun startTestContainers() {
            postgresContainer.start()
            System.setProperty("DATABASE_URL", postgresContainer.jdbcUrl)
            System.setProperty("DATABASE_USER", postgresContainer.username)
            System.setProperty("DATABASE_PASSWORD", postgresContainer.password)
        }
    }
}