package com.example.data.firestore

import com.example.base.FirestoreEmulatorTestBase
import com.example.data.model.Recipe
import com.example.data.model.User
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class FirestoreRepositoryRuleTest : FirestoreEmulatorTestBase() {

    @Test
    fun saveUserProfile_authenticatedUser_succeeds() = runBlocking {
        val uid = signInTestUser("elena@savor.app")
        val repo = FirestoreRepository(firestore, auth)

        val user = User(
            id = uid,
            username = "chef_elena",
            displayName = "Elena Rostova",
            email = "elena@savor.app",
            city = "Austin, TX"
        )

        val result = repo.saveUserProfile(user)
        assertTrue(result.isSuccess)

        val snapshot = firestore.collection("users").document(uid).get().await()
        assertTrue(snapshot.exists())
    }

    @Test
    fun unauthenticated_access_throwsException() = runBlocking {
        auth.signOut()
        val repo = FirestoreRepository(firestore, auth)

        var thrown = false
        try {
            repo.requireUserId()
        } catch (e: IllegalStateException) {
            thrown = true
        }
        assertTrue("requireUserId should fail when unauthenticated", thrown)
    }

    @Test
    fun createRecipe_authenticatedUser_succeeds() = runBlocking {
        val uid = signInTestUser("marco@savor.app")
        val repo = FirestoreRepository(firestore, auth)

        val recipe = Recipe(
            id = "test_recipe_1",
            authorId = uid,
            authorUsername = "marco_roma",
            title = "Tuscan Salmon",
            description = "Crispy pan-seared wild salmon"
        )

        val result = repo.createRecipe(recipe)
        assertTrue(result.isSuccess)
        val docId = result.getOrNull()
        assertNotNull(docId)
    }

    @Test
    fun shoppingList_scopedToAuthenticatedUser() = runBlocking {
        val uid = signInTestUser("jin@savor.app")
        val repo = FirestoreRepository(firestore, auth)

        val result = repo.addShoppingItem("Wild Salmon", "2", "fillets")
        assertTrue(result.isSuccess)
        val itemId = result.getOrNull()
        assertNotNull(itemId)

        val doc = firestore.collection("users").document(uid)
            .collection("shoppingItems").document(itemId!!).get().await()
        assertTrue(doc.exists())
    }
}
