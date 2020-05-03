package m.kampukter.mypeertopeer.ui

import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.widget.EditText
import androidx.fragment.app.DialogFragment
import m.kampukter.mypeertopeer.WebSocketService
import m.kampukter.mypeertopeer.privateNotesApplication

class NewUserDialogFragment: DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val input = EditText(activity)
        input.inputType = InputType.TYPE_CLASS_TEXT
        val builder = AlertDialog.Builder(activity)
        builder.setView(input)
            .setTitle("New User")
            .setPositiveButton(android.R.string.yes) { _, _ ->

                val stringCharacters1 = ('a'..'z') + ('A'..'Z') + ('0'..'9').toList().toTypedArray()
                val keyOne = (1..8).map { stringCharacters1.random() }.joinToString("")
                val keyTwo = (1..24).map { stringCharacters1.random() }.joinToString("")

                privateNotesApplication.saveMyName(input.text.toString(),"$keyOne-${(1..9).random()}-$keyTwo")
                //val serviceIntent = Intent(, WebSocketService::class.java)
                //startService(serviceIntent)
                startActivity(
                    Intent(
                        activity,
                        MainActivity::class.java
                    ).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                )
            }
            .setNegativeButton(android.R.string.no){_,_-> }

        return builder.create()
    }
    companion object {
        fun create():NewUserDialogFragment = NewUserDialogFragment()
    }

}
