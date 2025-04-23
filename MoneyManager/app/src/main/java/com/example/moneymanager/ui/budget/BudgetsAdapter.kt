package com.example.moneymanager.ui.budget

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.moneymanager.R
import com.example.moneymanager.database.LocalBudget

class BudgetsAdapter(private val budgets: List<LocalBudget>) :
    RecyclerView.Adapter<BudgetsAdapter.BudgetViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BudgetViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_budget, parent, false)
        return BudgetViewHolder(view)
    }

    override fun onBindViewHolder(holder: BudgetViewHolder, position: Int) {
        val budget = budgets[position]
        holder.bind(budget)
    }

    override fun getItemCount(): Int = budgets.size

    class BudgetViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameTextView: TextView = itemView.findViewById(R.id.budget_name)
        private val amountTextView: TextView = itemView.findViewById(R.id.budget_amount)
        private val spentView: View = itemView.findViewById(R.id.spent_view)
        private val remainingView: View = itemView.findViewById(R.id.remaining_view)
        private val spentAmountText: TextView = itemView.findViewById(R.id.spent_amount_text)

        fun bind(budget: LocalBudget) {
            nameTextView.text = budget.name
            amountTextView.text = "$${budget.amount}"

            // Use ViewTreeObserver to ensure that the layout is measured before calculating the width
            itemView.viewTreeObserver.addOnPreDrawListener {
                val totalWidth = itemView.width // This will now be correct after the layout is measured

                val percentSpent = budget.percentSpent
                val spentWidth = (totalWidth * (percentSpent / 100)).toInt()
                val remainingWidth = totalWidth - spentWidth

                // Set the width of the spent view
                val paramsSpent = spentView.layoutParams
                paramsSpent.width = spentWidth
                spentView.layoutParams = paramsSpent

                // Set the width of the remaining view
                val paramsRemaining = remainingView.layoutParams
                paramsRemaining.width = remainingWidth
                remainingView.layoutParams = paramsRemaining

                // Calculate amount spent
                val spentAmount = (budget.amount * (percentSpent / 100))

                // Set the text of the spent amount
                spentAmountText.text = "Spent: $${spentAmount}"

                // Optionally change the color of the spent view (green/red depending on the remaining amount)
                if (spentAmount == budget.amount) {
                    spentView.setBackgroundResource(android.R.color.holo_red_dark) // Red if all is spent
                    amountTextView.setTextColor(ContextCompat.getColor(itemView.context, android.R.color.holo_red_dark))
                } else {
                    spentView.setBackgroundResource(android.R.color.holo_green_light) // Green otherwise

                }

                true
            }
        }
    }
}
