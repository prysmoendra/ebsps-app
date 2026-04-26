package com.example.ebsps

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.PopupMenu
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class SurveyAdapter(
    private val surveys: MutableList<Survey> = mutableListOf(),
    private val onDeleteSurvey: ((Survey) -> Unit)? = null
) : RecyclerView.Adapter<SurveyAdapter.SurveyViewHolder>() {

    class SurveyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvOwnerName: TextView = itemView.findViewById(R.id.tv_owner_name)
        val tvAddress: TextView = itemView.findViewById(R.id.tv_address)
        val btnMenuOverflow: ImageButton = itemView.findViewById(R.id.btn_menu_overflow)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SurveyViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_survey, parent, false)
        return SurveyViewHolder(view)
    }

    override fun onBindViewHolder(holder: SurveyViewHolder, position: Int) {
        val survey = surveys[position]
        holder.tvOwnerName.text = survey.ownerName
        holder.tvAddress.text = survey.address
        
        // Set click listener for the entire item
        holder.itemView.setOnClickListener {
            val intent = Intent(holder.itemView.context, DetailSurveyActivity::class.java)
            intent.putExtra("survey_id", survey.id)
            holder.itemView.context.startActivity(intent)
        }
        
        // Set up menu overflow button
        holder.btnMenuOverflow.setOnClickListener { view ->
            showPopupMenu(view, survey)
        }
    }

    override fun getItemCount(): Int = surveys.size

    fun updateSurveys(newSurveys: List<Survey>) {
        android.util.Log.d("SurveyAdapter", "Updating surveys: ${newSurveys.size} items")
        surveys.clear()
        surveys.addAll(newSurveys)
        notifyDataSetChanged()
        android.util.Log.d("SurveyAdapter", "Surveys updated, total items: ${surveys.size}")
    }
    
    private fun showPopupMenu(view: View, survey: Survey) {
        val popup = PopupMenu(view.context, view)
        popup.menuInflater.inflate(R.menu.survey_item_menu, popup.menu)
        
        popup.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_delete -> {
                    onDeleteSurvey?.invoke(survey)
                    true
                }
                else -> false
            }
        }
        
        popup.show()
    }
}
