package fr.fred.launcher.phone

import android.content.ContentUris
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.ContactsContract

/**
 * Préparation de Fred Phone.
 * Ce composant n'est pas activé dans la V1 Launcher : il sera utilisé lorsque
 * Fred deviendra éventuellement l'application Téléphone par défaut.
 */
class ContactPhotoResolver(private val context: Context) {
    data class ContactInfo(
        val displayName: String?,
        val photo: Bitmap?,
    )

    fun findByPhoneNumber(phoneNumber: String): ContactInfo? {
        val lookupUri = Uri.withAppendedPath(
            ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
            Uri.encode(phoneNumber),
        )

        val projection = arrayOf(
            ContactsContract.PhoneLookup._ID,
            ContactsContract.PhoneLookup.DISPLAY_NAME,
        )

        context.contentResolver.query(lookupUri, projection, null, null, null)?.use { cursor ->
            if (!cursor.moveToFirst()) return null
            val id = cursor.getLong(cursor.getColumnIndexOrThrow(ContactsContract.PhoneLookup._ID))
            val name = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.PhoneLookup.DISPLAY_NAME))
            val contactUri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, id)
            val photo = ContactsContract.Contacts.openContactPhotoInputStream(
                context.contentResolver,
                contactUri,
                true,
            )?.use(BitmapFactory::decodeStream)
            return ContactInfo(name, photo)
        }
        return null
    }
}
