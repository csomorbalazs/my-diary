package hu.csomorbalazs.mydiary

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.widget.EditText
import android.widget.RadioButton
import androidx.fragment.app.DialogFragment
import hu.csomorbalazs.mydiary.data.DiaryEntry
import kotlinx.android.synthetic.main.diary_entry_dialog.view.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class DiaryEntryDialog : DialogFragment() {

    interface DiaryEntryHandler {
        fun diaryEntryCreated(diaryEntry: DiaryEntry)
    }

    lateinit var diaryEntryHandler: DiaryEntryHandler

    override fun onAttach(context: Context) {
        super.onAttach(context)

        if (context is DiaryEntryHandler) {
            diaryEntryHandler = context
        } else {
            throw RuntimeException(
                "The Activity doesn't implement the DiaryEntryHandler interface"
            )
        }
    }

    lateinit var rbPersonal: RadioButton
    lateinit var etTitle: EditText
    lateinit var etDescription: EditText
    lateinit var etDate: EditText
    lateinit var etPlace: EditText

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialogBuilder = AlertDialog.Builder(requireContext())

        val dialogView = requireActivity().layoutInflater.inflate(
            R.layout.diary_entry_dialog, null
        )

        rbPersonal = dialogView.rbPersonal
        etTitle = dialogView.etTitle
        etDescription = dialogView.etDescription
        etDate = dialogView.etDate
        etPlace = dialogView.etPlace

        val now = LocalDate.now()

        etDate.setText(now.format(DateTimeFormatter.ISO_LOCAL_DATE))

        dialogView.etDate.setOnClickListener {
            val picker = DatePickerDialog(
                requireContext(),
                DatePickerDialog.OnDateSetListener { view, year, month, dayOfMonth ->
                    etDate.setText(
                        LocalDate.of(
                            year,
                            month,
                            dayOfMonth
                        ).format(DateTimeFormatter.ISO_LOCAL_DATE)
                    )
                }, now.year, now.monthValue, now.dayOfMonth
            )

            picker.show()
        }

        dialogBuilder.setView(dialogView)
        dialogBuilder.setPositiveButton("Create") { _, _ -> }

        return dialogBuilder.create()
    }

    override fun onResume() {
        super.onResume()

        (dialog as AlertDialog).getButton(Dialog.BUTTON_POSITIVE)
            .setOnClickListener {
                if (etTitle.text.isNotEmpty() && etDescription.text.isNotEmpty()) {
                    handleDiaryEntryCreate()
                    (dialog as AlertDialog).dismiss()
                } else {
                    if (etTitle.text.isNullOrEmpty()) {
                        etTitle.error =
                            "The title can not be empty"
                    }

                    if (etDescription.text.isNullOrEmpty()) {
                        etDescription.error =
                            "The description can not be empty"
                    }
                }
            }
    }

    private fun handleDiaryEntryCreate() {
        val date = if (etDate.text.isNullOrEmpty()) {
            LocalDate.now()
        } else {
            LocalDate.parse(etDate.text.toString(), DateTimeFormatter.ISO_LOCAL_DATE)
        }

        diaryEntryHandler.diaryEntryCreated(
            DiaryEntry(
                null,
                date,
                etTitle.text.toString(),
                etDescription.text.toString(),
                rbPersonal.isChecked,
                etPlace.text.toString()
            )
        )
    }

}