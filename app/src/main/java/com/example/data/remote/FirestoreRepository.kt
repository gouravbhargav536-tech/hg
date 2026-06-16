package com.example.data.remote

import com.example.data.models.FollowUp
import com.example.data.models.Lead
import com.example.data.models.Template
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class FirestoreRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val leadsCollection = firestore.collection("leads")
    private val templatesCollection = firestore.collection("templates")
    private val followUpsCollection = firestore.collection("follow_ups")

    val allLeads: Flow<List<Lead>> = leadsCollection.snapshots().map { snapshot ->
        snapshot.toObjects(Lead::class.java)
    }

    val allTemplates: Flow<List<Template>> = templatesCollection.snapshots().map { snapshot ->
        snapshot.toObjects(Template::class.java)
    }

    val pendingFollowUps: Flow<List<FollowUp>> = followUpsCollection
        .whereEqualTo("completed", false)
        .snapshots().map { snapshot ->
            snapshot.toObjects(FollowUp::class.java)
        }

    suspend fun getLeadById(id: String): Lead? {
        return leadsCollection.document(id).get().await().toObject(Lead::class.java)
    }

    suspend fun insertLead(lead: Lead) {
        val docRef = if (lead.id == 0L) leadsCollection.document() else leadsCollection.document(lead.id.toString())
        val updatedLead = if (lead.id == 0L) {
            // In a real app, you might want to use the docId as the id
            lead.copy(id = docRef.id.hashCode().toLong()) 
        } else lead
        docRef.set(updatedLead).await()
    }

    suspend fun updateLead(lead: Lead) {
        leadsCollection.document(lead.id.toString()).set(lead).await()
    }

    suspend fun deleteLead(lead: Lead) {
        leadsCollection.document(lead.id.toString()).delete().await()
    }

    suspend fun insertTemplate(template: Template) {
        templatesCollection.document().set(template).await()
    }

    suspend fun deleteTemplate(template: Template) {
        templatesCollection.document(template.id.toString()).delete().await()
    }

    suspend fun insertFollowUp(followUp: FollowUp) {
        followUpsCollection.document().set(followUp).await()
    }

    suspend fun updateFollowUp(followUp: FollowUp) {
        followUpsCollection.document(followUp.id.toString()).set(followUp).await()
    }
}
