package com.example.bookshelf.fragment

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat.startActivity

class AlertDialogFragment {
    fun errorHandling(message: String, paramContext : Context) {
        val alertDialog = AlertDialog.Builder(paramContext)
        alertDialog.setTitle("Ooops!")
        alertDialog.setMessage(message)
        alertDialog.setCancelable(false)
        alertDialog.setPositiveButton("Ok",
            DialogInterface.OnClickListener { dialog, which ->
                dialog.dismiss()
                val a = Intent(Intent.ACTION_MAIN)
                a.addCategory(Intent.CATEGORY_HOME)
                a.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(paramContext, a, Bundle())
            })
        alertDialog.create()
        alertDialog.show()
    }

    fun addBookErrorHandling(message: String, paramContext: Context) {
        val alertDialog = AlertDialog.Builder(paramContext)
        alertDialog.setTitle("Ooops!")
        alertDialog.setMessage(message)
        alertDialog.setCancelable(false)
        alertDialog.setPositiveButton("Ok",
            DialogInterface.OnClickListener { dialog, which ->
                dialog.dismiss()
            })
        alertDialog.create()
        alertDialog.show()
    }
}
