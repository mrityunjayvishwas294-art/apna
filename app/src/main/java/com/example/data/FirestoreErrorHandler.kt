package com.example.data

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestoreException

enum class OperationType {
  CREATE,
  UPDATE,
  DELETE,
  LIST,
  GET,
  WRITE
}

data class FirestoreErrorInfo(
  val error: String,
  val operationType: OperationType,
  val path: String?,
  val authUserId: String?,
  val authEmail: String?,
  val isEmailVerified: Boolean?
)

fun handleFirestoreError(
  e: Throwable,
  operationType: OperationType,
  path: String?,
  auth: FirebaseAuth
): Nothing {
  val currentUser = auth.currentUser
  val info = FirestoreErrorInfo(
    error = e.message ?: e.toString(),
    operationType = operationType,
    path = path,
    authUserId = currentUser?.uid,
    authEmail = currentUser?.email,
    isEmailVerified = currentUser?.isEmailVerified
  )
  Log.e("FirestoreError", "Firestore $operationType failed at $path: $info", e)
  if (e is FirebaseFirestoreException && e.code == FirebaseFirestoreException.Code.PERMISSION_DENIED) {
    throw SecurityException("Permission denied on $path ($operationType): ${e.message}", e)
  }
  throw e
}
