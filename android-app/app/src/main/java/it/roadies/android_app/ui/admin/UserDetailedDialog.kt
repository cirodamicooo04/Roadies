import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import it.roadies.android_app.client.models.user.UserResponseDTO
import it.roadies.android_app.ui.admin.UserDetailsUi

@Composable
fun UserDetailDialog(
    user: UserDetailsUi,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {

                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = "User Details", fontSize = 22.sp, fontWeight = FontWeight.Bold)

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .background(Color.Red, RoundedCornerShape(8.dp))
                            .size(32.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                }

                Divider(modifier = Modifier.padding(vertical = 16.dp))

                DetailRow(label = "ID", value = user.id)
                DetailRow(label = "Username", value = user.username ?: "N/A")
                DetailRow(label = "First Name", value = user.firstname)
                DetailRow(label = "Last Name", value = user.lastname)
                DetailRow(label = "email", value = user.email)
            }
        }
    }
}

@Composable
fun DetailRow(label: String, value: String?) {
    Row(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(text = "$label: ", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
        if (value != null) {
            Text(text = value, fontSize = 16.sp)
        }
    }
}