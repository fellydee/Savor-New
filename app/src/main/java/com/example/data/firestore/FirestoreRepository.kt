package com.example.data.firestore

import android.content.Context
import com.example.R
import com.example.data.model.Allergen
import com.example.data.model.Recipe
import com.example.data.model.ShoppingItem
import com.example.data.model.User
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.util.UUID

class FirestoreRepository(
    val firestore: FirebaseFirestore,
    val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {
    // Secondary constructor resolving named database ID from firebase_applet_config.xml
    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        ),
        FirebaseAuth.getInstance()
    )

    fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")
    }

    suspend fun saveUserProfile(user: User): Result<Unit> {
        val uid = requireUserId()
        val docRef = firestore.collection("users").document(uid)
        return try {
            val data = mapOf(
                "userId" to uid,
                "username" to user.username,
                "displayName" to user.displayName,
                "email" to (auth.currentUser?.email ?: user.email),
                "city" to user.city,
                "bio" to user.bio,
                "avatarUrl" to (auth.currentUser?.photoUrl?.toString() ?: user.avatarUrl),
                "followersCount" to user.followersCount,
                "followingCount" to user.followingCount,
                "isPremium" to user.isPremium,
                "isVerified" to user.isVerified,
                "totalLikesReceived" to user.totalLikesReceived,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            docRef.set(data).await()
            Result.success(Unit)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path)
            Result.failure(e)
        }
    }

    fun observeUserProfile(userId: String): Flow<User?> = flow {
        val docRef = firestore.collection("users").document(userId)
        docRef.snapshots().map { snapshot ->
            if (snapshot.exists()) {
                val data = snapshot.data ?: return@map null
                User(
                    id = data["userId"] as? String ?: userId,
                    username = data["username"] as? String ?: "chef",
                    displayName = data["displayName"] as? String ?: "Chef",
                    email = data["email"] as? String ?: "",
                    city = data["city"] as? String ?: "Austin, TX",
                    avatarUrl = data["avatarUrl"] as? String ?: "",
                    bio = data["bio"] as? String ?: "",
                    followersCount = (data["followersCount"] as? Number)?.toInt() ?: 0,
                    followingCount = (data["followingCount"] as? Number)?.toInt() ?: 0,
                    isPremium = data["isPremium"] as? Boolean ?: false,
                    isVerified = data["isVerified"] as? Boolean ?: false,
                    totalLikesReceived = (data["totalLikesReceived"] as? Number)?.toInt() ?: 0
                )
            } else {
                null
            }
        }.catch { e ->
            if (e is Exception) handleFirestoreError(e, OperationType.GET, docRef.path)
            throw e
        }.collect { emit(it) }
    }

    suspend fun createRecipe(recipe: Recipe): Result<String> {
        val uid = requireUserId()
        val docId = if (recipe.id.isNotBlank() && !recipe.id.startsWith("recipe_")) recipe.id else UUID.randomUUID().toString()
        val docRef = firestore.collection("recipes").document(docId)
        return try {
            val payload = mapOf(
                "authorId" to uid,
                "authorUsername" to recipe.authorUsername,
                "title" to recipe.title,
                "description" to recipe.description,
                "cookTimeMinutes" to recipe.cookTimeMinutes,
                "servings" to recipe.servings,
                "mediaType" to recipe.mediaType.name,
                "mediaUrl" to recipe.mediaUrl,
                "privacyLevel" to recipe.privacyLevel.name,
                "likesCount" to recipe.likesCount,
                "createdAt" to FieldValue.serverTimestamp()
            )
            docRef.set(payload).await()
            Result.success(docId)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun addShoppingItem(name: String, amount: String, unit: String): Result<String> {
        val uid = requireUserId()
        val itemId = UUID.randomUUID().toString()
        val docRef = firestore.collection("users").document(uid).collection("shoppingItems").document(itemId)
        return try {
            val payload = mapOf(
                "userId" to uid,
                "name" to name,
                "amount" to amount,
                "unit" to unit,
                "isChecked" to false,
                "createdAt" to FieldValue.serverTimestamp()
            )
            docRef.set(payload).await()
            Result.success(itemId)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            Result.failure(e)
        }
    }
}
