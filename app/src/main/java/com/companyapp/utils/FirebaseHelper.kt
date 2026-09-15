package com.companyapp.utils

import android.content.Context
import com.companyapp.R
import com.companyapp.models.Company
import com.companyapp.models.Employee
import com.companyapp.models.Permissions
import com.companyapp.models.StockItem
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

object FirebaseHelper {
    val auth: FirebaseAuth get() = FirebaseAuth.getInstance()
    val db: FirebaseFirestore get() = Firebase.firestore

    fun getGoogleSignInClient(context: Context): GoogleSignInClient {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(R.string.default_web_client_id))
            .requestEmail()
            .requestProfile()
            .build()
        return GoogleSignIn.getClient(context, gso)
    }

    fun signOut(context: Context, onComplete: () -> Unit) {
        auth.signOut()
        getGoogleSignInClient(context).signOut().addOnCompleteListener { onComplete() }
    }

    private fun emailKey(email: String) =
        email.trim().lowercase().replace(".", "_dot_").replace("@", "_at_")

    fun isGmailAddress(email: String?): Boolean {
        val value = email?.trim()?.lowercase() ?: return false
        return value.endsWith("@gmail.com") || value.endsWith("@googlemail.com")
    }

    fun createCompany(
        company: Company,
        onSuccess: (String) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val ref = db.collection("companies").document()
        ref.set(company.copy(id = ref.id))
            .addOnSuccessListener { onSuccess(ref.id) }
            .addOnFailureListener { onError(it) }
    }

    fun getCompanyByShareCode(
        code: String,
        onSuccess: (Company?) -> Unit,
        onError: (Exception) -> Unit
    ) {
        db.collection("companies").whereEqualTo("shareCode", code).get()
            .addOnSuccessListener { snap ->
                val doc = snap.documents.firstOrNull()
                onSuccess(doc?.toObject(Company::class.java)?.copy(id = doc.id))
            }
            .addOnFailureListener { onError(it) }
    }

    fun getCompanyByOwnerId(
        ownerId: String,
        onSuccess: (Company?) -> Unit,
        onError: (Exception) -> Unit
    ) {
        db.collection("companies").whereEqualTo("ownerId", ownerId).get()
            .addOnSuccessListener { snap ->
                val doc = snap.documents.firstOrNull()
                onSuccess(doc?.toObject(Company::class.java)?.copy(id = doc.id))
            }
            .addOnFailureListener { onError(it) }
    }

    fun getCompany(
        companyId: String,
        onSuccess: (Company?) -> Unit,
        onError: (Exception) -> Unit
    ) {
        db.collection("companies").document(companyId).get()
            .addOnSuccessListener { doc ->
                onSuccess(doc.toObject(Company::class.java)?.copy(id = doc.id))
            }
            .addOnFailureListener { onError(it) }
    }

    fun addEmployee(
        companyId: String,
        employee: Employee,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        db.collection("companies").document(companyId)
            .collection("employees").document(emailKey(employee.email))
            .set(employee.copy(email = employee.email.trim().lowercase()))
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onError(it) }
    }

    fun checkEmployeeAuthorized(
        companyId: String,
        email: String,
        onResult: (Boolean, Employee?) -> Unit
    ) {
        db.collection("companies").document(companyId)
            .collection("employees").document(emailKey(email)).get()
            .addOnSuccessListener { doc ->
                onResult(doc.exists(), doc.toObject(Employee::class.java))
            }
            .addOnFailureListener { onResult(false, null) }
    }

    fun markEmployeeJoined(
        companyId: String,
        employee: Employee,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        db.collection("companies").document(companyId)
            .collection("employees").document(emailKey(employee.email))
            .set(employee.copy(email = employee.email.trim().lowercase()))
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onError(it) }
    }

    fun getEmployees(
        companyId: String,
        onSuccess: (List<Employee>) -> Unit,
        onError: (Exception) -> Unit
    ) {
        db.collection("companies").document(companyId)
            .collection("employees").get()
            .addOnSuccessListener { snap ->
                onSuccess(snap.documents.mapNotNull { it.toObject(Employee::class.java) })
            }
            .addOnFailureListener { onError(it) }
    }

    fun updateEmployeePermissions(
        companyId: String,
        email: String,
        permissions: Permissions,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        db.collection("companies").document(companyId)
            .collection("employees").document(emailKey(email))
            .update("permissions", permissions)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onError(it) }
    }

    fun removeEmployee(
        companyId: String,
        email: String,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        db.collection("companies").document(companyId)
            .collection("employees").document(emailKey(email))
            .delete()
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onError(it) }
    }

    fun getStockItems(
        companyId: String,
        onSuccess: (List<StockItem>) -> Unit,
        onError: (Exception) -> Unit
    ) {
        db.collection("companies").document(companyId)
            .collection("stock").get()
            .addOnSuccessListener { snap ->
                onSuccess(snap.documents.mapNotNull { it.toObject(StockItem::class.java)?.copy(id = it.id) })
            }
            .addOnFailureListener { onError(it) }
    }

    fun saveStockItem(
        companyId: String,
        item: StockItem,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        val ref = if (item.id.isEmpty()) {
            db.collection("companies").document(companyId).collection("stock").document()
        } else {
            db.collection("companies").document(companyId).collection("stock").document(item.id)
        }
        ref.set(item.copy(id = ref.id, lastUpdated = System.currentTimeMillis()))
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onError(it) }
    }

    fun deleteStockItem(
        companyId: String,
        itemId: String,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        db.collection("companies").document(companyId)
            .collection("stock").document(itemId)
            .delete()
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onError(it) }
    }
}
