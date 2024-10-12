package mobi.sevenwinds.migrations

import mobi.sevenwinds.app.budget.BudgetTable
import mobi.sevenwinds.app.budget.BudgetType
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update

fun updateBudgetType() {
    transaction {
        // Обновляем значение 'Комиссия' на 'Расход'
        BudgetTable.update({ BudgetTable.type eq BudgetType.Комиссия }) {
            it[type] = BudgetType.Расход
        }
    }
}