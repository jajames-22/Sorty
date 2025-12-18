package com.example.sorty

import java.util.Properties
import javax.mail.Authenticator
import javax.mail.Message
import javax.mail.PasswordAuthentication
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

object GMailSender {

    private const val SENDER_EMAIL = "sorty.tridev@gmail.com"
    private const val SENDER_PASSWORD = "yvks bdrh mkoz olht" // 16-character App Password

    fun sendEmail(recipientEmail: String, subject: String, body: String): Boolean {
        return try {
            val props = Properties()
            props["mail.smtp.auth"] = "true"
            props["mail.smtp.starttls.enable"] = "true"
            props["mail.smtp.host"] = "smtp.gmail.com"
            props["mail.smtp.port"] = "587"

            val session = Session.getInstance(props, object : Authenticator() {
                override fun getPasswordAuthentication(): PasswordAuthentication {
                    return PasswordAuthentication(SENDER_EMAIL, SENDER_PASSWORD)
                }
            })

            val message = MimeMessage(session)
            message.setFrom(InternetAddress(SENDER_EMAIL))
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail))
            message.subject = subject

            // CRITICAL CHANGE: Set content type to HTML
            message.setContent(body, "text/html; charset=utf-8")

            Transport.send(message)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}