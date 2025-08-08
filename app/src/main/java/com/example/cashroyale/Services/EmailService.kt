package com.example.cashroyale.Services

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi


class EmailService {


    /*
    * We were initial going to use sendgrid but were unable to incorporate it into our project as it required
    * us to do domain authentication from our email sender.(DMARC check failed) Thus prompting us to use androids
    * built in email sender.
    * Gemini: Google, 05 June 2025 https://g.co/gemini/share/3dd862c80371*/
    @RequiresApi(Build.VERSION_CODES.O)
    fun sendSpendBreakdownEmail(context: Context, recipientEmail: String, breakdownText: String) {
        val emailSubject = "Your Monthly Spend Breakdown - ${java.time.LocalDate.now().month.name}"
        val emailBody = "Dear User,\n\nHere's your spend breakdown for the past month:\n\n" +
                "${breakdownText}\n\n" +
                "Thanks for using our app!"

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_EMAIL, arrayOf(recipientEmail))
            putExtra(Intent.EXTRA_SUBJECT, emailSubject)
            putExtra(Intent.EXTRA_TEXT, emailBody)
        }

        if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(Intent.createChooser(intent, "Send budget report via..."))
        } else {
            Toast.makeText(context, "No email app found.", Toast.LENGTH_SHORT).show()
        }
    }
}