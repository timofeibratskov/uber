package com.example.driver_service.repository

import com.example.driver_service.model.entity.OutboxEventEntity
import org.apache.ibatis.annotations.Delete
import org.apache.ibatis.annotations.Insert
import org.apache.ibatis.annotations.Mapper
import org.apache.ibatis.annotations.Select
import org.apache.ibatis.annotations.Update

@Mapper
interface OutboxEventRepository {
    @Select("select * from outbox_event_table order by created_at")
    fun findAllByOrderByCreatedAt(): List<OutboxEventEntity>

    @Insert("""insert into outbox_event_table (topic, event_type, payload, created_at) values (#{topic}, #{eventType}, #{payload}, #{createdAt})""")
    fun save(outboxEventEntity: OutboxEventEntity): Int

    @Delete("delete from outbox_event_table where id = #{id}")
    fun deleteById(id: Long): Int

    @Update("TRUNCATE TABLE outbox_event_table")
    fun deleteAll()
}
