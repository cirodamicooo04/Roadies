package it.roadies.android_app.repository

import android.content.Context
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import it.roadies.android_app.client.apis.user.DocumentManagementApi
import it.roadies.android_app.client.models.user.UserDocumentRequestDTO
import it.roadies.android_app.client.models.user.UserDocumentResponseDTO
import it.roadies.android_app.repository.utils.ApiResponse
import it.roadies.android_app.repository.utils.safeApiCall
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject

class UserDocumentRepository @Inject constructor(
    private val documentApi: DocumentManagementApi,
    @ApplicationContext private val context: Context
) {

    suspend fun getMyDocuments(userId: String): ApiResponse<List<UserDocumentResponseDTO>> {
        return safeApiCall { documentApi.getMyDocumentsByUser(userId) }
    }

    suspend fun uploadDocument(
        userId: String,
        documentType: UserDocumentRequestDTO.DocumentType,
        documentNumber: String,
        uri: Uri
    ): ApiResponse<UserDocumentResponseDTO> {
        val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
            ?: throw IllegalArgumentException("Impossibile leggere il file selezionato")

        val mimeType = context.contentResolver.getType(uri) ?: "application/pdf"

        val filePart = MultipartBody.Part.createFormData(
            name = "file",
            filename = "document_${System.currentTimeMillis()}",
            body = bytes.toRequestBody(mimeType.toMediaTypeOrNull())
        )

        val requestDto = UserDocumentRequestDTO(documentType, documentNumber)

        return safeApiCall {
            documentApi.upload(userId, requestDto, filePart)
        }
    }
}