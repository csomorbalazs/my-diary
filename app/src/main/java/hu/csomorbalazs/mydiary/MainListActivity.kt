package hu.csomorbalazs.mydiary

import android.content.DialogInterface
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import hu.csomorbalazs.mydiary.adapter.DiaryEntryAdapter
import hu.csomorbalazs.mydiary.data.AppDatabase
import hu.csomorbalazs.mydiary.data.DiaryEntry
import kotlinx.android.synthetic.main.activity_diary_entries.*

class MainListActivity : AppCompatActivity(), DiaryEntryDialog.DiaryEntryHandler {

    private lateinit var diaryEntryAdapter: DiaryEntryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_diary_entries)


        fabAdd.setOnClickListener {
            DiaryEntryDialog().show(supportFragmentManager, "DIARY_DIALOG")
        }

        fabDeleteAll.setOnClickListener {
            val alertDialog = AlertDialog.Builder(this)
                .setTitle("Are you sure?")
                .setMessage("This action will delete all entries in your diary")
                .setPositiveButton("Yes") { _, _ ->
                    deleteAllDiaryEntries()
                }
                .setNegativeButton("No") { _, _ -> }

            alertDialog.show()
        }

        initRecyclerView()
    }

    private fun initRecyclerView() {
        Thread {
            val diaryEntries = AppDatabase
                .getInstance(this@MainListActivity)
                .diaryEntryDao()
                .getAllDiaryEntries()

            runOnUiThread {
                diaryEntryAdapter = DiaryEntryAdapter(this, diaryEntries)

                recyclerDiary.adapter = diaryEntryAdapter
            }
        }.start()
    }

    override fun diaryEntryCreated(diaryEntry: DiaryEntry) {
        Thread {
            val newId = AppDatabase
                .getInstance(this@MainListActivity)
                .diaryEntryDao()
                .addDiaryEntry(diaryEntry)

            diaryEntry.diaryId = newId

            runOnUiThread {
                diaryEntryAdapter.addDiaryEntry(diaryEntry)
            }
        }.start()
    }

    private fun deleteAllDiaryEntries() {
        Thread {
            AppDatabase
                .getInstance(this@MainListActivity)
                .diaryEntryDao()
                .deleteAllDiaryEntries()

            runOnUiThread {
                diaryEntryAdapter.deleteAllDiaryEntries()
            }
        }.start()
    }
}
