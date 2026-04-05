package com.example.driver_service.repository

import com.example.driver_service.model.entity.DriverEntity
import java.util.UUID
import org.apache.ibatis.annotations.Insert
import org.apache.ibatis.annotations.Mapper
import org.apache.ibatis.annotations.Select
import org.apache.ibatis.annotations.Update

@Mapper
interface DriverRepository {
    @Select("SELECT COUNT(*) FROM driver_table WHERE email = #{email}")
    fun existsByEmail(email: String): Int

    @Select("SELECT COUNT(*) FROM driver_table WHERE phone_number = #{phoneNumber}")
    fun existsByPhoneNumber(phoneNumber: String): Int

    @Select("SELECT * FROM driver_table WHERE id = #{id}")
    fun findById(id: UUID): DriverEntity?

    @Select("SELECT * FROM driver_table WHERE email = #{email}")
    fun findByEmail(email: String): DriverEntity?

    @Insert("""INSERT INTO driver_table (id, name, email, password, phone_number, rating, gender, car_id, work_status, created_at, updated_at) VALUES (#{id}, #{name}, #{email}, #{password}, #{phoneNumber}, #{rating}, #{gender}, #{carId}, #{workStatus}, NOW(), NOW())""")
    fun save(driver: DriverEntity): Int

    @Update("""UPDATE driver_table SET updated_at = NOW(), name = #{name},  car_id = #{carId}, gender = #{gender}, phone_number = #{phoneNumber}, work_status = #{workStatus} WHERE id = #{id}""")
    fun update(driver: DriverEntity)

    @Update("TRUNCATE TABLE driver_table RESTART IDENTITY CASCADE")
    fun deleteAll()
}