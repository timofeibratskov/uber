package com.example.driver_service.mybatisMapper

import com.example.driver_service.entity.DriverEntity
import org.apache.ibatis.annotations.*

@Mapper
interface DriverMapper {
    @Select("SELECT * FROM drivers")
    fun findAll(): List<DriverEntity>?

    @Select("SELECT * FROM drivers WHERE id = #{id}")
    fun findById(id: Long): DriverEntity?

    @Select("SELECT * FROM drivers WHERE gmail = #{gmail}")
    fun findByEmail(gmail: String): DriverEntity?

    @Select("SELECT * FROM drivers WHERE name = #{name}")
    fun findByName(name: String): DriverEntity?

    @Select("SELECT * FROM drivers WHERE phone_number = #{phoneNumber}")
    fun findByPhoneNumber(gmail: String): DriverEntity?

    @Insert(
        """
    INSERT INTO drivers (name, gmail, password, phone_number, rating)
    VALUES (#{name}, #{gmail}, #{password}, #{phoneNumber}, #{rating})
"""
    )
    @Options(useGeneratedKeys = true, keyProperty = "id")
    fun create(driver: DriverEntity): Int

    @Update(
        """UPDATE drivers
        SET name=#{name}, gmail=#{gmail}, password=#{password},
        phone_number=#{phoneNumber}, rating=#{rating}
        WHERE id=#{id}
        """
    )
    fun update(driver: DriverEntity): Int

    @Delete("DELETE FROM drivers WHERE id=#{id}")
    fun delete(id: Long): Int
}