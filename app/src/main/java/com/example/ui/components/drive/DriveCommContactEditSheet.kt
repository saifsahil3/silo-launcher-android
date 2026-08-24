package com.example.ui.components.drive

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.provider.ContactsContract
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.content.Context
import android.graphics.drawable.Drawable
import android.provider.Telephony
import androidx.compose.foundation.Image
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import coil.compose.rememberAsyncImagePainter
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.db.DriveCommShortcutEntity

/**
 * Bottom sheet to configure a Communication shortcut with native Contact Picker.
 *
 * Channel selector is a single horizontal row of 4 pills, each with a distinct
 * coloured icon background circle so channel types are immediately scannable.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DriveCommContactEditSheet(
    slotIndex: Int,
    existingShortcut: DriveCommShortcutEntity?,
    onSave: (name: String, phoneNumber: String, channelType: String, photoUri: String?) -> Unit,
    onDelete: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var contactName    by remember { mutableStateOf(existingShortcut?.name        ?: "") }
    var phoneNumber    by remember { mutableStateOf(existingShortcut?.phoneNumber  ?: "") }
    var selectedChannel by remember { mutableStateOf(existingShortcut?.channelType ?: "CALL") }
    var photoUri       by remember { mutableStateOf<String?>(existingShortcut?.photoUri) }

    // Direct Phone Number Contact Picker
    val phonePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data?.data != null) {
            val contactUri: Uri = result.data!!.data!!
            try {
                val projection = arrayOf(
                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                    ContactsContract.CommonDataKinds.Phone.NUMBER,
                    ContactsContract.CommonDataKinds.Phone.PHOTO_URI,
                    ContactsContract.CommonDataKinds.Phone.PHOTO_THUMBNAIL_URI
                )
                context.contentResolver.query(contactUri, projection, null, null, null)?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val nameCol  = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                        val numCol   = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                        val photoCol = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_URI)
                        val thumbCol = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_THUMBNAIL_URI)

                        if (nameCol  != -1) { val v = cursor.getString(nameCol);  if (!v.isNullOrBlank()) contactName = v }
                        if (numCol   != -1) { val v = cursor.getString(numCol);   if (!v.isNullOrBlank()) phoneNumber = v }
                        var fetchedPhoto: String? = null
                        if (photoCol != -1) { fetchedPhoto = cursor.getString(photoCol) }
                        if (fetchedPhoto.isNullOrBlank() && thumbCol != -1) { fetchedPhoto = cursor.getString(thumbCol) }
                        photoUri = resolveAndCopyContactPhoto(context, slotIndex, contactUri, fetchedPhoto)
                    }
                }
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFF141722),
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(22.dp)
        ) {
            // ── Header ────────────────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Contact Shortcut",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color.White.copy(alpha = 0.7f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // ── Contact Picker Button ──────────────────────────────────────────
            Button(
                onClick = {
                    val pickIntent = Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI)
                    phonePickerLauncher.launch(pickIntent)
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("pick_from_contacts_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Contacts,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Pick from Device Contacts",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // ── Contact Name ───────────────────────────────────────────────────
            OutlinedTextField(
                value = contactName,
                onValueChange = { contactName = it },
                label = { Text("Contact Name") },
                placeholder = { Text("e.g. Mom, Sarah, Office") },
                singleLine = true,
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = Color(0xFF3B82F6)
                    )
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = Color(0xFF3B82F6),
                    unfocusedBorderColor = Color(0xFF283042)
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))

            // ── Phone Number ───────────────────────────────────────────────────
            OutlinedTextField(
                value = phoneNumber,
                onValueChange = { phoneNumber = it },
                label = { Text("Phone Number") },
                placeholder = { Text("+1 (555) 000-0000") },
                singleLine = true,
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Call,
                        contentDescription = null,
                        tint = Color(0xFF3B82F6)
                    )
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = Color(0xFF3B82F6),
                    unfocusedBorderColor = Color(0xFF283042)
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(18.dp))

            // ── Channel Selector — single horizontal row ───────────────────────
            Text(
                text = "Communication Channel",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White.copy(alpha = 0.8f)
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ChannelOptionPill(
                    channel     = "CALL",
                    label       = "Call",
                    icon        = Icons.Default.Call,
                    iconBgColor = Color(0xFF10B981),
                    isSelected  = selectedChannel == "CALL",
                    onClick     = { selectedChannel = "CALL" },
                    modifier    = Modifier.weight(1f)
                )
                ChannelOptionPill(
                    channel     = "WHATSAPP",
                    label       = "WhatsApp",
                    icon        = Icons.AutoMirrored.Filled.Chat,
                    iconBgColor = Color(0xFF25D366),
                    isSelected  = selectedChannel == "WHATSAPP",
                    onClick     = { selectedChannel = "WHATSAPP" },
                    modifier    = Modifier.weight(1f)
                )
                ChannelOptionPill(
                    channel     = "SMS",
                    label       = "SMS",
                    icon        = Icons.AutoMirrored.Filled.Message,
                    iconBgColor = Color(0xFF0284C7),
                    isSelected  = selectedChannel == "SMS",
                    onClick     = { selectedChannel = "SMS" },
                    modifier    = Modifier.weight(1f)
                )
                ChannelOptionPill(
                    channel     = "TELEGRAM",
                    label       = "Telegram",
                    icon        = Icons.AutoMirrored.Filled.Send,
                    iconBgColor = Color(0xFF229ED9),
                    isSelected  = selectedChannel == "TELEGRAM",
                    onClick     = { selectedChannel = "TELEGRAM" },
                    modifier    = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(22.dp))

            // ── Action Buttons ─────────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (existingShortcut != null && existingShortcut.phoneNumber.isNotBlank()) {
                    OutlinedButton(
                        onClick = {
                            onDelete()
                            onDismiss()
                        },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF4444)),
                        border = BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Clear")
                    }
                }

                Button(
                    onClick = {
                        onSave(contactName.trim(), phoneNumber.trim(), selectedChannel, photoUri)
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1.5f)
                ) {
                    Text("Save Shortcut", fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}

/**
 * Single channel pill with a distinct coloured icon-background circle.
 * When selected, the border brightens and the circle becomes fully opaque.
 */
