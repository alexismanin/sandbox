package stackoverflow;

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.selects.select
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.`java-time`.date
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.*

object TestTable : Table("test_table") {
    val day = varchar("day", 10)
    val date = date("dateColumn")
}

/*
 * Try to reproduce https://stackoverflow.com/questions/64137800/kotlin-and-exposed-orm
 */
fun main() {
    HikariDataSource(HikariConfig().apply { jdbcUrl = "jdbc:h2:mem:test" }).use {datasource ->
        val db = Database.connect(datasource)

        // Create records
        transaction(db) {

            addLogger(StdOutSqlLogger)

            SchemaUtils.create(TestTable)
            sequence<LocalDate> {
                yield(LocalDate.of(2020, 11, 19))
                yield(LocalDate.of(2020, 11, 20))
                yield(LocalDate.of(2020, 11, 21))
            }.forEach { inputDate ->
                TestTable.insert {
                    it[date] = inputDate
                    it[day] = inputDate.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.ENGLISH)
                }
            }

            val query = TestTable.selectAll().andWhere {
                TestTable.date.between(
                    LocalDate.of(2020, 11, 20),
                    LocalDate.of(2020, 11, 21)
                )
            }

            println(query.prepareSQL(this))

            query.forEach {
                val date = it[TestTable.date]
                val day = it[TestTable.day]
                println("$date is a $day")
            }
        }
    }
}
