package hu.csomorbalazs.mydiary.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import hu.csomorbalazs.mydiary.MainListActivity
import hu.csomorbalazs.mydiary.R
import hu.csomorbalazs.mydiary.data.AppDatabase
import hu.csomorbalazs.mydiary.data.DiaryEntry
import kotlinx.android.synthetic.main.diary_entry_card.view.*
import java.time.format.DateTimeFormatter

class DiaryEntryAdapter(private val context: Context, diaryEntryList: List<DiaryEntry>) :
    RecyclerView.Adapter<DiaryEntryAdapter.ViewHolder>() {

    private var diaryEntries = mutableListOf<DiaryEntry>()

    init {
        diaryEntries.addAll(diaryEntryList)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val diaryEntryView = LayoutInflater.from(context).inflate(
            R.layout.diary_entry_card, parent, false
        )

        return ViewHolder(diaryEntryView)
    }

    override fun getItemCount(): Int {
        return diaryEntries.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val diaryEntry = diaryEntries[position]

        holder.tvTitle.text = diaryEntry.title
        holder.tvDescription.text = diaryEntry.description
        holder.tvCreationDate.text =
            diaryEntry.creationDate.format(DateTimeFormatter.ISO_LOCAL_DATE)
        holder.tvPlace.text = diaryEntry.place

        if (diaryEntry.isPersonal) {
            holder.ivType.setImageDrawable(
                context.getDrawable(R.drawable.ic_personal)
            )
        }

        if (diaryEntry.place.isNullOrEmpty()) {
            holder.ivLocation.visibility = View.GONE
        }

        holder.btnDelete.setOnClickListener {
            deleteDiaryEntry(holder.adapterPosition)
        }
    }

    fun addDiaryEntry(diaryEntry: DiaryEntry) {
        diaryEntries.add(diaryEntry)
        notifyItemInserted(diaryEntries.lastIndex)
    }

    fun deleteAllDiaryEntries() {
        val itemCount = diaryEntries.size
        diaryEntries.clear()
        notifyItemRangeRemoved(0, itemCount)
    }

    private fun deleteDiaryEntry(position: Int) {
        Thread {
            AppDatabase.getInstance(context)
                .diaryEntryDao()
                .deleteDiaryEntry(diaryEntries[position])

            (context as MainListActivity).runOnUiThread {
                diaryEntries.removeAt(position)
                notifyItemRemoved(position)
            }
        }.start()
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTitle: TextView = itemView.tvTitle
        val tvDescription: TextView = itemView.tvDescription
        val tvCreationDate: TextView = itemView.tvCreationDate
        val tvPlace: TextView = itemView.tvPlace

        val ivType: ImageView = itemView.ivType
        val ivLocation: ImageView = itemView.ivLocation

        val btnDelete: ImageButton = itemView.btnDelete
    }
}