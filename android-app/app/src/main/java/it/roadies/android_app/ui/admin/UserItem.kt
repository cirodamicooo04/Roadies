import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import it.roadies.android_app.client.models.user.UserProfileResponseDTO

@Composable
fun UserItem(
    user: UserProfileResponseDTO,
    onBlockClick: (String) -> Unit,
    onUnblockClick: (String) -> Unit
) {
    Card(modifier = Modifier.padding(8.dp).fillMaxWidth()) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = "${user.firstName} ${user.lastName}", style = MaterialTheme.typography.titleMedium)
                Text(text = user.email, style = MaterialTheme.typography.bodyMedium)
            }
            if (user.enabled) {
                Button(onClick = { onBlockClick(user.keycloakId) }) { Text("Block") }
            } else {
                Button(onClick = { onUnblockClick(user.keycloakId) }, colors = ButtonDefaults.buttonColors(containerColor = Color.Red)) {
                    Text("Unblock")
                }
            }
        }
    }
}