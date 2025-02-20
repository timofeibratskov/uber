package com.example.driver_service.mapper

import com.example.driver_service.entity.CarEntity
import org.apache.ibatis.annotations.Update
import org.apache.ibatis.annotations.Insert
import org.apache.ibatis.annotations.Mapper
import org.apache.ibatis.annotations.Options
import org.apache.ibatis.annotations.Select
import org.apache.ibatis.annotations.Delete

@Mapper
interface CarMapper {

    @Select("SELECT * FROM cars")
    fun findAll(): List<CarEntity>?

    @Select("SELECT * FROM cars WHERE id = #{id}")
    fun findById(id: Long): CarEntity?

    @Select("SELECT * FROM cars WHERE license_plate = #{licensePlate}")
    fun findByLicensePlate(licensePlate: String): CarEntity?

    @Insert(
        """INSERT INTO cars (driver_id, color, license_plate, brand, seats)
        VALUES (#{driverId}, #{color}, #{licensePlate}, #{brand}, #{seats})
        """
    )
    @Options(useGeneratedKeys = true, keyProperty = "id")
    fun create(car: CarEntity): Int

    @Update(
        """UPDATE cars
        SET driver_id=#{driverId}, color=#{color}, license_plate=#{licensePlate},
        brand=#{brand}, seats=#{seats}
        WHERE id=#{id}
        """
    )
    fun update(car: CarEntity): Int

    @Delete("DELETE FROM cars WHERE id=#{id}")
    fun delete(id: Long): Int
}
