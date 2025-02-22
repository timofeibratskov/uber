package com.example.rating_service.service
import com.example.rating_service.entity.DatabaseSequences
import org.springframework.data.mongodb.core.FindAndModifyOptions
import org.springframework.data.mongodb.core.MongoOperations
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import org.springframework.stereotype.Service

@Service
class SequenceGeneratorService (private val mongoOperations: MongoOperations){
    fun generateSequence(seqName: String): Long {
        val counter = mongoOperations.findAndModify(
            Query(Criteria.where("_id").`is`(seqName)),
            Update().inc("seq", 1),
            FindAndModifyOptions.options().returnNew(true).upsert(true),
            DatabaseSequences::class.java
        )
        return counter?.seq ?: 1
    }
}