@Composable
private fun ChannelOptionPill(
    channel: String,
    label: String,
    icon: ImageVector,
    iconBgColor: Color,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val appIconDrawable = remember(channel) {
        getChannelAppDrawable(context, channel)
    }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) iconBgColor.copy(alpha = 0.15f) else Color(0xFF1B202D),
        border = BorderStroke(1.5.dp, if (isSelected) iconBgColor else Color(0xFF283042)),
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            // Coloured icon background circle
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(30.dp)
                    .clip(CircleShape)
                    .background(
                        if (isSelected) iconBgColor
                        else iconBgColor.copy(alpha = 0.18f)
                    )
            ) {
                if (appIconDrawable != null) {
                    val appIconPainter = rememberAsyncImagePainter(appIconDrawable)
                    Image(
                        painter = appIconPainter,
                        contentDescription = label,
                        modifier = Modifier.size(18.dp)
                    )
                } else {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        tint = if (isSelected) Color.White else iconBgColor,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            Text(
                text = label,
                fontSize = 10.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) Color.White else Color.White.copy(alpha = 0.65f)
            )
        }
    }
}

private fun getChannelAppDrawable(context: Context, channelType: String): Drawable? {
    val pm = context.packageManager
    val packageName = when (channelType.uppercase()) {
        "WHATSAPP" -> "com.whatsapp"
        "TELEGRAM" -> "org.telegram.messenger"
        "SMS"      -> Telephony.Sms.getDefaultSmsPackage(context) ?: "com.google.android.apps.messaging"
        else -> {
            val dialIntent = Intent(Intent.ACTION_DIAL)
            dialIntent.resolveActivity(pm)?.packageName ?: "com.google.android.dialer"
        }
    }

    return try {
        pm.getApplicationIcon(packageName)
    } catch (e: Exception) {
        try {
            if (channelType.uppercase() == "WHATSAPP") pm.getApplicationIcon("com.whatsapp.w4b") else null
        } catch (ex: Exception) {
            null
        }
    }
}

private fun resolveAndCopyContactPhoto(
    context: Context,
    slotIndex: Int,
    contactUri: Uri,
    fallbackPhotoUriString: String?
): String? {
    return try {
        var input: java.io.InputStream? = try {
            ContactsContract.Contacts.openContactPhotoInputStream(context.contentResolver, contactUri, true)
        } catch (e: Exception) { null }

        if (input == null && !fallbackPhotoUriString.isNullOrBlank()) {
            input = try {
                context.contentResolver.openInputStream(Uri.parse(fallbackPhotoUriString))
            } catch (e: Exception) { null }
        }

        if (input != null) {
            val dir = java.io.File(context.filesDir, "drive_photos").apply { mkdirs() }
            val file = java.io.File(dir, "contact_slot_$slotIndex.jpg")
            file.outputStream().use { output ->
                input.copyTo(output)
            }
            input.close()
            return Uri.fromFile(file).toString()
        }
        null
    } catch (e: Exception) {
        e.printStackTrace()
        fallbackPhotoUriString
    }
}
